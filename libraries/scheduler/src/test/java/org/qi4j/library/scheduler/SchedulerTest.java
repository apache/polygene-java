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
import org.joda.time.Interval;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Iterables;
import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.qi4j.library.scheduler.timeline.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchedulerTest
    extends AbstractSchedulerTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerTest.class );

    protected void onAssembly( ModuleAssembly testAssembly )
        throws AssemblyException
    {
        ModuleAssembly moduleAssembly = testAssembly;
        ModuleAssembly configModuleAssembly = testAssembly;
// START SNIPPET: assembly
        new SchedulerAssembler().visibleIn( Visibility.layer )
            .withConfigAssembly( configModuleAssembly )
            .withTimeline()
            .assemble( moduleAssembly );
// END SNIPPET: assembly
    }

    @Test
    public void testTask()
        throws UnitOfWorkCompletionException,
               InterruptedException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "testTask" );
        UnitOfWork uow = module.newUnitOfWork( usecase );
        FooTask task = createFooTask( uow, "TestTask", Constants.BAZAR );

        String taskId = task.identity().get();
        task.run();
        uow.complete();

        usecase = UsecaseBuilder.newUsecase( "testTask" );
        uow = module.newUnitOfWork( usecase );
        task = uow.get( FooTask.class, taskId );
        assertEquals( Constants.BAR, task.output().get() );

        Thread.sleep( 10 * 1000 );
        uow.complete();
    }

    @Test
    public void testMinutely()
        throws Exception
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "testMinutely" );
        UnitOfWork uow = module.newUnitOfWork( usecase );
        try
        {
            Scheduler scheduler = module.<Scheduler>findService( Scheduler.class ).get();
            DateTime start = new DateTime();

            FooTask task = createFooTask( uow, "TestMinutely", Constants.BAZAR );
            String taskIdentity = task.identity().get();

            DateTime expectedRun = start.withMillisOfSecond( 0 ).withSecondOfMinute( 0 ).plusMinutes( 1 );
            scheduler.scheduleCron( task, "@minutely", true );

            uow.complete();

            LOGGER.info( "Task scheduled on {} to be run at {}", start.getMillis(), expectedRun.getMillis() );

            Thread.sleep( new Interval( start, expectedRun ).toDurationMillis() + 5000 ); // waiting a little more

            usecase = UsecaseBuilder.newUsecase( "testMinutely" );
            uow = module.newUnitOfWork( usecase );

            task = uow.get( FooTask.class, taskIdentity );
            assertNotNull( task );
            assertEquals( Constants.BAR, task.output().get() );

            Timeline timeline = module.<Timeline>findService( Timeline.class ).get();
            DateTime now = new DateTime();

            // Queries returning past records
            assertEquals( 2, Iterables.count( timeline.getLastRecords( 5 ) ) );
            assertEquals( 2, Iterables.count( timeline.getRecords( start.getMillis(), now.getMillis() ) ) );

            // Queries returning future records
            assertEquals( 4, Iterables.count( timeline.getNextRecords( 4 ) ) );
            assertEquals( 5, Iterables.count( timeline.getRecords( now.getMillis() + 100,
                                                                   now.plusMinutes( 5 ).getMillis() ) ) );

            // Queries returning mixed past and future records
            assertEquals( 7, Iterables.count( timeline.getRecords( start.getMillis(),
                                                                   now.plusMinutes( 5 ).getMillis() ) ) );

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

    @Test
    public void testOnce()
        throws UnitOfWorkCompletionException,
               InterruptedException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "testOnce" );
        UnitOfWork uow = module.newUnitOfWork( usecase );
        try
        {
            Scheduler scheduler = module.<Scheduler>findService( Scheduler.class ).get();

            FooTask task = createFooTask( uow, "TestOnce", Constants.BAZAR );
            String taskIdentity = task.identity().get();

            scheduler.scheduleOnce( task, 4, true );

            uow.complete();

            Thread.sleep( 5000 );

            usecase = UsecaseBuilder.newUsecase( "testOnce" );
            uow = module.newUnitOfWork( usecase );

            task = uow.get( FooTask.class, taskIdentity );
            assertNotNull( task );
            assertEquals( Constants.BAR, task.output().get() );

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
}
