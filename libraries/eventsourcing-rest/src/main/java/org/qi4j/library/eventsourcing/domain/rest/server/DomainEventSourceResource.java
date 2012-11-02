/**
 *
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.rest.server;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.functional.Iterables;
import org.qi4j.io.Outputs;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.atom.*;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Get events in various formats. The feed is paged, with one
 * current set page, one working set page, and the rest being archive pages that never change. The links "next", "previous",
 * "first" and "last" are used as expected per the Atom spec.
 * <p/>
 * / = current set of most recent events (event range: count-pagesize to count)
 * /n,m = events from index n to index m. These are archive pages.
 * /n = working set page, where n is the first event index to be presented
 */
public class DomainEventSourceResource
        extends Restlet
{
    EventSource source;

    public DomainEventSourceResource( @Service @Tagged("domain") EventSource source )
    {
        this.source = source;
    }

    @Override
    public void handle( Request request, Response response )
    {
        long eventCount = source.count();
        long pageSize = 10;
        long startEvent = -1;
        long endEvent = -1;
        long limit = pageSize;

        final List<UnitOfWorkDomainEventsValue> eventsValues = new ArrayList<UnitOfWorkDomainEventsValue>();

        final Feed feed = new Feed();
        feed.setBaseReference( request.getResourceRef().getParentRef() );
        List<Link> links = feed.getLinks();

        String remainingPart = request.getResourceRef().getRemainingPart();
        if (remainingPart.equals( "/" ))
        {
            // Current set - always contains the last "pageSize" events
            startEvent = Math.max( 0, eventCount - pageSize - 1 );

            feed.setTitle( new Text( "Current set" ) );

            if (startEvent > 0)
            {
                long previousStart = Math.max(0, startEvent-pageSize);
                long previousEnd = startEvent-1;

                Link link = new Link( new Reference( previousStart+","+previousEnd ), new Relation( "previous" ), MediaType.APPLICATION_ATOM );
                link.setTitle( "Previous page" );
                links.add( link );
            }

        } else
        {
            // Archive
            String[] indices = remainingPart.substring(1).split( "," );

            if (indices.length == 1)
            {
                // Working set
                startEvent = Long.parseLong( indices[0] );
                endEvent = startEvent + pageSize - 1;
                limit = pageSize;
                feed.setTitle( new Text("Working set") );
            } else if (indices.length == 2)
            {
                feed.setTitle( new Text("Archive page") );
                startEvent = Long.parseLong( indices[0] );
                endEvent = Long.parseLong( indices[1] );
                limit = 1+endEvent-startEvent;

            } else
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

            if (startEvent > 0)
            {
                long previousStart = Math.max(0, startEvent-pageSize);
                long previousEnd = startEvent-1;

                Link link = new Link( new Reference( previousStart+","+previousEnd ), new Relation( "previous" ), MediaType.APPLICATION_ATOM );
                link.setTitle( "Previous page" );
                links.add( link );
            }

            long nextStart = endEvent+1;
            long nextEnd = nextStart+pageSize-1;

            if (nextStart < eventCount)
                if (nextEnd >= eventCount)
                {
                    Link next = new Link( new Reference( nextStart+"" ), new Relation( "next" ), MediaType.APPLICATION_ATOM );
                    next.setTitle( "Working set" );
                    links.add( next );
                } else
                {
                    Link next = new Link( new Reference( nextStart+","+nextEnd ), new Relation( "next" ), MediaType.APPLICATION_ATOM );
                    next.setTitle( "Next page" );
                    links.add( next );
                }
        }

        try
        {
            source.events( startEvent, limit ).transferTo( Outputs.collection( eventsValues ) );
        } catch (Throwable throwable)
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, throwable );
        }

        Link last = new Link( new Reference( "0,"+(pageSize-1) ), new Relation( "last" ), MediaType.APPLICATION_ATOM );
        last.setTitle( "Last archive page" );
        links.add( last );

        Link first = new Link( new Reference( "." ), new Relation( "first" ), MediaType.APPLICATION_ATOM );
        first.setTitle( "Current set" );
        links.add( first );

/*
        if (previousPage != -1)
        {
            Link link = new Link( new Reference( ""+previousPage ), new Relation( "prev-archive" ), MediaType.APPLICATION_ATOM );
            link.setTitle( "Previous archive page" );
            links.add( link );
        }
        if (nextPage != -1)
        {
            Link link = new Link( new Reference( "" + nextPage ), new Relation( "next-archive" ), MediaType.APPLICATION_ATOM );
            link.setTitle( "Next archive page" );
            links.add( link );
        }
        else if (startEvent != workingSetOffset)
        {
            Link next = new Link( new Reference( "" ), new Relation( "next" ), MediaType.APPLICATION_ATOM );
            next.setTitle( "Next page" );
            links.add( next );
        }
*/

        Date lastModified = null;
        for (UnitOfWorkDomainEventsValue eventsValue : eventsValues)
        {
            Entry entry = new Entry();
            entry.setTitle( new Text( eventsValue.usecase().get() + "(" + eventsValue.user().get() + ")" ) );
            entry.setPublished( new Date( eventsValue.timestamp().get() ) );
            entry.setModificationDate( lastModified = new Date( eventsValue.timestamp().get() ) );
            entry.setId( Long.toString( startEvent + 1 ) );
            startEvent++;
            Content content = new Content();
            content.setInlineContent( new StringRepresentation( eventsValue.toString(), MediaType.APPLICATION_JSON ) );
            entry.setContent( content );
            feed.getEntries().add( entry );
        }

        feed.setModificationDate( lastModified );

        MediaType mediaType = request.getClientInfo().getPreferredMediaType( Iterables.toList( iterable( MediaType.TEXT_HTML, MediaType.APPLICATION_ATOM ) ));

        if (MediaType.APPLICATION_ATOM.equals( mediaType ))
        {
            WriterRepresentation representation = new WriterRepresentation( MediaType.APPLICATION_ATOM )
            {
                @Override
                public void write( final Writer writer ) throws IOException
                {
                    feed.write( writer );
                }
            };
            representation.setCharacterSet( CharacterSet.UTF_8 );
            response.setEntity( representation );
        } else
        {
            WriterRepresentation representation = new WriterRepresentation(MediaType.TEXT_HTML)
            {
                @Override
                public void write( Writer writer ) throws IOException
                {
                    writer.append( "<html><head><title>Events</title></head><body>" );

                    for( Link link : feed.getLinks() )
                    {
                        writer.append( "<a href=\"").append( link.getHref().getPath()).append( "\">" );
                        writer.append( link.getTitle() );
                        writer.append( "</a><br/>" );
                    }

                    writer.append( "<ol>" );
                    for( Entry entry : feed.getEntries() )
                    {
                        writer.append( "<li>" ).append( entry.getTitle().toString() ).append( "</li>" );
                    }
                    writer.append( "</ol></body>" );
                }
            };
            representation.setCharacterSet( CharacterSet.UTF_8 );
            response.setEntity( representation );
        }

/*
        } else
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE );
        }
*/
    }
}
