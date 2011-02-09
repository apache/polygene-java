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
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;

import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.constraints.CronExpression;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.task.Task;

/**
 * Represent the scheduling of a {@link Task}.
 */
public interface Schedule
{

    /**
     * @return Identity of the {@link Scheduler} used to create this Schedule, immutable.
     */
    @Immutable
    Property<String> schedulerIdentity();

    /**
     * @return  True if the associated {@link Task} is currently running, false otherwise
     */
    boolean isTaskRunning();

    /**
     * @return The cron expression that will be used on {@link UnitOfWork} completion to compute next run
     */
    @Queryable( false )
    @CronExpression
    Property<String> cronExpression();

    /**
     * Denote the Schedule durability.
     *
     * On shutdown and on startup, non durable Schedules are pruned.
     * Non durable Schedules with a cron expression with no next run are pruned by {@link SchedulerGarbageCollector}
     *
     * @return True if this Schedule will survice a Qi4j {@link Application} restart, false otherwise
     */
    @UseDefaults
    Property<Boolean> durable();

}
