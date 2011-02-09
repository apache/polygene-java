/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.scheduler.timeline;

import static org.qi4j.api.query.QueryExpressions.*;
import static org.qi4j.api.query.grammar.OrderBy.Order.*;

import static org.qi4j.library.scheduler.timeline.TimelineRecordStep.FUTURE;
import static org.qi4j.library.scheduler.timeline.TimelineRecordStep.RUNNING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.SchedulerService;
import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.task.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( TimelineService.Mixin.class )
public interface TimelineService
        extends Timeline,
                ServiceComposite
{

    /**
     * WARN TimelineService Mixin use SortedSets to keep records ordered and repeatedly search for the next run. Could be greedy with large intervals
     */
    abstract class Mixin
            implements Timeline
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private ValueBuilderFactory vbf;
        @Structure
        private QueryBuilderFactory qbf;
        @Service
        private SchedulerService scheduler;

        public Iterable<TimelineRecord> getLastRecords( int maxResults )
        {
            QueryBuilder<TimelineRecord> builder = qbf.newQueryBuilder( TimelineRecord.class );
            TimelineRecord template = templateFor( TimelineRecord.class );
            builder = builder.where( eqSchedulerIdentity( template ) );
            return builder.newQuery( uowf.currentUnitOfWork() ).
                    orderBy( orderBy( template.timestamp(), DESCENDING ) ).
                    maxResults( maxResults );
        }

        public Iterable<TimelineRecord> getNextRecords( int maxResults )
        {
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            OrderBy orderByNextRun = orderBy( template.nextRun() );

            QueryBuilder<ScheduleEntity> queryBuilder = qbf.newQueryBuilder( ScheduleEntity.class );
            queryBuilder = queryBuilder.where( and( eqSchedulerIdentity( template ),
                                                    ge( template.nextRun(), System.currentTimeMillis() ) ) );
            Query<ScheduleEntity> query = queryBuilder.newQuery( uowf.currentUnitOfWork() );
            query = query.orderBy( orderByNextRun ).maxResults( maxResults );

            if ( query.count() == 0 ) {
                return Collections.emptySet();
            }

            List<ScheduleEntity> queryAsList = new ArrayList<ScheduleEntity>();
            SortedSet<TimelineRecord> futureRuns = new TreeSet<TimelineRecord>();

            for ( ScheduleEntity eachSchedule : query ) {
                TimelineRecord record = buildRecordValue( eachSchedule, eachSchedule.nextRun().get() );
                queryAsList.add( eachSchedule );
                futureRuns.add( record );
            }

            boolean alreadyFilled = futureRuns.size() == maxResults;
            long alreadyFilledMax = futureRuns.last().timestamp().get();

            for ( ScheduleEntity eachSchedule : queryAsList ) {
                Long nextRun = eachSchedule.firstRunAfter( eachSchedule.nextRun().get() );
                while ( nextRun != null && ( ( alreadyFilled && nextRun < alreadyFilledMax ) || futureRuns.size() < maxResults ) ) {
                    TimelineRecord record = buildRecordValue( eachSchedule, nextRun );
                    futureRuns.add( record );
                    nextRun = eachSchedule.firstRunAfter( nextRun );
                }
            }

            // Build final result in a list so that the order is kept
            List<TimelineRecord> result = new ArrayList<TimelineRecord>();
            Iterator<TimelineRecord> it = futureRuns.iterator();
            for ( int idx = 0; idx < maxResults; idx++ ) {
                if ( !it.hasNext() ) {
                    break;
                }
                result.add( it.next() );
            }
            return result;
        }

        public Iterable<TimelineRecord> getRecords( Date from, Date to )
        {
            return getRecords( from.getTime(), to.getTime() );
        }

        public Iterable<TimelineRecord> getRecords( long from, long to )
        {
            if ( from > to ) {
                throw new IllegalArgumentException( "from (" + from + ") cannot be greater than to (" + to + ")" );
            }
            long now = System.currentTimeMillis();
            if ( from > now ) {
                // Future runs only
                LOGGER.trace( "TimelineService.getRecords( {}, {} ) Future runs only", from, to );
                return future( from, to );
            }
            QueryBuilder<TimelineRecord> builder = qbf.newQueryBuilder( TimelineRecord.class );
            TimelineRecord template = templateFor( TimelineRecord.class );
            builder = builder.where( and( eqSchedulerIdentity( template ),
                                          ge( template.timestamp(), from ),
                                          le( template.timestamp(), to ) ) );
            OrderBy order = orderBy( template.timestamp() );
            Iterable<TimelineRecord> pastRecords = builder.newQuery( uowf.currentUnitOfWork() ).orderBy( order );

            if ( to >= now ) {
                // Mixed past records and future runs
                LOGGER.trace( "TimelineService.getRecords( {}, {} ) Mixed past records and future runs", from, to );
                return Iterables.flatten( pastRecords, future( now, to ) );
            } else {
                LOGGER.trace( "TimelineService.getRecords( {}, {} ) Past records only", from, to );
            }

            return pastRecords;
        }

        private Iterable<TimelineRecord> future( long from, long to )
        {
            QueryBuilder<ScheduleEntity> queryBuilder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            queryBuilder = queryBuilder.where( and( eqSchedulerIdentity( template ),
                                                    ge( template.nextRun(), from ),
                                                    le( template.nextRun(), to ) ) );
            OrderBy order = orderBy( template.nextRun() );
            Query<ScheduleEntity> query = queryBuilder.newQuery( uowf.currentUnitOfWork() ).orderBy( order );

            SortedSet<TimelineRecord> futureRuns = new TreeSet<TimelineRecord>();
            for ( ScheduleEntity eachSchedule : query ) {

                Long nextRun = eachSchedule.nextRun().get();
                while ( nextRun <= to ) {

                    futureRuns.add( buildRecordValue( eachSchedule, nextRun ) );

                    nextRun = eachSchedule.firstRunAfter( nextRun );
                }

            }

            return futureRuns;
        }

        private TimelineRecord buildRecordValue( ScheduleEntity schedule, long timestamp )
        {
            ValueBuilder<TimelineRecordValue> recordBuilder = vbf.newValueBuilder( TimelineRecordValue.class );
            TimelineRecordValue record = recordBuilder.prototype();

            record.schedulerIdentity().set( scheduler.identity().get() );
            record.timestamp().set( timestamp );
            record.step().set( schedule.running().get() ? RUNNING : FUTURE );

            Task task = schedule.task().get();
            record.taskName().set( task.name().get() );
            record.taskTags().set( task.tags().get() );

            StringBuilder details = new StringBuilder();
            details.append( schedule.durable().get() ? "Durable " : "Non durable " ).
                    append( "Schedule " ).
                    append( schedule.cronExpression().get() );
            record.details().set( details.toString() );

            return recordBuilder.newInstance();
        }

        private EqualsPredicate<String> eqSchedulerIdentity( ScheduleEntity template )
        {
            return eq( template.schedulerIdentity(), scheduler.identity().get() );
        }

        private EqualsPredicate<String> eqSchedulerIdentity( TimelineRecord template )
        {
            return eq( template.schedulerIdentity(), scheduler.identity().get() );
        }

    }

}
