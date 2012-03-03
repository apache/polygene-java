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
package org.qi4j.library.scheduler;

import org.joda.time.DateTime;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.schedule.cron.CronExpression;
import org.qi4j.library.scheduler.timeline.Timeline;

import static org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.MANDATORY;

/**
 * Scheduler.
 *
 * This is the only interface you should use in your application for scheduling tasks.
 *
 * See {@link SchedulerConfiguration} for configuration properties.
 * See in {@link SchedulerAssembler} how to assemble a {@link Scheduler} and optional {@link Timeline}.
 *
 * By default, a {@link Schedule} is not durable. In other words, it do not survive an {@link Application} restart.
 * To make a {@link Schedule} durable, set it's durable property to true once its scheduled.
 * Durable {@link Schedule}s that have no future run are removed by {@link SchedulerGarbageCollector}.
 */
@Concerns( UnitOfWorkConcern.class )
public interface Scheduler
{

    /**
     * Schedule a Task to be run after a given initial delay in seconds.
     *
     * @param task                Task to be scheduled once
     * @param initialSecondsDelay Initial delay the Task will be run after, in seconds
     * @param durable             true if this Schedule should survive a restart.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleOnce( Task task, int initialSecondsDelay, boolean durable );

    /**
     * Schedule a Task to be run after a given initial delay in seconds.
     *
     * @param task    Task to be scheduled once
     * @param runAt   The future point in time when the Schedule will be run.
     * @param durable true if this Schedule should survive a restart.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleOnce( Task task, DateTime runAt, boolean durable );

    /**
     * Schedule a Task using a CronExpression.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     * @param durable        true if this Schedule should survive a restart.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression, boolean durable );

    /**
     * Schedule a Task using a CronExpression with a given initial delay in milliseconds.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     * @param initialDelay   Initial delay the Schedule will be active after, in milliseconds
     * @param durable        true if this Schedule should survive a restart.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression, long initialDelay, boolean durable );

    /**
     * Schedule a Task using a CronExpression starting at a given date.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     * @param start          Date from which the Schedule will become active
     * @param durable        true if this Schedule should survive a restart.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression, DateTime start, boolean durable );
}
