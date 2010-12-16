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

import java.util.Date;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.grammar.OrderBy.Order;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * @author Paul Merlin
 */
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

        public Iterable<TimelineRecord> getRecords( long from, long to )
        {
            // TODO Mix past and future if needed
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Iterable<TimelineRecord> getRecords( Date from, Date to )
        {
            return getRecords( from.getTime(), to.getTime() );
        }

    }

}
