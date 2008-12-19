/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.rest.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.api.structure.Module;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.WriterRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Listing of all Entities.
 *
 * Mapped to /entity/{type}
 */
public class EntitiesResource extends Resource
{
    @Service private EntityFinder entityFinder;
    @Service private IdentityGenerator identityGenerator;
    @Service private EntityStore entityStore;
    @Structure UnitOfWorkFactory uowf;
    @Structure Qi4jSPI spi;
    @Structure Module module;

    private String type;

    public EntitiesResource( @Uses Context context,
                             @Uses Request request,
                             @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        setModifiable( false );

        final Map<String, Object> attributes = getRequest().getAttributes();
        type = (String) attributes.get( "type" );

        setModifiable( true );
    }

    @Override public Representation represent( final Variant variant )
        throws ResourceException
    {
        // Generate the right representation according to its media type.
        if( MediaType.TEXT_XML.equals( variant.getMediaType() ) )
        {
            return representXml();
        }
        else if( MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ) )
        {
            return representRdf();
        }
        else if( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            return representHtml();
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private Representation representXml()
        throws ResourceException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( type, null, null, null, null );

            DomRepresentation representation = new DomRepresentation( MediaType.TEXT_XML );
            // Generate a DOM document representing the item.
            Document d = representation.getDocument();

            Element entitiesElement = d.createElement( "entities" );
            d.appendChild( entitiesElement );
            for( QualifiedIdentity entity : query )
            {
                Element entityElement = d.createElement( "entity" );
                entitiesElement.appendChild( entityElement );
                entityElement.setAttribute( "href", getRequest().getResourceRef().getPath() + "/" + entity.identity() );
                entityElement.appendChild( d.createTextNode( entity.identity() ) );
            }
            d.normalizeDocument();

            // Returns the XML representation of this document.
            return representation;
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }
    }

    private Representation representRdf()
        throws ResourceException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( type, null, null, null, null );

            Representation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
            {
                public void write( Writer writer ) throws IOException
                {
                    PrintWriter out = new PrintWriter( writer );
                    out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<rdf:RDF\n" +
                                 "\txmlns=\"urn:qi4j:\"\n" +
                                 "\txmlns:qi4j=\"http://www.qi4j.org/rdf/model/1.0/\"\n" +
                                 "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                                 "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" );
                    for( QualifiedIdentity qualifiedIdentity : query )
                    {
                        out.println( "<" + qualifiedIdentity.type() + " rdf:about=\"urn:qi4j:" + qualifiedIdentity.identity() + "\"/>" );
                    }

                    out.println( "</rdf:RDF>" );
                    out.close();
                }
            };
            representation.setCharacterSet( CharacterSet.UTF_8 );
            return representation;
        }
        catch( EntityFinderException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    @Override @SuppressWarnings( "unused" )
    public void acceptRepresentation( Representation entity ) throws ResourceException
    {
        try
        {
            Form form = new Form( entity );

            String identity = form.getFirstValue( "org.qi4j.api.entity.Identity:identity" );
            if( identity == null || identity.equals( "" ) )
            {
                identity = identityGenerator.generate( (Class<? extends Identity>) module.classLoader().loadClass( type ) );
            }

            QualifiedIdentity qid = new QualifiedIdentity( identity, type );
            EntityState entityState = entityStore.newEntityState( qid );


            EntityType entityType = entityState.entityType();
            for( PropertyType propertyType : entityType.properties() )
            {
                if( propertyType.propertyType() != PropertyType.PropertyTypeEnum.COMPUTED )
                {
                    String newStringValue = form.getFirstValue( propertyType.qualifiedName() );
                    Object newValue = EntityResource.toValue( newStringValue, propertyType.qualifiedName(), propertyType.type() );
                    entityState.setProperty( propertyType.qualifiedName(), newValue );
                }
            }
            for( AssociationType associationType : entityType.associations() )
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName() );
                if( newStringAssociation == null || newStringAssociation.equals( "" ) )
                {
                    entityState.setAssociation( associationType.qualifiedName(), null );
                }
                else
                {
                    entityState.setAssociation( associationType.qualifiedName(), QualifiedIdentity.parseQualifiedIdentity( newStringAssociation ) );
                }
            }
            for( ManyAssociationType associationType : entityType.manyAssociations() )
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName() );
                Collection<QualifiedIdentity> manyAssociation = entityState.getManyAssociation( associationType.qualifiedName() );
                if( newStringAssociation == null )
                {
                    continue;
                }

                manyAssociation.clear();
                BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                String qualifiedIdentity;
                try
                {
                    while( ( qualifiedIdentity = bufferedReader.readLine() ) != null )
                    {
                        manyAssociation.add( QualifiedIdentity.parseQualifiedIdentity( qualifiedIdentity ) );
                    }
                }
                catch( IOException e )
                {
                    // Ignore
                }
            }

            entityStore.prepare( Collections.singleton( entityState ), Collections.EMPTY_LIST, Collections.EMPTY_LIST ).commit();

            getResponse().setStatus( Status.REDIRECTION_PERMANENT );
            Reference ref = getRequest().getResourceRef().addSegment( qid.identity() + ".html" );
            getResponse().setLocationRef( ref );
        }
        catch( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }
    }

    private Representation representHtml()
        throws ResourceException
    {
        Representation representation = new WriterRepresentation( MediaType.TEXT_HTML )
        {
            public void write( Writer writer ) throws IOException
            {
                PrintWriter out = new PrintWriter( writer );

                out.println( "<html><head><title>Entities of type " + type + "</title></head><body>" );

                listEntitiesHtml( out );

                // Form for new entity
                newEntityForm( out );

                out.append( "</body></html>" );
                out.close();
            }
        };
        representation.setCharacterSet( CharacterSet.UTF_8 );
        return representation;
    }

    private void listEntitiesHtml( PrintWriter out )
        throws IOException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( type, null, null, null, null );

            out.println( "<h1>Entities of type " + type + "</h1><ul>" );
            for( QualifiedIdentity entity : query )
            {
                out.println( "<li><a href=\"" + getRequest().getResourceRef().getPath() + "/" + entity.identity() + ".html\">" + entity.identity() + "</a></li>" );
            }
            out.println( "</ul>" );
        }
        catch( EntityFinderException e )
        {
            throw new IOException( e.getMessage() );
        }
    }

    private void newEntityForm( PrintWriter out ) throws IOException
    {
        EntityType entityType = null;
        try
        {
            entityType = spi.getEntityDescriptor( module.classLoader().loadClass( type ), module ).entityType();
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }

        out.println( "<h2>Create entity</h2>" );
        out.append( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
        out.append( "<fieldset><legend>Properties</legend>\n<table>" );
        for( PropertyType propertyType : entityType.properties() )
        {
            PropertyType.PropertyTypeEnum propertyTypeEnum = propertyType.propertyType();
            out.append( "<tr><td>" +
                        "<label for=\"" + propertyType.qualifiedName() + "\" >" +
                        GenericPropertyInfo.getName( propertyType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><input " +
                        "type=\"text\" " +
                        ( propertyTypeEnum == PropertyType.PropertyTypeEnum.COMPUTED ? "readonly=\"true\" " : "" ) +
                        "name=\"" + propertyType.qualifiedName() + "\" " +
                        "value=\"" + "" + "\"></td></tr>" ); // TODO Show default value for property?
        }
        out.append( "</table></fieldset>\n" );

        out.append( "<fieldset><legend>Associations</legend>\n<table>" );
        for( AssociationType associationType : entityType.associations() )
        {
            out.append( "<tr><td>" +
                        "<label for=\"" + associationType.qualifiedName() + "\" >" +
                        GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><input " +
                        "type=\"text\" " +
                        "size=\"40\" " +
                        "name=\"" + associationType.qualifiedName() + "\" " +
                        "></td></tr>" );
        }
        out.append( "</table></fieldset>\n" );

        out.append( "<fieldset><legend>Many associations</legend>\n<table>" );
        for( ManyAssociationType associationType : entityType.manyAssociations() )
        {
            out.append( "<tr><td>" +
                        "<label for=\"" + associationType.qualifiedName() + "\" >" +
                        GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><textarea " +
                        "rows=\"10\" " +
                        "cols=\"40\" " +
                        "name=\"" + associationType.qualifiedName() + "\" >" +
                        "</textarea></td></tr>" );
        }
        out.append( "</table></fieldset>\n" );
        out.append( "<input type=\"submit\" value=\"Create\"/></form>\n" );
    }
}