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

import static org.qi4j.api.common.Visibility.module;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.junit.Test;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.qi4j.library.scheduler.timeline.Timeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerTest
        extends AbstractSchedulerTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerTest.class );

    protected void onAssembly( ModuleAssembly testAssembly )
            throws AssemblyException
    {
        new SchedulerAssembler().visibleIn( module ).
                withConfigAssembly( testAssembly ).
                withPulseRhythm( Constants.PULSE_RHYTHM_SECS ).
                withGarbageCollectorRhythm( Constants.GC_RHYTHM_SECS ).
                withTimeline().
                assemble( testAssembly );
    }

    @Test
    public void testTask()
            throws UnitOfWorkCompletionException,
            InterruptedException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        FooTask task = createFooTask( uow, "TestTask", Constants.BAZAR );

        String taskId = task.identity().get();
        task.run();
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        task = uow.get( FooTask.class, taskId );
        assertEquals( Constants.BAR, task.output().get() );

        Thread.sleep( 10 * 1000 );
        uow.complete();
    }

    @Test
    public void testMinutely()
            throws InterruptedException,
            UnitOfWorkCompletionException,
            Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        Scheduler scheduler = serviceLocator.<Scheduler>findService( Scheduler.class ).get();
        DateTime start = new DateTime();

        FooTask task = createFooTask( uow, "TestMinutely", Constants.BAZAR );
        String taskIdentity = task.identity().get();

        DateTime expectedRun = start.withMillisOfSecond( 0 ).withSecondOfMinute( 0 ).plusMinutes( 1 );
        scheduler.schedule( task, "@minutely" );

        uow.complete();

        LOGGER.info( "Task scheduled on {} to be run at {}", start.getMillis(), expectedRun.getMillis() );

        Thread.sleep( new Interval( start, expectedRun ).toDurationMillis() + 15000 ); // waiting a little more

        uow = unitOfWorkFactory.newUnitOfWork();

        task = uow.get( FooTask.class, taskIdentity );
        assertNotNull( task );
        assertEquals( Constants.BAR, task.output().get() );

        Timeline timeline = serviceLocator.<Timeline>findService( Timeline.class ).get();
        DateTime now = new DateTime();

        // Queries returning past records
        assertEquals( 1, Iterables.count( timeline.getLastRecords( 5 ) ) );
        assertEquals( 1, Iterables.count( timeline.getRecords( start.getMillis(), now.getMillis() ) ) );

        // Queries returning future records
        assertEquals( 5, Iterables.count( timeline.getNextRecords( 5 ) ) );
        assertEquals( 5, Iterables.count( timeline.getRecords( now.getMillis() + 100, now.plusMinutes( 5 ).getMillis() ) ) );

        // Queries returning mixed past and future records
        assertEquals( 6, Iterables.count( timeline.getRecords( start.getMillis(), now.plusMinutes( 5 ).getMillis() ) ) );

        uow.complete();
    }

    @Test
    public void testOnce()
            throws UnitOfWorkCompletionException,
            InterruptedException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        Scheduler scheduler = serviceLocator.<Scheduler>findService( Scheduler.class ).get();

        FooTask task = createFooTask( uow, "TestOnce", Constants.BAZAR );
        String taskIdentity = task.identity().get();

        scheduler.scheduleOnce( task, 10 );

        uow.complete();

        Thread.sleep( 20000 );

        uow = unitOfWorkFactory.newUnitOfWork();

        task = uow.get( FooTask.class, taskIdentity );
        assertNotNull( task );
        assertEquals( Constants.BAR, task.output().get() );

        uow.complete();
    }

}
