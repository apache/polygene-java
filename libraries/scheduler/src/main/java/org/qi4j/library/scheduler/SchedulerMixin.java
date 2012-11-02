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

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.schedule.ScheduleFactory;
import org.qi4j.library.scheduler.schedule.ScheduleTime;
import org.qi4j.library.scheduler.schedule.Schedules;
import org.qi4j.library.scheduler.schedule.cron.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerMixin
    implements Scheduler, SchedulerActivation
{
    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private static final int DEFAULT_WORKERS_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private static final int DEFAULT_WORKQUEUE_SIZE = 10;

    @Service
    private ScheduleFactory scheduleFactory;

    private final SortedSet<ScheduleTime> timingQueue = new TreeSet<ScheduleTime>();

    private ScheduledExecutorService managementExecutor;
    private ThreadPoolExecutor taskExecutor;

    @Structure
    private Module module;

    @This
    private SchedulerService me;

    @This
    private ThreadFactory threadFactory;

    @This
    private RejectedExecutionHandler rejectionHandler;

    @This
    private Configuration<SchedulerConfiguration> config;

    private ScheduleHandler scheduleHandler;

    @Override
    public Schedule scheduleOnce( Task task, int initialSecondsDelay, boolean durable )
    {
        long now = System.currentTimeMillis();
        Schedule schedule = scheduleFactory.newOnceSchedule( task, new DateTime( now + initialSecondsDelay * 1000 ), durable );
        if( durable )
        {
            Schedules schedules = module.currentUnitOfWork().get( Schedules.class, getSchedulesIdentity( me ) );
            schedules.schedules().add( schedule );
        }
        dispatchForExecution( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleOnce( Task task, DateTime runAt, boolean durable )
    {
        Schedule schedule = scheduleFactory.newOnceSchedule( task, runAt, durable );
        dispatchForExecution( schedule );
        if( durable )
        {
            Schedules schedules = module.currentUnitOfWork().get( Schedules.class, getSchedulesIdentity( me ) );
            schedules.schedules().add( schedule );
        }
        return schedule;
    }

    @Override
    public Schedule scheduleCron( Task task, String cronExpression, boolean durable )
    {
        DateTime now = new DateTime();
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, now, durable );
        if( durable )
        {
            Schedules schedules = module.currentUnitOfWork().get( Schedules.class, getSchedulesIdentity( me ) );
            schedules.schedules().add( schedule );
        }
        dispatchForExecution( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleCron( Task task, @CronExpression String cronExpression, DateTime start, boolean durable )
    {
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, start, durable );
        if( durable )
        {
            Schedules schedules = module.currentUnitOfWork().get( Schedules.class, getSchedulesIdentity( me ) );
            schedules.schedules().add( schedule );
        }
        dispatchForExecution( schedule );
        return schedule;
    }

    @Override
    public Schedule scheduleCron( Task task, String cronExpression, long initialDelay, boolean durable )
    {
        DateTime start = new DateTime( System.currentTimeMillis() + initialDelay );
        Schedule schedule = scheduleFactory.newCronSchedule( task, cronExpression, start, durable );
        if( durable )
        {
            Schedules schedules = module.currentUnitOfWork().get( Schedules.class, getSchedulesIdentity( me ) );
            schedules.schedules().add( schedule );
        }
        dispatchForExecution( schedule );
        return schedule;
    }

    private void dispatchForExecution( Schedule schedule )
    {
        long now = System.currentTimeMillis();
        synchronized( timingQueue )
        {
            if( timingQueue.size() == 0 )
            {
                long nextRun = schedule.nextRun( now );
                if( nextRun < 0 )
                {
                    return;
                }
                System.out.println( "Next run at: " + new DateTime( nextRun ) );
                timingQueue.add( new ScheduleTime( schedule.identity().get(), nextRun ) );
                if( scheduleHandler == null )
                {
                    dispatchHandler();
                }
            }
            else
            {
                ScheduleTime first = timingQueue.first();
                long nextRun = schedule.nextRun( now );
                if( nextRun < 0 )
                {
                    return;
                }
                System.out.println( "Next run at: " + new DateTime( nextRun ) );
                timingQueue.add( new ScheduleTime( schedule.identity().get(), nextRun ) );
                ScheduleTime newFirst = timingQueue.first();
                if( !first.equals( newFirst ) )
                {
                    // We need to restart the managementThread, which is currently waiting for a 'later' event to
                    // occur than the one that was just scheduled.
                    scheduleHandler.future.cancel( true );
                    dispatchHandler();
                }
            }
        }
    }

    private void dispatchHandler()
    {
        scheduleHandler = new ScheduleHandler();
        managementExecutor.schedule( scheduleHandler, timingQueue.first().nextTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS );
    }

    @Override
    public void activateScheduler()
        throws Exception
    {

        // Handle configuration defaults
        SchedulerConfiguration configuration = config.get();
        Integer workersCount = configuration.workersCount().get();
        Integer workQueueSize = configuration.workQueueSize().get();

        if( workersCount == null )
        {
            workersCount = DEFAULT_WORKERS_COUNT;
            LOGGER.debug( "Workers count absent from configuration, falled back to default: {} workers", DEFAULT_WORKERS_COUNT );
        }
        if( workQueueSize == null )
        {
            workQueueSize = DEFAULT_WORKQUEUE_SIZE;
            LOGGER.debug( "WorkQueue size absent from configuration, falled back to default: {}", DEFAULT_WORKQUEUE_SIZE );
        }

        int corePoolSize = 2;
        if( workersCount > 4 )
        {
            corePoolSize = workersCount / 4;
        }
        // Throws IllegalArgument if corePoolSize or keepAliveTime less than zero, or if workersCount less than or equal to zero, or if corePoolSize greater than workersCount.
        taskExecutor = new ThreadPoolExecutor( corePoolSize, workersCount,
                                               0, TimeUnit.MILLISECONDS,
                                               new LinkedBlockingQueue<Runnable>( workQueueSize ),
                                               threadFactory, rejectionHandler );
        taskExecutor.prestartAllCoreThreads();
        managementExecutor = new ScheduledThreadPoolExecutor( 2, threadFactory, rejectionHandler );
        loadSchedules();
        LOGGER.debug( "Activated" );
    }

    private void loadSchedules()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            Schedules schedules = uow.get( Schedules.class, getSchedulesIdentity( me ) );
            for( Schedule schedule : schedules.schedules() )
            {
                dispatchForExecution( schedule );
            }
        }
        catch( NoSuchEntityException e )
        {
            // Create a new Schedules entity for keeping track of them all.
            uow.newEntity( Schedules.class, getSchedulesIdentity( me ) );
            uow.complete();
        }
        finally
        {
            if( uow.isOpen() )
            {
                uow.discard();
            }
        }
    }

    public static String getSchedulesIdentity( SchedulerService service )
    {
        return "Schedules:" + service.identity().get();
    }

    @Override
    public void passivateScheduler()
        throws Exception
    {
        LOGGER.debug( "Passivated" );
    }

    /**
     * This little bugger wakes up when it is time to dispatch a Task, creates the Runner and dispatches itself
     * for the next run.
     */
    class ScheduleHandler
        implements Runnable
    {

        private ScheduleRunner scheduleRunner;
        private ScheduledFuture<?> future;

        @Override
        public void run()
        {
            synchronized( timingQueue )
            {
                ScheduleTime scheduleTime = timingQueue.first();
                timingQueue.remove( scheduleTime );
                scheduleRunner = new ScheduleRunner( scheduleTime, SchedulerMixin.this, module );
                taskExecutor.submit( scheduleRunner );
                if( timingQueue.size() == 0 )
                {
                    scheduleHandler = null;
                }
                else
                {
                    ScheduleTime nextTime = timingQueue.first();
                    future = managementExecutor.schedule( scheduleHandler, nextTime.nextTime, TimeUnit.MILLISECONDS );
                }
            }
        }
    }

    /**
     * Handle {@link Task}'s {@link org.qi4j.api.unitofwork.UnitOfWork} and {@link org.qi4j.library.scheduler.timeline.TimelineRecord}s creation.
     */
    public static class ScheduleRunner
        implements Runnable
    {
        private Module module;
        private ScheduleTime schedule;
        private SchedulerMixin schedulerMixin;

        public ScheduleRunner( ScheduleTime schedule, SchedulerMixin schedulerMixin, Module module )
        {
            this.schedule = schedule;
            this.schedulerMixin = schedulerMixin;
            this.module = module;
        }

        // WARN Watch this code, see if we can do better, maybe leverage @UnitOfWorkRetry
        @Override
        public void run()
        {
            System.out.println( "Running Schedule" );
            Usecase usecase = UsecaseBuilder.newUsecase( "ScheduleRunner" );
            UnitOfWork uow = module.newUnitOfWork( usecase );
            try
            {
                Schedule schedule = uow.get( Schedule.class, this.schedule.scheduleIdentity );
                Task task = schedule.task().get();
                schedule = uow.get( Schedule.class, this.schedule.scheduleIdentity );
                try
                {
                    schedule.taskStarting();
                    task.run();
                    schedule.taskCompletedSuccessfully();
                }
                catch( RuntimeException ex )
                {
                    schedule.taskCompletedWithException( ex );
                }
                schedulerMixin.dispatchForExecution( schedule );
                uow.complete();
            }
            catch( UnitOfWorkCompletionException ex )
            {
            }
            finally
            {
                // What should we do if we can't manage the Running flag??
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }
    }
}
