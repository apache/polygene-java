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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Text;
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
 * Mapped to /entity
 */
public class AllEntitiesResource extends Resource
{
    @Service private EntityFinder entityFinder;
    @Service private EntityStore entityStore;

    public AllEntitiesResource( @Uses Context context,
                                @Uses Request request,
                                @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_JAVA_OBJECT ) );
        getVariants().add( new Variant( MediaType.APPLICATION_ATOM ) );
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
        else if( MediaType.APPLICATION_ATOM.equals( variant.getMediaType() ) )
        {
            return representAtom();
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private Representation representXml()
        throws ResourceException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( Entity.class.getName(), null, null, null, null );

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
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( Entity.class.getName(), null, null, null, null );

            WriterRepresentation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
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

    private Representation representHtml()
        throws ResourceException
    {
        try
        {
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( Entity.class.getName(), null, null, null, null );
            Representation representation = new WriterRepresentation( MediaType.TEXT_HTML )
            {
                public void write( Writer buf ) throws IOException
                {
                    PrintWriter out = new PrintWriter( buf );
                    out.println( "<html><head><title>All entities</title</head><body><h1>All entities</h1><ul>" );

                    for( QualifiedIdentity entity : query )
                    {
                        out.println( "<li><a href=\"" + getRequest().getResourceRef().getPath() + "/" + entity.type() + "/" + entity.identity() + ".html\">" + entity.identity() + "</a></li>" );
                    }
                    out.println( "</ul></body></html>" );
                    out.close();
                }
            };
            representation.setCharacterSet( CharacterSet.UTF_8 );
            return representation;
        }
        catch( EntityFinderException e )
        {
            throw new ResourceException( e );
        }
    }

    private Representation representAtom() throws ResourceException
    {
        try
        {
            Feed feed = new Feed();
            feed.setTitle( new Text( MediaType.TEXT_PLAIN, "All entities" ) );
            List<Entry> entries = feed.getEntries();
            final Iterable<QualifiedIdentity> query = entityFinder.findEntities( Entity.class.getName(), null, null, null, null );
            for( QualifiedIdentity qualifiedIdentity : query )
            {
                Entry entry = new Entry();
                entry.setTitle( new Text( MediaType.TEXT_PLAIN, qualifiedIdentity.toString() ) );
                Link link = new Link();
                link.setHref( getRequest().getResourceRef().clone().addSegment( qualifiedIdentity.type() ).addSegment( qualifiedIdentity.identity() ) );
                entry.getLinks().add( link );
                entries.add( entry );
            }

            return feed;
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }
    }

    public void acceptRepresentation( Representation entity ) throws ResourceException
    {
        try
        {
            InputStream in = entity.getStream();
            ObjectInputStream oin = new ObjectInputStream( in );
            Iterable<EntityState> newState = (Iterable<EntityState>) oin.readObject();
            Iterable<EntityState> loadedState = (Iterable<EntityState>) oin.readObject();
            Iterable<QualifiedIdentity> removedState = (Iterable<QualifiedIdentity>) oin.readObject();

            // Ensure that EntityTypes are registered properly
            Set<EntityType> entityTypes = new HashSet<EntityType>();
            for( EntityState entityState : newState )
            {
                entityTypes.add( entityState.entityType() );
            }
            for( EntityType entityType : entityTypes )
            {
                try
                {
                    entityStore.getEntityType( entityType.type() );
                }
                catch( UnknownEntityTypeException e )
                {
                    entityStore.registerEntityType( entityType );
                }
            }

            // Store state
            try
            {
                entityStore.prepare( newState, loadedState, removedState ).commit();
            }
            catch( ConcurrentEntityStateModificationException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
            }
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }
    }
}