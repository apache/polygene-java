/*
 * Copyright (c) 2010-2014, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.scheduler;

import java.util.concurrent.Callable;
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
import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.qi4j.library.scheduler.timeline.Timeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.functional.Iterables.count;
import static org.qi4j.library.scheduler.Constants.BAR;
import static org.qi4j.library.scheduler.Constants.BAZAR;

public class SchedulerTest
    extends AbstractSchedulerTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerTest.class );

    @Override
    protected void onAssembly( ModuleAssembly testAssembly )
        throws AssemblyException
    {
        ModuleAssembly moduleAssembly = testAssembly;
        ModuleAssembly configModuleAssembly = testAssembly;
        // START SNIPPET: assembly
        new SchedulerAssembler().visibleIn( Visibility.application )
            .withConfig( configModuleAssembly, Visibility.layer )
            .withTimeline()
            .assemble( moduleAssembly );
        // END SNIPPET: assembly
    }

    @Test
    public void testTaskWithoutScheduling()
        throws UnitOfWorkCompletionException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "testTask" );
        String taskId;
        try( UnitOfWork uow = module.newUnitOfWork( usecase ) )
        {
            FooTask task = createFooTask( uow, "TestTask", BAZAR );
            taskId = task.identity().get();
            task.run();
            uow.complete();
        }
        try( UnitOfWork uow = module.newUnitOfWork( usecase ) )
        {
            FooTask task = uow.get( FooTask.class, taskId );
            assertThat( task.output().get(), equalTo( BAR ) );
        }
    }

    @Test
    public void testMinutely()
        throws UnitOfWorkCompletionException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "TestMinutely" );
        DateTime start = new DateTime();
        String taskIdentity;
        long sleepMillis;
        try( UnitOfWork uow = module.newUnitOfWork( usecase ) )
        {
            Scheduler scheduler = module.findService( Scheduler.class ).get();

            FooTask task = createFooTask( uow, usecase.name(), BAZAR );
            taskIdentity = task.identity().get();

            DateTime expectedRun = start.withMillisOfSecond( 0 ).withSecondOfMinute( 0 ).plusMinutes( 1 );
            scheduler.scheduleCron( task, "@minutely", true );

            uow.complete();

            sleepMillis = new Interval( start, expectedRun ).toDurationMillis();
            LOGGER.info( "Task scheduled on {} to be run at {}", start.getMillis(), expectedRun.getMillis() );
        }

        await( usecase.name() ).
            atMost( sleepMillis + 5000, MILLISECONDS ).
            until( taskOutput( taskIdentity ), equalTo( BAR ) );

        try( UnitOfWork uow = module.newUnitOfWork( usecase ) )
        {
            Timeline timeline = module.findService( Timeline.class ).get();
            DateTime now = new DateTime();

            // Queries returning past records
            assertThat( count( timeline.getLastRecords( 5 ) ),
                        is( 2L ) );
            assertThat( count( timeline.getRecords( start.getMillis(), now.getMillis() ) ),
                        is( 2L ) );

            // Queries returning future records
            assertThat( count( timeline.getNextRecords( 4 ) ),
                        is( 4L ) );
            assertThat( count( timeline.getRecords( now.getMillis() + 100, now.plusMinutes( 5 ).getMillis() ) ),
                        is( 5L ) );

            // Queries returning mixed past and future records
            assertThat( count( timeline.getRecords( start.getMillis(), now.plusMinutes( 5 ).getMillis() ) ),
                        is( 7L ) );
        }
    }

    @Test
    public void testOnce()
        throws UnitOfWorkCompletionException
    {
        final Usecase usecase = UsecaseBuilder.newUsecase( "TestOnce" );
        final String taskIdentity;
        try( UnitOfWork uow = module.newUnitOfWork( usecase ) )
        {
            Scheduler scheduler = module.findService( Scheduler.class ).get();

            FooTask task = createFooTask( uow, usecase.name(), BAZAR );
            taskIdentity = task.identity().get();

            scheduler.scheduleOnce( task, 2, true );

            uow.complete();
        }

        await( usecase.name() ).until( taskOutput( taskIdentity ), equalTo( BAR ) );
    }

    private Callable<String> taskOutput( final String taskIdentity )
    {
        return new Callable<String>()
        {
            @Override
            public String call()
                throws Exception
            {
                try( UnitOfWork uow = module.newUnitOfWork() )
                {
                    FooTask task = uow.get( FooTask.class, taskIdentity );
                    return task == null ? null : task.output().get();
                }
            }
        };
    }
}
