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
package org.qi4j.library.scheduler.schedule;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

import org.qi4j.library.scheduler.constraints.CronExpression;

/**
 * Represent the scheduling of a Task.
 */
public interface Schedule
{

    /**
     * @return  True if the associated Task is currently running, false otherwise
     */
    boolean isTaskRunning();

    /**
     * @return The cron expression that will be used on UoW completion to compute next run
     */
    @Queryable( false )
    @CronExpression
    Property<String> cronExpression();

    /**
     * Denote the Schedule durability.
     *
     * On shutdown and on startup, non durable Schedules are pruned.
     * Non durable Schedules with a cron expression with no next run are pruned by SchedulerGarbageCollector.
     *
     * @return True if this Schedule will survice a JVM restart, false otherwise
     */
    @UseDefaults
    Property<Boolean> durable();

}
