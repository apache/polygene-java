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

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CronScheduleTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( CronSchedule.class );
        module.entities( Task.class ).withMixins( DummyTask.class );
    }

    @Test
    public void given15SecondCronWhenRequestingNextExpectEvery15Seconds()
        throws Exception
    {

        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<Task> builder1 = work.newEntityBuilder( Task.class );
        builder1.instance().name().set( "abc" );
        Task task = builder1.newInstance();
        EntityBuilder<CronSchedule> builder = work.newEntityBuilder( CronSchedule.class );
        builder.instance().start().set( DateTime.now() );
        builder.instance().task().set( task );
        builder.instance().cronExpression().set( "*/15 * * * * *" );
        CronSchedule schedule = builder.newInstance();
        long nextRun = schedule.nextRun( System.currentTimeMillis() );
        for( int i = 0; i < 1000; i++ )
        {
            long previousRun = nextRun;
            nextRun = schedule.nextRun( previousRun ); 
            assertThat( "nextRun( previousRun + 1s ) @" + i, nextRun, is( previousRun + 15000 ) );
        }
        work.discard();
    }

    public static abstract class DummyTask implements Task
    {
        @Override
        public void run()
        {
            System.out.println( "Dummy" );
        }
    }
}
