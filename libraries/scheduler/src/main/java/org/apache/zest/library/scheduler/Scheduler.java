/*
 * Copyright (c) 2010-2012, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.scheduler;

import org.joda.time.DateTime;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.library.scheduler.bootstrap.SchedulerAssembler;
import org.apache.zest.library.scheduler.schedule.Schedule;
import org.apache.zest.library.scheduler.schedule.cron.CronExpression;
import org.apache.zest.library.scheduler.timeline.Timeline;

import static org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.MANDATORY;

/**
 * Scheduler.
 * <p>
 * This is the only interface you should use in your application for scheduling tasks.
 * </p>
 * <p>
 * See {@link SchedulerConfiguration} for configuration properties.
 * </p>
 * <p>
 * See in {@link SchedulerAssembler} how to assemble a {@link Scheduler} and optional {@link Timeline}.
 * </p>
 * <p>
 * By default, a {@link Schedule} is not durable. In other words, it do not survive an {@link Application} restart.
 * </p>
 * <p>
 * All {@link Schedule}s are durable and stored in the visible {@link org.apache.zest.spi.entitystore.EntityStore} like
 * any ordinary {@link org.apache.zest.api.entity.EntityComposite}. There is also a {@link org.apache.zest.library.scheduler.schedule.Schedules}
 * entity composite that has Associations to all active, completed and cancelled schedules.
 * </p>
 * <p>
 *
 * </p>
 */
@Concerns( UnitOfWorkConcern.class )
public interface Scheduler
{
    /**
     * Schedule a Task to be run after a given initial delay in seconds.
     *
     * @param task                Task to be scheduled once
     * @param initialSecondsDelay Initial delay the Task will be run after, in seconds
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleOnce( Task task, int initialSecondsDelay );

    /**
     * Schedule a Task to be run after a given initial delay in seconds.
     *
     * @param task  Task to be scheduled once
     * @param runAt The future point in time when the Schedule will be run.
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleOnce( Task task, DateTime runAt );

    /**
     * Schedule a Task using a CronExpression.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression );

    /**
     * Schedule a Task using a CronExpression with a given initial delay in milliseconds.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     * @param initialDelay   Initial delay the Schedule will be active after, in milliseconds
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression, long initialDelay );

    /**
     * Schedule a Task using a CronExpression starting at a given date.
     *
     * @param task           Task to be scheduled once
     * @param cronExpression CronExpression for creating the Schedule for the given Task
     * @param start          Date from which the Schedule will become active
     *
     * @return The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleCron( Task task, @CronExpression String cronExpression, DateTime start );

    /** Schedules a custom Schedule.
     *
     *
     * @param schedule The Schedule instance to be scheduled.
     */
    @UnitOfWorkPropagation( MANDATORY )
    void scheduleCron( Schedule schedule );

    /** Cancels a Schedule.
     * Reads the Schedule from the EntityStore and calls {@link #cancelSchedule(Schedule)}.
     *
     * @param scheduleId The identity of the Schedule to be cancelled.
     */
    @UnitOfWorkPropagation( MANDATORY )
    void cancelSchedule( String scheduleId );

    /** Cancels the provided Schedule.
     *
     * Cancellation can be done before, while and after execution of the Schedule. If the execution
     * is in progress, it will not be interrupted.
     *
     * @param schedule The schedule to be cancelled.
     */
    @UnitOfWorkPropagation( MANDATORY )
    public void cancelSchedule( Schedule schedule );
}