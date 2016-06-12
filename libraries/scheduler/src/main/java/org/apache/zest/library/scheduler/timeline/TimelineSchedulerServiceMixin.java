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
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.Module;
import org.apache.zest.functional.Iterables;
import org.apache.zest.library.scheduler.SchedulerService;
import org.apache.zest.library.scheduler.SchedulesHandler;
import org.apache.zest.library.scheduler.Schedule;
import org.apache.zest.library.scheduler.internal.Schedules;

/**
 * WARN TimelineService Mixin use SortedSets to keep records ordered and repeatedly search for the next run.
 * Could be greedy with large intervals
 */
public abstract class TimelineSchedulerServiceMixin
    implements Timeline, ServiceComposite
{
    @Structure
    private Module module;

    @This
    private SchedulerService scheduler;

    @This
    private SchedulesHandler schedulesHandler;

    @Override
    public Iterable<TimelineRecord> getLastRecords( int maxResults )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();

        Schedules schedules = schedulesHandler.getActiveSchedules();
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getLastRecords( maxResults );
            Iterables.addAll( result, lastRecords );
        }
        return Iterables.limit( maxResults, Iterables.reverse( result ) );
    }

    @Override
    public Iterable<TimelineRecord> getNextRecords( int maxResults )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();
        Schedules schedules = schedulesHandler.getActiveSchedules();
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getNextRecords( maxResults );
            Iterables.addAll( result, lastRecords );
        }
        return Iterables.limit( maxResults, result );
    }

    @Override
    public Iterable<TimelineRecord> getRecords( ZonedDateTime from, ZonedDateTime to )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();

        Schedules schedules = schedulesHandler.getActiveSchedules();
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getRecords( from, to );
            Iterables.addAll( result, lastRecords );
        }
        return result;
    }

    @Override
    public Iterable<TimelineRecord> getRecords( Instant from, Instant to )
    {
        SortedSet<TimelineRecord> result = new TreeSet<>();

        Schedules schedules = schedulesHandler.getActiveSchedules();
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getRecords( from, to );
            Iterables.addAll( result, lastRecords );
        }
        return result;
    }
}
