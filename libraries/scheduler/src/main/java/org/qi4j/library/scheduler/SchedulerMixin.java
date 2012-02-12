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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.codeartisans.sked.crontab.schedule.CronSchedule;
import org.codeartisans.sked.crontab.schedule.CronScheduleFactoryImpl;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleFactory;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.qi4j.library.scheduler.schedule.ScheduleRunner;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.slaves.SchedulerPulse;
import org.qi4j.library.scheduler.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerMixin
    implements Scheduler, Activatable
{
    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private static final int DEFAULT_PULSE_RHYTHM = 60;
    private static final int DEFAULT_GC_RHYTHM = 600;
    private static final int DEFAULT_WORKERS_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_WORKQUEUE_SIZE = 10;

    @Service
    private ScheduleFactory scheduleFactory;

    @Service
    private ScheduleRepository scheduleRepository;

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

    public void enqueue( String scheduleIdentity )
    {
        ScheduleRunner scheduleRunner = module.newObject( ScheduleRunner.class, scheduleIdentity );
        taskExecutor.execute( scheduleRunner );
        LOGGER.debug( "Enqueued Task identity to be run immediatly: {}", scheduleIdentity );
    }

    public Schedule scheduleOnce( Task task, int initialSecondsDelay )
    {
        CronSchedule cronEx = new CronScheduleFactoryImpl().newNowInstance( initialSecondsDelay );
        return scheduleFactory.newSchedule( task, cronEx.toString(), System.currentTimeMillis() );
    }

    public Schedule schedule( Task task, String cronExpression )
    {
        return scheduleFactory.newSchedule( task, cronExpression, System.currentTimeMillis() );
    }

    public Schedule schedule( Task task, String cronExpression, long initialDelay )
    {
        return scheduleFactory.newSchedule( task, cronExpression, System.currentTimeMillis() + initialDelay );
    }

    public Schedule schedule( Task task, String cronExpression, Date start )
    {
        return scheduleFactory.newSchedule( task, cronExpression, start.getTime() );
    }

    @Override
    public void activate()
        throws Exception
    {

        // Handle configuration defaults
        SchedulerConfiguration configuration = config.configuration();
        Integer workersCount = configuration.workersCount().get();
        Integer workQueueSize = configuration.workQueueSize().get();
        Integer pulseRhythmSeconds = configuration.pulseRhythmSeconds().get();
        Integer gcRhythmSeconds = configuration.garbageCollectorRhythmSeconds().get();

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
        if( pulseRhythmSeconds == null )
        {
            pulseRhythmSeconds = DEFAULT_PULSE_RHYTHM;
            LOGGER.debug( "Pulse rythm absent from configuration, falled back to default: {} seconds", DEFAULT_PULSE_RHYTHM );
        }
        if( gcRhythmSeconds == null )
        {
            gcRhythmSeconds = DEFAULT_GC_RHYTHM;
            LOGGER.debug( "Garbage Collector rythm absent from configuration, falled back to default: {} seconds", DEFAULT_GC_RHYTHM );
        }

        removeNonDurableSchedules();
        setAllSchedulesAsNotRunning();

        taskExecutor = new ThreadPoolExecutor( workersCount, workersCount,
                                               0, TimeUnit.MILLISECONDS,
                                               new LinkedBlockingQueue<Runnable>( workQueueSize ),
                                               threadFactory, rejectionHandler );
        taskExecutor.prestartAllCoreThreads();

        managementExecutor = new ScheduledThreadPoolExecutor( 2, threadFactory, rejectionHandler );

        // Start the Pulse
        SchedulerPulse pulse = module.newObject( SchedulerPulse.class );
        managementExecutor.scheduleAtFixedRate( pulse, 0, pulseRhythmSeconds, TimeUnit.SECONDS );

        // Start the Garbage Collector
        SchedulerGarbageCollector garbageCollector = module.newObject( SchedulerGarbageCollector.class );
        managementExecutor.scheduleAtFixedRate( garbageCollector, 0, gcRhythmSeconds, TimeUnit.SECONDS );

        LOGGER.debug( "Activated" );
    }

    @Override
    public void passivate()
        throws Exception
    {
        LOGGER.debug( "Passivated" );
    }

    private void removeNonDurableSchedules()
        throws UnitOfWorkCompletionException
    {
        // Remove not durable schedules
        UnitOfWork uow = module.newUnitOfWork();
        Query<ScheduleEntity> notDurableQuery = scheduleRepository.findNotDurable( me.identity().get() );
        long notDurableCount = notDurableQuery.count();
        if( notDurableCount > 0 )
        {
            LOGGER.debug( "Found {} not durable schedules at activation, removing them", notDurableCount );
            for( ScheduleEntity eachNotDurable : notDurableQuery )
            {
                uow.remove( eachNotDurable );
            }
        }
        uow.complete();
    }

    private void setAllSchedulesAsNotRunning()
        throws UnitOfWorkCompletionException
    {
        // Handling schedules that were running when last activated
        UnitOfWork uow = module.newUnitOfWork();
        Query<ScheduleEntity> runningQuery = scheduleRepository.findRunning( me.identity().get() );
        long runningCount = runningQuery.count();
        if( runningCount > 0 )
        {
            LOGGER.debug( "Found {} running schedules at activation, setting them back to not running", runningCount );
            for( ScheduleEntity eachRunning : runningQuery )
            {
                eachRunning.running().set( false );
            }
        }
        uow.complete();
    }
}
