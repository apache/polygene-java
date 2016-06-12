/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.scheduler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.scheduler.bootstrap.SchedulerAssembler;
import org.apache.zest.library.scheduler.timeline.Timeline;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.zest.functional.Iterables.count;
import static org.apache.zest.library.scheduler.Constants.BAR;
import static org.apache.zest.library.scheduler.Constants.BAZAR;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SchedulerTest
    extends AbstractSchedulerTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerTest.class );

    @Override
    protected void onAssembly( ModuleAssembly testAssembly )
        throws AssemblyException
    {
        @SuppressWarnings( "UnnecessaryLocalVariable" )
        ModuleAssembly moduleAssembly = testAssembly;

        @SuppressWarnings( "UnnecessaryLocalVariable" )
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
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            FooTask task = createFooTask( uow, "TestTask", BAZAR );
            taskId = task.identity().get();
            task.run();
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            FooTask task = uow.get( FooTask.class, taskId );
            assertThat( task.runCounter().get(), equalTo( 1 ) );
            assertThat( task.output().get(), equalTo( BAR ) );
        }
    }

    @Test
    public void testMinutely()
        throws UnitOfWorkCompletionException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "TestMinutely" );
        ZonedDateTime start = ZonedDateTime.now();
        String taskIdentity;
        long sleepMillis;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            Scheduler scheduler = serviceFinder.findService( Scheduler.class ).get();

            FooTask task = createFooTask( uow, usecase.name(), BAZAR );
            taskIdentity = task.identity().get();

            ZonedDateTime expectedRun = start.withNano( 0 ).withSecond( 0 ).plusMinutes( 1 );
            scheduler.scheduleCron( task, "@minutely" );

            uow.complete();

            sleepMillis = Duration.between( start, expectedRun ).toMillis();
            LOGGER.info( "Task scheduled on {} to be run at {}", start.toLocalTime(), expectedRun.toLocalTime() );
        }

        await( usecase.name() )
            .atMost( sleepMillis + 5000, MILLISECONDS )
            .until( taskOutput( taskIdentity ), equalTo( 1 ) );

        //noinspection unused
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            Timeline timeline = serviceFinder.findService( Timeline.class ).get();
            ZonedDateTime now =  ZonedDateTime.now();

            // Queries returning past records
            assertThat( count( timeline.getLastRecords( 5 ) ),
                        is( 2L ) );
            assertThat( count( timeline.getRecords( start.toInstant(), now.toInstant() ) ),
                        is( 2L ) );

            // Queries returning future records
            assertThat( count( timeline.getNextRecords( 4 ) ),
                        is( 4L ) );
            assertThat( count( timeline.getRecords( now.plusNanos( 100000000L ), now.plusMinutes( 5 )) ),
                        is( 5L ) );

            // Queries returning mixed past and future records
            assertThat( count( timeline.getRecords( start, now.plusMinutes( 5 ) ) ),
                        is( 7L ) );
        }
    }

    @Test
    public void testOnce()
        throws UnitOfWorkCompletionException, InterruptedException
    {
        System.setProperty( "zest.entity.print.state", Boolean.TRUE.toString() );
        final Usecase usecase = UsecaseBuilder.newUsecase( "TestOnce" );
        final String taskIdentity;
        Scheduler scheduler = serviceFinder.findService( Scheduler.class ).get();

        Schedule schedule1;
        Schedule schedule2;
        Schedule schedule3;
        Schedule schedule4;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            FooTask task = createFooTask( uow, usecase.name(), BAZAR );
            taskIdentity = task.identity().get();

            schedule1 = scheduler.scheduleOnce( task, 1 );
            schedule2 = scheduler.scheduleOnce( task, 2 );
            schedule3 = scheduler.scheduleOnce( task, 3 );
            schedule4 = scheduler.scheduleOnce( task, 4 );

            uow.complete();
        }
        await( usecase.name() )
            .atMost( 6, SECONDS )
            .until( taskOutput( taskIdentity ), equalTo( 4 ) );

        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( usecase ) )
        {
            schedule1 = uow.get( schedule1 );
            schedule2 = uow.get( schedule2 );
            schedule3 = uow.get( schedule3 );
            schedule4 = uow.get( schedule4 );
            assertThat(schedule1.cancelled().get(), equalTo( false ));
            assertThat(schedule2.cancelled().get(), equalTo( false ));
            assertThat(schedule3.cancelled().get(), equalTo( false ));
            assertThat(schedule4.cancelled().get(), equalTo( false ));
            assertThat(schedule1.done().get(), equalTo( true ));
            assertThat(schedule2.done().get(), equalTo( true ));
            assertThat(schedule3.done().get(), equalTo( true ));
            assertThat(schedule4.done().get(), equalTo( true ));
            assertThat(schedule1.running().get(), equalTo( false ));
            assertThat(schedule2.running().get(), equalTo( false ));
            assertThat(schedule3.running().get(), equalTo( false ));
            assertThat(schedule4.running().get(), equalTo( false ));
        }
    }

    private Callable<Integer> taskOutput( final String taskIdentity )
    {
        return () -> {
            try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
            {
                FooTask task = uow.get( FooTask.class, taskIdentity );
                Integer count = task.runCounter().get();
                uow.discard();
                return count;
            }
        };
    }
}
