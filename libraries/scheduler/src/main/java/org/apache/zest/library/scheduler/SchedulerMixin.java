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

import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.library.scheduler.schedule.Schedule;
import org.apache.zest.library.scheduler.schedule.ScheduleFactory;
import org.apache.zest.library.scheduler.schedule.Schedules;
import org.apache.zest.library.scheduler.schedule.cron.CronExpression;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerMixin
    implements Scheduler, ServiceActivation
{
    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );

    @Service
    private ScheduleFactory scheduleFactory;

    @Structure
    private Module module;

    @This
    private SchedulerService me;

    @This
    private SchedulesHandler schedulesHandler;

    @This
    private Execution execution;

    @This
    private Configuration<SchedulerConfiguration> config;

    public SchedulerMixin()
    {
    }

    @Override
    public Schedule scheduleOnce( Task task, int initialSecondsDelay )
    {
        long now = System.currentTimeMillis();
        Schedule schedule = scheduleFactory.newOnceSchedule( task, new DateTime( now + initialSecondsDelay * 1000 ) );
        saveAndDispatch( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleOnce( Task task, DateTime runAt )
    {
        Schedule schedule = scheduleFactory.newOnceSchedule( task, runAt );
        saveAndDispatch( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleCron( Task task, String cronExpression )
    {
        DateTime now = new DateTime();
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, now );
        saveAndDispatch( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleCron( Task task, @CronExpression String cronExpression, DateTime start )
    {
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, start );
        saveAndDispatch( schedule );
        return schedule;
    }

    @Override
    public void scheduleCron( Schedule schedule )
    {
        saveAndDispatch( schedule );
    }

    @Override
    public Schedule scheduleCron( Task task, String cronExpression, long initialDelay )
    {
        DateTime start = new DateTime( System.currentTimeMillis() + initialDelay );
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, start );
        saveAndDispatch( schedule );
        return schedule;
    }

    @Override
    public void cancelSchedule( String scheduleId )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        Schedule schedule = null;
        try
        {
            schedule = uow.get( Schedule.class, scheduleId );
        }
        catch( NoSuchEntityException e )
        {
            return;
        }
        cancelSchedule( schedule );
    }

    @Override
    public void cancelSchedule( Schedule schedule )
    {
        Schedules active = schedulesHandler.getActiveSchedules();
        if( active.schedules().remove( schedule ) )
        {
            schedule.cancelled().set( true );
        }
    }

    private void saveAndDispatch( Schedule schedule )
    {
        Schedules schedules = schedulesHandler.getActiveSchedules();
        schedules.schedules().add( schedule );
        execution.dispatchForExecution( schedule );
    }

    private void loadSchedules()
        throws UnitOfWorkCompletionException
    {
        try (UnitOfWork ignored = module.newUnitOfWork( UsecaseBuilder.newUsecase( "Initialize Schedules" ) ))
        {
            Schedules schedules = schedulesHandler.getActiveSchedules();
            for( Schedule schedule : schedules.schedules() )
            {
                if( schedule.cancelled().get() || schedule.done().get() )
                {
                    schedules.schedules().remove( schedule );
                }
                else
                {
                    execution.dispatchForExecution( schedule );
                }
            }
        }
    }

    @Override
    public void activateService()
        throws Exception
    {
        // Throws IllegalArgument if corePoolSize or keepAliveTime less than zero,
        // or if workersCount less than or equal to zero,
        // or if corePoolSize greater than workersCount.
        execution.start();
        loadSchedules();
        LOGGER.debug( "Activated" );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        execution.stop();
        LOGGER.debug( "Passivated" );
    }
}
