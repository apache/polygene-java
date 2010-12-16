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

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.slaves.SchedulerPulse;
import org.qi4j.library.scheduler.slaves.SchedulerWorkQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Merlin
 */
public class SchedulerActivation
        implements Activatable
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private static final int DEFAULT_PULSE_RHYTHM = 60;
    private static final int DEFAULT_GC_RHYTHM = 60;
    private static final int DEFAULT_WORKERS_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_WORKQUEUE_SIZE = 10;
    @Structure
    private ObjectBuilderFactory obf;
    @Structure
    private UnitOfWorkFactory uowf;
    @This
    private Configuration<SchedulerConfiguration> config;
    @This
    private SchedulerService me;
    @Service
    private ScheduleRepository scheduleRepository;
    private SchedulerWorkQueue workQueue;
    private SchedulerPulse pulse;
    private SchedulerGarbageCollector gc;
    private Thread pulseThread;
    private Thread gcThread;

    public void activate()
            throws Exception
    {
        // Handle configuration defaults
        SchedulerConfiguration configuration = config.configuration();
        Integer workersCount = configuration.workersCount().get();
        Integer workQueueSize = configuration.workQueueSize().get();
        Integer pulseRhythmSeconds = configuration.pulseRhythmSeconds().get();
        Integer gcRhythmSeconds = configuration.garbageCollectorRhythmSeconds().get();

        if ( workersCount == null ) {
            workersCount = DEFAULT_WORKERS_COUNT;
            LOGGER.debug( "Workers count absent from configuration, falled back to default: {} workers", DEFAULT_WORKERS_COUNT );
        }
        if ( workQueueSize == null ) {
            workQueueSize = DEFAULT_WORKQUEUE_SIZE;
            LOGGER.debug( "WorkQueue size absent from configuration, falled back to default: {}", DEFAULT_WORKQUEUE_SIZE );
        }
        if ( pulseRhythmSeconds == null ) {
            pulseRhythmSeconds = DEFAULT_PULSE_RHYTHM;
            LOGGER.debug( "Pulse rythm absent from configuration, falled back to default: {} seconds", DEFAULT_PULSE_RHYTHM );
        }
        if ( gcRhythmSeconds == null ) {
            gcRhythmSeconds = DEFAULT_GC_RHYTHM;
            LOGGER.debug( "Garbage Collector rythm absent from configuration, falled back to default: {} seconds", DEFAULT_GC_RHYTHM );
        }

        removeNonDurableSchedules();
        setAllSchedulesAsNotRunning();

        startGarbageCollector( gcRhythmSeconds );
        startPulse( workersCount, workQueueSize, pulseRhythmSeconds );

        LOGGER.debug( "Activated" );

    }

    public void passivate()
            throws Exception
    {
        // Handle configuration defaults
        SchedulerConfiguration configuration = config.configuration();
        Boolean stopViolently = configuration.stopViolently().get();
        if ( stopViolently == null ) {
            stopViolently = Boolean.FALSE;
        }

        stopGarbageCollector();
        stopPulse( stopViolently );

        LOGGER.debug( "Passivated" );
    }

    private void removeNonDurableSchedules()
            throws UnitOfWorkCompletionException
    {
        // Remove not durable schedules
        UnitOfWork uow = uowf.newUnitOfWork();
        Query<ScheduleEntity> notDurableQuery = scheduleRepository.findNotDurable();
        long notDurableCount = notDurableQuery.count();
        if ( notDurableCount > 0 ) {
            LOGGER.debug( "Found {} not durable schedules at activation, removing them", notDurableCount );
            for ( ScheduleEntity eachNotDurable : notDurableQuery ) {
                uow.remove( eachNotDurable );
            }
        }
        uow.complete();
    }

    private void setAllSchedulesAsNotRunning()
            throws UnitOfWorkCompletionException
    {
        // Handling schedules that were running when last activated
        UnitOfWork uow = uowf.newUnitOfWork();
        Query<ScheduleEntity> runningQuery = scheduleRepository.findRunning();
        long runningCount = runningQuery.count();
        if ( runningCount > 0 ) {
            LOGGER.debug( "Found {} running schedules at activation, setting them back to not running", runningCount );
            for ( ScheduleEntity eachRunning : runningQuery ) {
                eachRunning.running().set( false );
            }
        }
        uow.complete();
    }

    private void startPulse( Integer workersCount, Integer workQueueSize, Integer pulseRhythmSeconds )
    {
        workQueue = obf.newObjectBuilder( SchedulerWorkQueue.class ).
                use( me.identity().get() ).
                use( workersCount ).
                use( workQueueSize ).
                newInstance();
        pulse = obf.newObjectBuilder( SchedulerPulse.class ).
                use( Long.valueOf( pulseRhythmSeconds * 1000 ) ).
                use( workQueue ).
                newInstance();
        pulseThread = new Thread( pulse, me.identity().get() + "-Pulse" );
        pulseThread.start();
        LOGGER.debug( "Pulsing every {}s", pulseRhythmSeconds );
    }

    private void stopPulse( Boolean stopViolently )
    {
        pulse.suicideAfterCurrentCycle();
        pulseThread.interrupt();

        // TODO wait for running tasks !

        pulse = null;
        pulseThread = null;
    }

    private void startGarbageCollector( Integer gcRhythmSeconds )
    {
        gc = obf.newObjectBuilder( SchedulerGarbageCollector.class ).
                use( Long.valueOf( gcRhythmSeconds * 1000 ) ).
                newInstance();
        gcThread = new Thread( gc, me.identity().get() + "-GC" );
        gcThread.start();
        LOGGER.debug( "Garbage Collecting every {}s", gcRhythmSeconds );
    }

    private void stopGarbageCollector()
    {
        gc.suicideAfterCurrentCycle();
        gcThread.interrupt();
        gc = null;
        gcThread = null;
    }

}
