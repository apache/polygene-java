/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest.events;

import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Content;
import org.restlet.representation.StringRepresentation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.spi.entity.UnitOfWorkEventFeed;
import org.qi4j.spi.entity.helpers.UnitOfWorkEventsEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Atom feed for UnitOfWork events
 */
public class EventsFeed
    extends Feed
{
    @Service UnitOfWorkEventFeed eventsFeed;
    private String startId;
    private int count;

    public void setEventQuery(String startId, int count)
    {
        this.startId = startId;
        this.count = count;
    }

    @Override public List<Entry> getEntries()
    {
        ArrayList<Entry> entries = new ArrayList<Entry>( );

        Iterable<UnitOfWorkEventsEntry> events = eventsFeed.getUnitOfWorkEvents( startId, count, Usecase.DEFAULT, new MetaInfo() );
        for( UnitOfWorkEventsEntry event : events )
        {
            Entry entry = new Entry();
            entry.setId( event.identity() );
            entry.setModificationDate( new Date(event.timeStamp()) );
            Content content = new Content();
            content.setInlineContent( new StringRepresentation( event.toString()) );
            entry.setContent( content );
            entries.add( entry );
        }

        return entries;
    }
}
