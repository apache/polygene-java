/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.rest.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.library.rdf.entity.EntityStateSerializer;
import org.apache.polygene.library.rdf.serializer.RdfXmlSerializer;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.ConcurrentEntityStateModificationException;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.helpers.JSONEntityState;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class EntityResource
    extends ServerResource
{

    @Service
    private EntityStore entityStore;

    @Service
    private JsonSerialization serialization;

    @Structure
    private ModuleDescriptor module;

    @Uses
    private EntityStateSerializer entitySerializer;

    private Identity identity;

    public EntityResource()
    {
        // Define the supported variant.
        getVariants().addAll( Arrays.asList(
            new Variant( MediaType.TEXT_HTML ),
            new Variant( MediaType.APPLICATION_RDF_XML ),
            new Variant( MediaType.APPLICATION_JSON ) ) );
        setNegotiated( true );
        setAllowedMethods( Collections.singleton( Method.ALL ) );
    }

    @Override
    protected void doInit()
        throws ResourceException
    {
        // /entity/{reference}
        Map<String, Object> attributes = getRequest().getAttributes();
        identity = StringIdentity.fromString( (String) attributes.get( "reference" ) );
    }

    @Override
    protected Representation delete( Variant variant )
        throws ResourceException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "Remove entity" );
        EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( module, usecase, SystemTime.now() );
        try
        {
            EntityReference reference = EntityReference.create( identity );
            uow.entityStateOf( module, reference ).remove();
            uow.applyChanges().commit();
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch( EntityNotFoundException e )
        {
            uow.discard();
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }

        return new EmptyRepresentation();
    }

    @Override
    protected Representation get( Variant variant )
        throws ResourceException
    {
        EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( module, UsecaseBuilder.newUsecase( "Get entity" ),
                                                               SystemTime.now() );

        try
        {
            EntityState entityState = getEntityState( uow );

            // Check modification date
            java.util.Date lastModified = getRequest().getConditions().getModifiedSince();
            if( lastModified != null )
            {
                if( lastModified.toInstant().getEpochSecond() == entityState.lastModified().getEpochSecond() )
                {
                    throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED );
                }
            }

            // Generate the right representation according to its media type.
            if( MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ) )
            {
                return entityHeaders( representRdfXml( entityState ), entityState );
            }
            else if( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
            {
                return entityHeaders( representHtml( entityState ), entityState );
            }
            else if( MediaType.APPLICATION_JSON.equals( variant.getMediaType() ) )
            {
                return entityHeaders( representJson( entityState ), entityState );
            }
        }
        catch( ResourceException ex )
        {
            uow.discard();
            throw ex;
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private EntityState getEntityState( EntityStoreUnitOfWork unitOfWork )
        throws ResourceException
    {
        EntityState entityState;
        try
        {
            EntityReference entityReference = EntityReference.create( identity );
            entityState = unitOfWork.entityStateOf( module, entityReference );
        }
        catch( EntityNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        return entityState;
    }

    private Representation entityHeaders( Representation representation, EntityState entityState )
    {
        representation.setModificationDate( java.util.Date.from( entityState.lastModified() ) );
        representation.setTag( new Tag( "" + entityState.version() ) );
        representation.setCharacterSet( CharacterSet.UTF_8 );
        representation.setLanguages( Collections.singletonList( Language.ENGLISH ) );

        return representation;
    }

    private Representation representHtml( final EntityState entity )
    {
        return new WriterRepresentation( MediaType.TEXT_HTML )
        {
            @Override
            public void write( Writer writer )
                throws IOException
            {
                PrintWriter out = new PrintWriter( writer );
                out.println( "<html><head><title>" + entity.entityReference() + "</title>"
                             + "<link rel=\"alternate\" type=\"application/rdf+xml\" "
                             + "href=\"" + entity.entityReference() + ".rdf\"/></head><body>" );
                out.println( "<h1>" + entity.entityReference() + "</h1>" );

                out.println( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
                out.println( "<fieldset><legend>Properties</legend>\n<table>" );

                final EntityDescriptor descriptor = entity.entityDescriptor();

                descriptor.state().properties().forEach( persistentProperty -> {
                    Object value = entity.propertyValueOf( persistentProperty.qualifiedName() );
                    out.println( "<tr><td>"
                                 + "<label for=\"" + persistentProperty.qualifiedName() + "\" >"
                                 + persistentProperty.qualifiedName().name()
                                 + "</label></td>\n"
                                 + "<td><input "
                                 + "size=\"80\" "
                                 + "type=\"text\" "
                                 + ( persistentProperty.isImmutable() ? "readonly=\"true\" " : "" )
                                 + "name=\"" + persistentProperty.qualifiedName() + "\" "
                                 + "value=\"" + ( value == null ? "" : serialization.serialize( value ) )
                                 + "\"/></td></tr>" );
                } );
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>Associations</legend>\n<table>" );
                descriptor.state().associations().forEach( associationType -> {
                    Object value = entity.associationValueOf( associationType.qualifiedName() );
                    if( value == null )
                    {
                        value = "";
                    }
                    out.println( "<tr><td>"
                                 + "<label for=\"" + associationType.qualifiedName() + "\" >"
                                 + associationType.qualifiedName().name()
                                 + "</label></td>\n"
                                 + "<td><input "
                                 + "type=\"text\" "
                                 + "size=\"80\" "
                                 + "name=\"" + associationType.qualifiedName() + "\" "
                                 + "value=\"" + value + "\"/></td></tr>" );
                } );
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>ManyAssociations</legend>\n<table>" );
                descriptor.state().manyAssociations().forEach( associationType -> {
                    ManyAssociationState references = entity.manyAssociationValueOf( associationType.qualifiedName() );
                    String value = "";
                    for( EntityReference reference : references )
                    {
                        value += reference.identity() + "\n";
                    }

                    out.println( "<tr><td>"
                                 + "<label for=\"" + associationType.qualifiedName() + "\" >"
                                 + associationType.qualifiedName().name()
                                 + "</label></td>\n"
                                 + "<td><textarea "
                                 + "rows=\"10\" "
                                 + "cols=\"80\" "
                                 + "name=\"" + associationType.qualifiedName() + "\" >"
                                 + value
                                 + "</textarea></td></tr>" );
                });
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>NamedAssociations</legend>\n<table>" );
                descriptor.state().namedAssociations().forEach( associationType -> {
                    NamedAssociationState identities = entity.namedAssociationValueOf( associationType.qualifiedName() );
                    String value = "";
                    for( String name : identities )
                    {
                        value += name + "\n" + identities.get( name ).identity() + "\n";
                    }

                    out.println( "<tr><td>"
                                 + "<label for=\"" + associationType.qualifiedName() + "\" >"
                                 + associationType.qualifiedName().name()
                                 + "</label></td>\n"
                                 + "<td><textarea "
                                 + "rows=\"10\" "
                                 + "cols=\"80\" "
                                 + "name=\"" + associationType.qualifiedName() + "\" >"
                                 + value
                                 + "</textarea></td></tr>" );
                } );
                out.println( "</table></fieldset>\n" );

                out.println( "<input type=\"submit\" value=\"Update\"/></form>\n" );

                out.println( "</body></html>\n" );
            }
        };
    }

    private Representation representJson( EntityState entityState )
    {
        // TODO This guy needs to represent an Entity as JSON
        if( entityState instanceof JSONEntityState )
        {
            JSONEntityState jsonState = (JSONEntityState) entityState;
            return new StringRepresentation( jsonState.state().toString(), MediaType.APPLICATION_JSON );
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_ACCEPTABLE );
        }
    }

    private Representation representRdfXml( final EntityState entity )
        throws ResourceException
    {
        Representation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
        {
            @Override
            public void write( Writer writer )
                throws IOException
            {
                try
                {
                    Iterable<Statement> statements = entitySerializer.serialize( entity );
                    new RdfXmlSerializer().serialize( statements, writer );
                }
                catch( RDFHandlerException e )
                {
                    throw new IOException( e );
                }
            }
        };
        representation.setCharacterSet( CharacterSet.UTF_8 );
        return representation;
    }

    @Override
    protected Representation put( Representation representation, Variant variant )
        throws ResourceException
    {
        return post( representation, variant );
    }

    @Override
    public Representation post( Representation entityRepresentation, Variant variant )
        throws ResourceException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "Update entity" );
        EntityStoreUnitOfWork unitOfWork = entityStore.newUnitOfWork( module, usecase, SystemTime.now() );
        EntityState entity = getEntityState( unitOfWork );

        Form form = new Form( entityRepresentation );

        try
        {
            final EntityDescriptor descriptor = entity.entityDescriptor();

            // Parse JSON into properties
            descriptor.state().properties().forEach( persistentProperty -> {
                if( !persistentProperty.isImmutable() )
                {
                    String formValue = form.getFirstValue( persistentProperty.qualifiedName().name(), null );
                    if( formValue == null )
                    {
                        entity.setPropertyValue( persistentProperty.qualifiedName(), null );
                    }
                    else
                    {
                        entity.setPropertyValue(
                            persistentProperty.qualifiedName(),
                            serialization.deserialize( module, persistentProperty.valueType(), formValue ) );
                    }
                }
            } );

            descriptor.state().associations().forEach( associationType -> {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName().name() );
                if( newStringAssociation == null || newStringAssociation.isEmpty() )
                {
                    entity.setAssociationValue( associationType.qualifiedName(), null );
                }
                else
                {
                    entity.setAssociationValue( associationType.qualifiedName(),
                                                EntityReference.parseEntityReference( newStringAssociation ) );
                }
            } );
            descriptor.state().manyAssociations().forEach( associationType -> {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName().name() );
                ManyAssociationState manyAssociation = entity.manyAssociationValueOf( associationType.qualifiedName() );
                if( newStringAssociation == null )
                {
                    // Remove "left-overs"
                    manyAssociation.clear();
                }
                else
                {
                    BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                    String identity;

                    try
                    {
                        // Synchronize old and new association
                        int index = 0;
                        while( ( identity = bufferedReader.readLine() ) != null )
                        {
                            EntityReference reference = EntityReference.parseEntityReference( identity );

                            if( manyAssociation.count() < index && manyAssociation.get( index ).equals( reference ) )
                            {
                                continue;
                            }

                            try
                            {
                                unitOfWork.entityStateOf( module, reference );

                                manyAssociation.remove( reference );
                                manyAssociation.add( index++, reference );
                            }
                            catch( EntityNotFoundException e )
                            {
                                // Ignore this entity - doesn't exist
                            }
                        }

                        // Remove "left-overs"
                        while( manyAssociation.count() > index )
                        {
                            manyAssociation.remove( manyAssociation.get( index ) );
                        }
                    }
                    catch( IOException e )
                    {
                        // Ignore
                    }
                }
            } );
            descriptor.state().namedAssociations().forEach( associationType -> {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName().name() );
                NamedAssociationState namedAssociation = entity.namedAssociationValueOf( associationType.qualifiedName() );
                if( newStringAssociation == null )
                {
                    // Remove "left-overs"
                    namedAssociation.clear();
                }
                else
                {
                    Set<String> names = new HashSet<>();
                    BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                    String line;
                    try
                    {
                        while( ( line = bufferedReader.readLine() ) != null )
                        {
                            String name = line;
                            line = bufferedReader.readLine();
                            if( line == null )
                            {
                                break;
                            }
                            String identity = line;
                            EntityReference reference = EntityReference.parseEntityReference( identity );
                            try
                            {
                                unitOfWork.entityStateOf( module, reference );

                                namedAssociation.remove( name );
                                namedAssociation.put( name, reference );

                                names.add( name );
                            }
                            catch( EntityNotFoundException e )
                            {
                                // Ignore this entity - doesn't exist
                            }
                        }

                        // Remove "left-overs"
                        StreamSupport.stream( namedAssociation.spliterator(), false )
                            .filter( assocName -> !names.contains( assocName ) )
                            .forEach( namedAssociation::remove );
                    }
                    catch( IOException e )
                    {
                        // Ignore
                    }
                }
            } );
        }
        catch( SerializationException | IllegalArgumentException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }

        try
        {
            unitOfWork.applyChanges().commit();
        }
        catch( ConcurrentEntityStateModificationException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
        }
        catch( EntityNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_GONE );
        }

        getResponse().setStatus( Status.SUCCESS_RESET_CONTENT );

        return new EmptyRepresentation();
    }
}
