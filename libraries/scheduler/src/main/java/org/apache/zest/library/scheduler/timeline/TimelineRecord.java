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
import java.util.List;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.library.scheduler.Scheduler;

/**
 * Record in {@link Scheduler}'s {@link Timeline}.
 *
 * {@link TimelineRecord}s are {@link Comparable} regarding their {@link TimelineRecord#timestamp()}.
 */
@Mixins( TimelineRecord.Mixin.class )
public interface TimelineRecord
    extends Comparable<TimelineRecord>, ValueComposite
{
    /**
     * @return Identity of the associated {@link Scheduler}
     */
    Property<String> scheduleIdentity();

    /**
     * @return Timestamp of this record
     */
    Property<Instant> timestamp();

    /**
     * @return Name of the associated {@link org.apache.zest.library.scheduler.Task}
     */
    Property<String> taskName();

    /**
     * @return Tags of the associated {@link org.apache.zest.library.scheduler.Task}
     */
    @UseDefaults
    Property<List<String>> taskTags();

    Property<TimelineRecordStep> step();

    /**
     * @return Details text of this record
     */
    @Queryable( false )
    @UseDefaults
    Property<String> details();

    abstract class Mixin
        implements TimelineRecord
    {

        @Override
        public int compareTo( TimelineRecord o )
        {
            return timestamp().get().compareTo( o.timestamp().get() );
        }
    }

}
