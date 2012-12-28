/*
 * Copyright (c) 2010-2012, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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

import org.joda.time.DateTime;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;

/**
 * Timeline allow to browse in past and future Task runs.
 */
// START SNIPPET: timeline
public interface Timeline
{
// END SNIPPET: timeline

    /**
     * @param maxResults Maximum number of TimelineRecord to compute
     *
     * @return Last past records
     */
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
// START SNIPPET: timeline
    Iterable<TimelineRecord> getLastRecords( int maxResults );
// END SNIPPET: timeline

    /**
     * @param maxResults Maximum number of TimelineRecord to compute
     *
     * @return Next running or future records
     */
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
// START SNIPPET: timeline
    Iterable<TimelineRecord> getNextRecords( int maxResults );
// END SNIPPET: timeline

    /**
     * @param from Lower limit
     * @param to   Upper limit
     *
     * @return Records between the given dates
     */
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
// START SNIPPET: timeline
    Iterable<TimelineRecord> getRecords( DateTime from, DateTime to );
// END SNIPPET: timeline

    /**
     * @param from Lower limit
     * @param to   Upper limit
     *
     * @return Records between the given dates
     */
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
// START SNIPPET: timeline
    Iterable<TimelineRecord> getRecords( long from, long to );
}
// END SNIPPET: timeline
