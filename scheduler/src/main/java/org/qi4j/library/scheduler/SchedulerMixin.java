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

import java.util.Date;

import org.qi4j.api.injection.scope.Service;

import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.schedule.ScheduleFactory;
import org.qi4j.library.scheduler.task.Task;

/**
 * @author Paul Merlin
 */
public class SchedulerMixin
        implements Scheduler
{

    @Service
    private ScheduleFactory scheduleFactory;

    public Schedule shedule( Task task, String cronExpression )
    {
        return scheduleFactory.newSchedule( task, cronExpression, System.currentTimeMillis() );
    }

    public Schedule shedule( Task task, String cronExpression, long initialDelay )
    {
        return scheduleFactory.newSchedule( task, cronExpression, System.currentTimeMillis() + initialDelay );
    }

    public Schedule shedule( Task task, String cronExpression, Date start )
    {
        return scheduleFactory.newSchedule( task, cronExpression, start.getTime() );
    }

}
