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

import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.library.scheduler.bootstrap.TimelineAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import org.qi4j.library.scheduler.task.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Merlin
 */
public class SchedulerTest
        extends AbstractQi4jTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerTest.class );
    private static final Integer PULSE_RHYTHM_SECS = Integer.valueOf( 5 );
    private static final Integer PULSE_RHYTHM_MILLIS = Integer.valueOf( PULSE_RHYTHM_SECS * 1000 );
    private static final Integer GC_RHYTHM_SECS = Integer.valueOf( 30 );

    public void assemble( ModuleAssembly testAssembly )
            throws AssemblyException
    {
        new SchedulerAssembler().assemble( testAssembly );
        testAssembly.addEntities( SchedulerConfiguration.class );
        SchedulerConfiguration config = testAssembly.forMixin( SchedulerConfiguration.class ).declareDefaults();
        config.pulseRhythmSeconds().set( PULSE_RHYTHM_SECS );
        config.garbageCollectorRhythmSeconds().set( GC_RHYTHM_SECS );

        new TimelineAssembler().assemble( testAssembly );

        testAssembly.addEntities( FooTask.class );

        new EntityTestAssembler().assemble( testAssembly );
        new RdfMemoryStoreAssembler().assemble( testAssembly );
    }

    @Mixins( FooTask.Mixin.class )
    static interface FooTask
            extends Task, EntityComposite
    {

        Property<String> input();

        @Optional
        Property<String> output();

        static abstract class Mixin
                implements Runnable
        {

            @This
            private FooTask me;

            public void run()
            {
                LOGGER.info( "FooTaskEntity.run({})", me.input().get() );
                if ( me.input().get().equals( "bazar" ) ) {
                    me.output().set( "bar" );
                }
            }

        }

    }

    @Test
    @Ignore
    public void testTask()
            throws UnitOfWorkCompletionException, InterruptedException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        FooTask task = createFooTask( uow, "FooTaskName", "bazar" );

        String taskId = task.identity().get();
        task.run();
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        task = uow.get( FooTask.class, taskId );
        task.run();
        Assert.assertEquals( "bar", task.output().get() );

        Thread.sleep( 10 * 1000 );
        uow.complete();
    }

    @Test
    public void testMinutely()
            throws InterruptedException, UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        Scheduler scheduler = serviceLocator.<Scheduler>findService( Scheduler.class ).get();
        DateTime start = new DateTime();

        FooTask task = createFooTask( uow, "FooTaskName", "bazar" );
        String taskIdentity = task.identity().get();

        DateTime expectedRun = start.withMillisOfSecond( 0 ).withSecondOfMinute( 0 ).plusMinutes( 1 );
        scheduler.shedule( task, "@minutely" );

        uow.complete();

        LOGGER.info( "Task scheduled on {}, expected to be run at {}", start.getMillis(), expectedRun.getMillis() );

        Thread.sleep( new Interval( start, expectedRun ).toDurationMillis() + 1000 ); // waiting a little more

        uow = unitOfWorkFactory.newUnitOfWork();

        task = uow.get( FooTask.class, taskIdentity );
        Assert.assertNotNull( task );
        Assert.assertEquals( "bar", task.output().get() );

        uow.complete();
    }

    private FooTask createFooTask( UnitOfWork uow, String name, String input )
    {
        EntityBuilder<FooTask> builder = uow.newEntityBuilder( FooTask.class );
        FooTask task = builder.instance();
        task.name().set( name );
        task.input().set( input );
        return builder.newInstance();
    }

}
