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
package org.qi4j.library.scheduler;

import static org.qi4j.api.unitofwork.UnitOfWorkPropagation.Propagation.MANDATORY;

import java.util.Date;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.UnitOfWorkPropagation;

import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.qi4j.library.scheduler.constraints.CronExpression;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.task.Task;
import org.qi4j.library.scheduler.timeline.Timeline;

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
     * @param task                  Task to be scheduled once
     * @param initialSecondsDelay   Initial delay the Task will be run after, in seconds
     * @return                      The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule scheduleOnce( Task task, int initialSecondsDelay );

    /**
     * Schedule a Task using a CronExpression.
     * 
     * @param task                  Task to be scheduled once
     * @param cronExpression        CronExpression for creating the Schedule for the given Task
     * @return                      The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule schedule( Task task, @CronExpression String cronExpression );

    /**
     * Schedule a Task using a CronExpression with a given initial delay in milliseconds.
     *
     * @param task                  Task to be scheduled once
     * @param cronExpression        CronExpression for creating the Schedule for the given Task
     * @param initialDelay          Initial delay the Schedule will be active after, in milliseconds
     * @return                      The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule schedule( Task task, @CronExpression String cronExpression, long initialDelay );

    /**
     * Schedule a Task using a CronExpression starting at a given date.
     *
     * @param task                  Task to be scheduled once
     * @param cronExpression        CronExpression for creating the Schedule for the given Task
     * @param start                 Date from which the Schedule will become active
     * @return                      The newly created Schedule
     */
    @UnitOfWorkPropagation( MANDATORY )
    Schedule schedule( Task task, @CronExpression String cronExpression, Date start );

}
