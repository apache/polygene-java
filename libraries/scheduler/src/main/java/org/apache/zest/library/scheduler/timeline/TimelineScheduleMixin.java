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
package org.apache.zest.library.scheduler.timeline;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.library.scheduler.Schedule;

public class TimelineScheduleMixin
    implements Timeline
{
    @Structure
    private Module module;

    @This
    private TimelineScheduleState state;

    @This
    private Schedule me;

    @Override
    public Iterable<TimelineRecord> getLastRecords( int maxResults )
    {
        List<TimelineRecord> timelineRecords = state.history().get();
        int size = timelineRecords.size();
        if( size < maxResults )
        {
            return Collections.unmodifiableCollection( timelineRecords );
        }
        SortedSet<TimelineRecord> result = new TreeSet<>();
        for( int i = size - maxResults; i < size; i++ )
        {
            result.add( timelineRecords.get( i ) );
        }
        return result;
    }

    @Override
    public Iterable<TimelineRecord> getNextRecords( int maxResults )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();
        Instant time = Instant.now();
        for( int i = 0; i < maxResults; i++ )
        {
            time = me.nextRun( time );
            result.add( createFutureRecord( time ) );
        }
        return result;
    }

    @Override
    public Iterable<TimelineRecord> getRecords( ZonedDateTime from, ZonedDateTime to )
    {
        return getRecords( from.toInstant(), to.toInstant() );
    }

    @Override
    public Iterable<TimelineRecord> getRecords( Instant from, Instant to )
    {
        Instant now = Instant.now();
        SortedSet<TimelineRecord> result = new TreeSet<>();
        result.addAll( getPastRecords( from ) );
        result.addAll( getFutureRecords( now, to ) );
        return result;
    }

    private Collection<? extends TimelineRecord> getPastRecords( Instant from )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();
        List<TimelineRecord> timelineRecords = state.history().get();
        for( TimelineRecord record : timelineRecords )
        {
            Instant timestamp = record.timestamp().get();
            if( timestamp.isAfter( from ) )
            {
                result.add( record );
            }
        }
        return result;
    }

    private Collection<? extends TimelineRecord> getFutureRecords( Instant now, Instant to )
    {
        if( now.isAfter( to ) )
        {
            return Collections.emptyList();
        }

        SortedSet<TimelineRecord> result = new TreeSet<>();
        Instant time = Instant.now();
        while( time.isBefore(to) )
        {
            time = me.nextRun( time );
            if( time.isBefore( to ) )
            {
                result.add( createFutureRecord( time ) );
            }
        }
        return result;
    }

    private TimelineRecord createFutureRecord( Instant when )
    {
        ValueBuilder<TimelineRecord> builder = module.newValueBuilder( TimelineRecord.class );
        TimelineRecord prototype = builder.prototype();
        prototype.step().set( TimelineRecordStep.FUTURE );
        prototype.taskName().set( me.task().get().name().get() );
        List<String> tags = me.task().get().tags().get();
        prototype.taskTags().set( tags );
        prototype.timestamp().set( when );
        prototype.scheduleIdentity().set( me.identity().get() );
        prototype.details().set( "" );
        return builder.newInstance();
    }
}
