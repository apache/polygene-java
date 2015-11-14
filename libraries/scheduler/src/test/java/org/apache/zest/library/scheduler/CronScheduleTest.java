/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.library.scheduler;

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.library.scheduler.schedule.cron.CronSchedule;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class CronScheduleTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new OrgJsonValueSerializationAssembler().assemble( module );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.entities( CronSchedule.class );
        module.entities( Task.class ).withMixins( DummyTask.class );
    }

    @Test
    public void given15SecondCronWhenRequestingNextExpectEvery15Seconds()
        throws Exception
    {

        UnitOfWork work = module.newUnitOfWork();
        EntityBuilder<Task> builder1 = work.newEntityBuilder( Task.class );
        builder1.instance().name().set( "abc" );
        Task task = builder1.newInstance();
        EntityBuilder<CronSchedule> builder = work.newEntityBuilder( CronSchedule.class );
        builder.instance().start().set( DateTime.now() );
        builder.instance().task().set( task );
        builder.instance().cronExpression().set( "*/15 * * * * *" );
        CronSchedule schedule = builder.newInstance();
        long runAt = schedule.nextRun( System.currentTimeMillis() );
        for( int i = 0; i < 1000; i++ )
        {
            long nextRun = schedule.nextRun( runAt + 1000 );  // Needs to push forward one second...
            assertThat( "At:" + i, (double) nextRun, closeTo( runAt + 15000, 50 ) );
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
