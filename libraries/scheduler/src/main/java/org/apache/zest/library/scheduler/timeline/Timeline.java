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
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;

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
    Iterable<TimelineRecord> getRecords( ZonedDateTime from, ZonedDateTime to );
// END SNIPPET: timeline

    /**
     * @param from Lower limit
     * @param to   Upper limit
     *
     * @return Records between the given dates
     */
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
// START SNIPPET: timeline
    Iterable<TimelineRecord> getRecords( Instant from, Instant to );
}
// END SNIPPET: timeline
