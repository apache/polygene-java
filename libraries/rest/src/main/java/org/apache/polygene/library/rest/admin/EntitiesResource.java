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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.spi.query.EntityFinder;
import org.apache.polygene.spi.query.EntityFinderException;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Link;
import org.restlet.ext.atom.Text;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import static java.util.stream.Collectors.toList;

/**
 * Listing of all Entities.
 * <p>
 * Mapped to /entity
 * </p>
 */
public class EntitiesResource
    extends ServerResource
{

    @Service
    private EntityFinder entityFinder;
    @Service
    private Serialization stateSerialization;

    public EntitiesResource()
    {
        super();

        // Define the supported variants.
        getVariants().addAll(
            Arrays.asList( new Variant( MediaType.TEXT_HTML ), new Variant( MediaType.APPLICATION_RDF_XML ),
                           new Variant( MediaType.APPLICATION_JSON ), new Variant( MediaType.APPLICATION_ATOM ) ) );

        setNegotiated( true );
    }

    @Override
    protected Representation get( Variant variant )
        throws ResourceException
    {
        // Generate the right representation according to its media type.
        if( MediaType.APPLICATION_JSON.equals( variant.getMediaType() ) )
        {
            return representJson();
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

    private Representation representJson()
        throws ResourceException
    {
        try
        {
            final Iterable<EntityReference> query = entityFinder.findEntities( EntityComposite.class, null, null, null, null, Collections.emptyMap() )
                                                                .collect( toList() );
            return new OutputRepresentation( MediaType.APPLICATION_JSON )
            {
                @Override
                public void write( OutputStream outputStream )
                    throws IOException
                {
                    stateSerialization.serialize( new OutputStreamWriter( outputStream ), query );
                }
            };
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
            final Stream<EntityReference> query = entityFinder.findEntities( EntityComposite.class, null, null, null, null, Collections.emptyMap() );

            WriterRepresentation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
            {
                @Override
                public void write( Writer writer )
                    throws IOException
                {
                    PrintWriter out = new PrintWriter( writer );
                    out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<rdf:RDF\n"
                                 + "\txmlns=\"urn:polygene:\"\n" + "\txmlns:polygene=\"http://polygene.apache.org/rdf/model/1.0/\"\n"
                                 + "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                                 + "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" );
                    query.forEach( qualifiedIdentity -> out.println( "<polygene:entity rdf:about=\""
                                                                     + getRequest().getResourceRef().getPath() + "/"
                                                                     + qualifiedIdentity.identity() + ".rdf\"/>" ) );

                    out.println( "</rdf:RDF>" );
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
            Stream<EntityReference> query = entityFinder.findEntities( EntityComposite.class, null, null, null, null, Collections.emptyMap() );
            Representation representation = new WriterRepresentation( MediaType.TEXT_HTML )
            {
                @Override
                public void write( Writer buf )
                    throws IOException
                {
                    PrintWriter out = new PrintWriter( buf );
                    out.println( "<html><head><title>All entities</title></head><body><h1>All entities</h1><ul>" );

                    query.forEach( entity -> out.println( "<li><a href=\""
                                                          + getRequest().getResourceRef().clone().addSegment( entity.identity() + ".html" )
                                                          + "\">" + entity.identity() + "</a></li>" ) );
                    out.println( "</ul></body></html>" );
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

    private Representation representAtom()
        throws ResourceException
    {
        try
        {
            Feed feed = new Feed();
            feed.setTitle( new Text( MediaType.TEXT_PLAIN, "All entities" ) );
            List<Entry> entries = feed.getEntries();
            entityFinder.findEntities( EntityComposite.class, null, null, null, null, Collections.emptyMap() )
                        .forEach(
                            entityReference ->
                            {
                                Entry entry = new Entry();
                                entry.setTitle( new Text( MediaType.TEXT_PLAIN, entityReference.toString() ) );
                                Link link = new Link();
                                link.setHref( getRequest().getResourceRef().clone()
                                                          .addSegment( entityReference.identity().toString() ) );
                                entry.getLinks().add( link );
                                entries.add( entry );
                            } );
            return feed;
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }
    }

    @Override
    protected Representation post( Representation entity, Variant variant )
        throws ResourceException
    {
        try
        {
            /*
             * InputStream in = entity.getStream(); ObjectInputStream oin = new ObjectInputStream( in ); String reference
             * = oin.readUTF(); Usecase usecase = (Usecase) oin.readUnshared(); MetaInfo unitofwork = (MetaInfo)
             * oin.readUnshared(); Iterable<UnitOfWorkEvent> events = (Iterable<UnitOfWorkEvent>) oin.readUnshared();
             *
             * // Store state try { entityStore.apply( reference, events, usecase, unitofwork ).commit(); } catch(
             * ConcurrentEntityStateModificationException e ) { throw new ResourceException(
             * Status.CLIENT_ERROR_CONFLICT ); }
             */
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }

        return new EmptyRepresentation();
    }
}