/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.*;
import org.qi4j.spi.entitystore.helpers.JSONEntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.*;
import java.util.*;

public class EntityResource
        extends ServerResource
{
    public static Object toValue( String stringValue, QualifiedName propertyName, TypeName propertyType )
            throws IllegalArgumentException
    {
        Object newValue = null;
        try
        {
            // TODO A ton of more types need to be added here. Converter registration mechanism needed?
            newValue = null;
            if (propertyType.isClass( String.class ))
            {
                newValue = stringValue;
            } else if (propertyType.isClass( Integer.class ))
            {
                newValue = Integer.parseInt( stringValue );
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException( "Value '" + stringValue + "' is not of type " + propertyType );
        }

        return newValue;
    }

    public static String toString( Object newValue, ValueType valueType )
            throws IllegalArgumentException, JSONException
    {
        if (newValue == null)
        {
            return "";
        } else
        {
            return valueType.toJSON( newValue ).toString().replace( "\"", "&quot;" );
        }
    }

    @Service
    private EntityStore entityStore;

    @Structure
    private Qi4jSPI spi;

    @Structure
    private ModuleSPI module;

    @Uses
    EntityStateSerializer entitySerializer;

    private String identity;

    public EntityResource()
    {
        // Define the supported variant.
        getVariants().addAll( Arrays.asList(
                new Variant(MediaType.TEXT_HTML),
                new Variant(MediaType.APPLICATION_RDF_XML),
                new Variant(MediaType.APPLICATION_JSON)));
        setNegotiated( true );
    }

    @Override
    protected void doInit() throws ResourceException
    {
        // /entity/{identity}
        Map<String, Object> attributes = getRequest().getAttributes();
        identity = (String) attributes.get( "identity" );
    }

    @Override
    protected Representation delete() throws ResourceException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "Remove entity" );
        EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( usecase, module );
        try
        {
            EntityReference identityRef = EntityReference.parseEntityReference( identity );
            uow.getEntityState( identityRef ).remove();
            uow.applyChanges().commit();
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch (EntityNotFoundException e)
        {
            uow.discard();
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }

        return new EmptyRepresentation();
    }

    @Override
    protected Representation get( Variant variant ) throws ResourceException
    {
        EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase( "Get entity" ), module );

        try
        {
            EntityState entityState = getEntityState( uow );

            // Check modification date
            Date lastModified = getRequest().getConditions().getModifiedSince();
            if (lastModified != null)
            {
                if (lastModified.getTime() / 1000 == entityState.lastModified() / 1000)
                {
                    throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED );
                }
            }

            // Generate the right representation according to its media type.
            if (MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ))
            {
                return entityHeaders( representRdfXml( entityState ), entityState );
            } else if (MediaType.TEXT_HTML.equals( variant.getMediaType() ))
            {
                return entityHeaders( representHtml( entityState ), entityState );
            }else if (MediaType.APPLICATION_JSON.equals( variant.getMediaType() ))
            {
                return entityHeaders( representJson( entityState ), entityState );
            }
        } catch (ResourceException ex)
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
            EntityReference entityReference = EntityReference.parseEntityReference( identity );
            entityState = unitOfWork.getEntityState( entityReference );
        }
        catch (EntityNotFoundException e)
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        return entityState;
    }

    private Representation entityHeaders( Representation representation, EntityState entityState )
    {
        representation.setModificationDate( new Date( entityState.lastModified() ) );
        representation.setTag( new Tag( "" + entityState.version() ) );
        representation.setCharacterSet( CharacterSet.UTF_8 );
        representation.setLanguages( Collections.singletonList( Language.ENGLISH ) );

        return representation;
    }

    private Representation representHtml( final EntityState entity )
    {
        return new WriterRepresentation( MediaType.TEXT_HTML )
        {
            public void write( Writer writer ) throws IOException
            {
                PrintWriter out = new PrintWriter( writer );
                out.println( "<html><head><title>" + entity.identity() + "</title><link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" + entity.identity() + ".rdf\"/></head><body>" );
                out.println( "<h1>" + entity.identity() + "</h1>" );

                out.println( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
                out.println( "<fieldset><legend>Properties</legend>\n<table>" );

                final EntityType type = entity.entityDescriptor().entityType();

                for (PropertyType propertyType : type.properties())
                {
                    Object value = entity.getProperty( propertyType.qualifiedName() );
                    try
                    {
                        out.println( "<tr><td>" +
                                "<label for=\"" + propertyType.qualifiedName() + "\" >" +
                                propertyType.qualifiedName().name() +
                                "</label></td>\n" +
                                "<td><input " +
                                "size=\"80\" " +
                                "type=\"text\" " +
                                (propertyType.propertyType() != PropertyType.PropertyTypeEnum.MUTABLE ? "readonly=\"true\" " : "") +
                                "name=\"" + propertyType.qualifiedName() + "\" " +
                                "value=\"" + EntityResource.toString( value, propertyType.type() ) + "\"></td></tr>" );
                    } catch (JSONException e)
                    {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
                    }
                }
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>Associations</legend>\n<table>" );
                for (AssociationType associationType : type.associations())
                {
                    Object value = entity.getAssociation( associationType.qualifiedName() );
                    if (value == null)
                    {
                        value = "";
                    }
                    out.println( "<tr><td>" +
                            "<label for=\"" + associationType.qualifiedName() + "\" >" +
                            associationType.qualifiedName().name() +
                            "</label></td>\n" +
                            "<td><input " +
                            "type=\"text\" " +
                            "size=\"80\" " +
                            "name=\"" + associationType.qualifiedName() + "\" " +
                            "value=\"" + value + "\"></td></tr>" );
                }
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>Many manyAssociations</legend>\n<table>" );
                for (ManyAssociationType associationType : type.manyAssociations())
                {
                    ManyAssociationState identities = entity.getManyAssociation( associationType.qualifiedName() );
                    String value = "";
                    for (EntityReference identity : identities)
                    {
                        value += identity.toString() + "\n";
                    }

                    out.println( "<tr><td>" +
                            "<label for=\"" + associationType.qualifiedName() + "\" >" +
                            associationType.qualifiedName().name() +
                            "</label></td>\n" +
                            "<td><textarea " +
                            "rows=\"10\" " +
                            "cols=\"80\" " +
                            "name=\"" + associationType.qualifiedName() + "\" >" +
                            value +
                            "</textarea></td></tr>" );
                }
                out.println( "</table></fieldset>\n" );
                out.println( "<input type=\"submit\" value=\"Update\"/></form>\n" );

                out.println( "</body></html>\n" );
            }
        };
    }

    private Representation representJson( EntityState entityState )
    {
        if (entityState instanceof JSONEntityState)
        {
            JSONEntityState jsonState = (JSONEntityState) entityState;
            return new StringRepresentation(jsonState.state().toString(), MediaType.APPLICATION_JSON);
        } else
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
        }
    }

    private Representation representRdfXml( final EntityState entity ) throws ResourceException
    {
        Representation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
        {
            public void write( Writer writer ) throws IOException
            {
                try
                {
                    Iterable<Statement> statements = entitySerializer.serialize( entity );
                    new RdfXmlSerializer().serialize( statements, writer );
                }
                catch (RDFHandlerException e)
                {
                    throw (IOException) new IOException().initCause( e );
                }

                writer.close();
            }
        };
        representation.setCharacterSet( CharacterSet.UTF_8 );
        return representation;

    }

    @Override
    protected Representation put( Representation representation, Variant variant ) throws ResourceException
    {
        return post( representation, variant );
    }

    @Override
    public Representation post( Representation entityRepresentation, Variant variant )
            throws ResourceException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "Update entity" );
        MetaInfo info = new MetaInfo();
        EntityStoreUnitOfWork unitOfWork = entityStore.newUnitOfWork( usecase, module );
        EntityState entity = getEntityState( unitOfWork );

        Form form = new Form( entityRepresentation );

        try
        {
            final EntityType type = entity.entityDescriptor().entityType();

            // Create JSON string of all properties
            StringBuilder str = new StringBuilder();
            str.append( '{' );

            boolean first = true;
            for (PropertyType propertyType : type.properties())
            {
                if (propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE)
                {
                    if (!first)
                        str.append( "," );
                    first = false;

                    str.append( '"' ).append( propertyType.qualifiedName().name() ).append( "\":" );

                    String newStringValue = form.getFirstValue( propertyType.qualifiedName().toString() );

                    if (newStringValue == null)
                    {
                        str.append("null");
                    } else
                    {
                        if (propertyType.type().isValue())
                        {
                            str.append(newStringValue);
                        } else if (propertyType.type().isString())
                        {
                            str.append( '"' ).append( newStringValue ).append( '"' );
                        } else
                        {
                            str.append(newStringValue);
                        }
                    }
                }
            }

            str.append( '}' );
            
            // Parse JSON into properties
            JSONObject properties = new JSONObject(str.toString());

            for (PropertyType propertyType : type.properties())
            {
                if (propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE)
                {
                    Object jsonValue = properties.get( propertyType.qualifiedName().name() );

                    if (jsonValue == JSONObject.NULL)
                        jsonValue = null;

                    Object value = propertyType.type().fromJSON( jsonValue, module );

                    entity.setProperty( propertyType.qualifiedName(), value );
                }
            }

            for (AssociationType associationType : type.associations())
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName().toString() );
                if (newStringAssociation == null || newStringAssociation.equals( "" ))
                {
                    entity.setAssociation( associationType.qualifiedName(), null );
                } else
                {
                    entity.setAssociation( associationType.qualifiedName(), EntityReference.parseEntityReference( newStringAssociation ) );
                }
            }
            for (ManyAssociationType associationType : type.manyAssociations())
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName().toString() );
                ManyAssociationState manyAssociation = entity.getManyAssociation( associationType.qualifiedName() );
                if (newStringAssociation == null)
                {
                    // Remove "left-overs"
                    for (EntityReference entityReference : manyAssociation)
                    {
                        manyAssociation.remove( entityReference );
                    }
                    continue;
                }

                BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                String identity;

                try
                {
                    // Synchronize old and new association
                    int index = 0;
                    while ((identity = bufferedReader.readLine()) != null)
                    {
                        EntityReference reference = new EntityReference( identity );

                        if (manyAssociation.count() < index && manyAssociation.get( index ).equals( reference ))
                        {
                            continue;
                        }

                        try
                        {
                            unitOfWork.getEntityState( reference );

                            manyAssociation.remove( reference );
                            manyAssociation.add( index++, reference );
                        } catch (EntityNotFoundException e)
                        {
                            // Ignore this entity - doesn't exist
                        }
                    }

                    // Remove "left-overs"
                    while (manyAssociation.count() > index)
                    {
                        manyAssociation.remove( manyAssociation.get( index ) );
                    }
                }
                catch (IOException e)
                {
                    // Ignore
                }
            }
        }
        catch (JSONException e)
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        catch (IllegalArgumentException e)
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }

        try
        {
            unitOfWork.applyChanges().commit();
        }
        catch (ConcurrentEntityStateModificationException e)
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
        }
        catch (EntityNotFoundException e)
        {
            throw new ResourceException( Status.CLIENT_ERROR_GONE );
        }

        getResponse().setStatus( Status.SUCCESS_RESET_CONTENT );

        return new EmptyRepresentation();
    }
}