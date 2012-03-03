/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.scheduler.timeline;

import java.util.SortedSet;
import java.util.TreeSet;
import org.joda.time.DateTime;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.functional.Iterables;
import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.SchedulerMixin;
import org.qi4j.library.scheduler.SchedulerService;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.schedule.Schedules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WARN TimelineService Mixin use SortedSets to keep records ordered and repeatedly search for the next run. Could be greedy with large intervals
 */
public abstract class TimelineSchedulerServiceMixin
    implements Timeline, ServiceComposite
{
    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );

    @Structure
    private Module module;

    @Service
    private SchedulerService scheduler;

    @Override
    public Iterable<TimelineRecord> getLastRecords( int maxResults )
    {
        SortedSet<TimelineRecord> result = new TreeSet<TimelineRecord>();

        UnitOfWork uow = module.currentUnitOfWork();
        String schedulesName = SchedulerMixin.getSchedulesIdentity( scheduler );
        Schedules schedules = uow.get( Schedules.class, schedulesName );
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
        SortedSet<TimelineRecord> result = new TreeSet<TimelineRecord>();
        UnitOfWork uow = module.currentUnitOfWork();
        String schedulesName = SchedulerMixin.getSchedulesIdentity( scheduler );
        Schedules schedules = uow.get( Schedules.class, schedulesName );
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getNextRecords( maxResults );
            Iterables.addAll( result, lastRecords );
        }
        return Iterables.limit( maxResults, result );
    }

    @Override
    public Iterable<TimelineRecord> getRecords( DateTime from, DateTime to )
    {
        SortedSet<TimelineRecord> result = new TreeSet<TimelineRecord>();

        UnitOfWork uow = module.currentUnitOfWork();
        String schedulesName = SchedulerMixin.getSchedulesIdentity( scheduler );
        Schedules schedules = uow.get( Schedules.class, schedulesName );
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getRecords( from, to );
            Iterables.addAll( result, lastRecords );
        }
        return result;
    }

    @Override
    public Iterable<TimelineRecord> getRecords( long from, long to )
    {
        SortedSet<TimelineRecord> result = new TreeSet<TimelineRecord>();

        UnitOfWork uow = module.currentUnitOfWork();
        String schedulesName = SchedulerMixin.getSchedulesIdentity( scheduler );
        Schedules schedules = uow.get( Schedules.class, schedulesName );
        for( Schedule schedule : schedules.schedules() )
        {
            Timeline timeline = (Timeline) schedule;
            Iterable<TimelineRecord> lastRecords = timeline.getRecords( from, to );
            Iterables.addAll( result, lastRecords );
        }
        return result;
    }
}
