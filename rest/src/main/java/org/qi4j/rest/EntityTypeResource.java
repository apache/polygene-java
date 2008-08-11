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
package org.qi4j.rest;

import java.util.Map;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.structure.Module;
import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityTypeResource extends Resource
{
    @Service private EntityFinder entityFinder;

    private String type;
    private Request request;

    public EntityTypeResource( @Uses Context context,
                               @Uses Request request,
                               @Uses Response response,
                               @Structure Module module )
        throws ClassNotFoundException
    {
        super( context, request, response );
        this.request = request;

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        setModifiable( false );

        final Map<String, Object> attributes = getRequest().getAttributes();
        type = (String) attributes.get( "type" );

        String ext = getRequest().getResourceRef().getExtensions();

        if( ext != null )
        {
            type = type.substring( 0, type.length() - ext.length() - 1 );
        }
    }

    @Override public Representation represent( final Variant variant )
        throws ResourceException
    {
        String ext = getRequest().getResourceRef().getExtensions();
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
                entityElement.setAttribute( "href", request.getResourceRef().getPath() + "/" + entity.identity() );
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

            DomRepresentation representation = new DomRepresentation( MediaType.TEXT_XML );
            // Generate a DOM document representing the item.
            Document d = representation.getDocument();

            Element entitiesElement = d.createElement( "entities" );
            d.appendChild( entitiesElement );
            for( QualifiedIdentity entity : query )
            {
                Element entityElement = d.createElement( "entity" );
                entitiesElement.appendChild( entityElement );
                entityElement.setAttribute( "href", request.getResourceRef().getPath() + "/" + entity.identity() );
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

    private Representation representHtml()
        throws ResourceException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( type, null, null, null, null );

            StringBuffer buf = new StringBuffer();
            buf.append( "<html><body><h1>Entities</h1><ul>" );

            for( QualifiedIdentity entity : query )
            {
                buf.append( "<li><a href=\"" + request.getResourceRef().getPath() + "/" + entity.identity() + "\">" + entity.identity() + "</a></li>" );
            }
            buf.append( "</ul></body></html>" );

            // Returns the XML representation of this document.
            return new StringRepresentation( buf, MediaType.TEXT_HTML, Language.ENGLISH );
        }
        catch( EntityFinderException e )
        {
            throw new ResourceException( e );
        }
    }
}
