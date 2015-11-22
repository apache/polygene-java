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

package org.apache.library.scheduler;

import java.util.Date;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.library.scheduler.SchedulerAssembler;
import org.apache.zest.library.scheduler.SchedulerService;
import org.apache.zest.library.scheduler.ZestJob;
import org.apache.zest.library.scheduler.ZestJobDetail;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.jackson.JacksonValueSerializationAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.Test;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulerTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new SchedulerAssembler().assemble( module );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        new JacksonValueSerializationAssembler().assemble( module );
        module.entities( ZestJob.class ).withMixins( HelloJob.class );
    }

    @Test
    public void givenSchedulerWhenScheduleJobExpectJobExecuted()
        throws Exception
    {
        try(UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "testing" )) )
        {
            SchedulerService underTest = module.findService( SchedulerService.class ).get();
            ZestJob job = uow.newEntity( ZestJob.class, "job://group1.job1" );
            ZestJobDetail details = underTest.createJobDetails( job );

            CronTrigger trigger = newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(cronSchedule("* * * * * ?"))
                .build();
            underTest.getScheduler().scheduleJob( details, trigger );
            uow.complete();

        }
        Thread.sleep(15000);
    }

    public static abstract class HelloJob
        implements ZestJob
    {
        @Override
        public void execute( JobExecutionContext context )
            throws JobExecutionException
        {
            System.out.println("Hello, Quartz!");
        }
    }
}
