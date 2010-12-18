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

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.OrderBy.Order;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.task.Task;

@Mixins( TimelineService.Mixin.class )
public interface TimelineService
        extends Timeline, ServiceComposite
{

    abstract class Mixin
            implements Timeline
    {

        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private ValueBuilderFactory vbf;
        @Structure
        private QueryBuilderFactory qbf;

        public Iterable<TimelineRecord> getLastRecords( int maxResults )
        {
            QueryBuilder<TimelineRecord> builder = qbf.newQueryBuilder( TimelineRecord.class );
            return builder.newQuery( uowf.currentUnitOfWork() ).
                    orderBy( orderBy( templateFor( TimelineRecord.class ).timestamp(), Order.ASCENDING ) ).
                    maxResults( maxResults );
        }

        public Iterable<TimelineRecord> getNextRecords( int maxResults )
        {
            // TODO Compute from runnings + cron expression resolution
            throw new UnsupportedOperationException( "Not supported yet." );
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
                return future( from, to );
            }
            QueryBuilder<TimelineRecord> builder = qbf.newQueryBuilder( TimelineRecord.class );
            TimelineRecord template = templateFor( TimelineRecord.class );
            builder = builder.where( and( ge( template.timestamp(), from ),
                                          le( template.timestamp(), to ) ) );
            OrderBy order = orderBy( template.timestamp() );
            Iterable<TimelineRecord> pastRecords = builder.newQuery( uowf.currentUnitOfWork() ).orderBy( order );

            if ( to >= now ) {
                // Mixed past records and future runs
                return Iterables.flatten( pastRecords, future( now, to ) );
            }

            return pastRecords;
        }

        /**
         * WARN Using a SortedSet to keep records ordered and repeatedly searching for the next run could be greedy with large intervals
         */
        private Iterable<TimelineRecord> future( long from, long to )
        {
            QueryBuilder<ScheduleEntity> queryBuilder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            queryBuilder = queryBuilder.where( and( ge( template.nextRun(), from ),
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

            record.timestamp().set( timestamp );
            record.event().set( SchedulerEvent.TASK_RUN_FUTURE );

            Task task = schedule.task().get();
            record.taskName().set( task.name().get() );
            record.taskTags().set( task.tags().get() );

            StringBuilder details = new StringBuilder();
            details.append( schedule.durable().get() ? "Durable " : "Non durable " ).
                    append( "Schedule with cron expression: " ).
                    append( schedule.cronExpression().get() );
            record.details().set( details.toString() );

            return recordBuilder.newInstance();
        }

    }

}
