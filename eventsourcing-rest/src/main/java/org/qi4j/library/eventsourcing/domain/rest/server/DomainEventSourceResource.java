/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.service.qualifier.Tagged;
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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Get events before or after a given date in various formats. The feed is paged, with one
 * current set page, one working set page, and the rest being archive pages that never change. The links "next", "previous",
 * "first" and "last" are used as expected per the Atom spec. Page size is 10.
 * <p/>
 * / = current set of most recent events
 * /n = archive page nr n
 * /w = where w is the last page that is not yet completed
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
        long currentPage = -1;
        long previousPage = -1;
        long nextPage = -1;
        long eventCount = source.count();
        long pageSize = 100;
        long offset = 0;

        long workingSetOffset = eventCount - (eventCount % pageSize);
        if (workingSetOffset == eventCount)
            workingSetOffset -= pageSize;


        final List<UnitOfWorkDomainEventsValue> eventsValues = new ArrayList<UnitOfWorkDomainEventsValue>();

        String remainingPart = request.getResourceRef().getRemainingPart();
        if (remainingPart.equals( "/" ))
        {
            // Current set - always contains the last "pageSize" events
            offset = eventCount - pageSize;

            currentPage = eventCount / pageSize;
            previousPage = currentPage - 1;
        } else
        {


            // Archive
            currentPage = Long.parseLong( remainingPart.substring( 1 ) );
            offset = currentPage * pageSize;

            if (offset >= workingSetOffset)
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

            if (currentPage > 0)
                previousPage = currentPage - 1;

            if (offset + pageSize < workingSetOffset)
                nextPage = currentPage + 1;
        }

        try
        {
            source.events( offset, pageSize ).transferTo( Outputs.collection( eventsValues ) );
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }

        final Feed feed = new Feed();
        feed.setTitle( new Text( "Latest domain events" ) );

        List<Link> links = feed.getLinks();
        links.add( new Link( new Reference( "/0" ), new Relation( "last" ), MediaType.APPLICATION_ATOM ) );
        if (previousPage != -1)
            links.add( new Link( new Reference( "/" + previousPage ), new Relation( "prev-archive" ), MediaType.APPLICATION_ATOM ) );
        if (nextPage != -1)
            links.add( new Link( new Reference( "/" + nextPage ), new Relation( "next-archive" ), MediaType.APPLICATION_ATOM ) );
        else if (offset != workingSetOffset)
            links.add( new Link( new Reference( "/" ), new Relation( "next" ), MediaType.APPLICATION_ATOM ) );
        links.add( new Link( new Reference( "/" ), new Relation( "first" ), MediaType.APPLICATION_ATOM ) );

        for (UnitOfWorkDomainEventsValue eventsValue : eventsValues)
        {
            Entry entry = new Entry();
            entry.setTitle( new Text( eventsValue.usecase().get() + "(" + eventsValue.user().get() + ")" ) );
            entry.setPublished( new Date( eventsValue.timestamp().get() ) );
            entry.setModificationDate( new Date( eventsValue.timestamp().get() ) );
            entry.setId( Long.toString( offset + 1 ) );
            offset++;
            Content content = new Content();
            content.setInlineContent( new StringRepresentation( eventsValue.toJSON(), MediaType.APPLICATION_JSON ) );
            entry.setContent( content );
            feed.getEntries().add( entry );
        }

        WriterRepresentation representation = new WriterRepresentation( MediaType.APPLICATION_ATOM )
        {
            public void write( final Writer writer ) throws IOException
            {
                feed.write( writer );
            }
        };

        representation.setCharacterSet( CharacterSet.UTF_8 );
        response.setEntity( representation );
/*
        } else
        {
            throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE );
        }
*/
    }
}
