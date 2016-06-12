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

package org.apache.zest.library.scheduler.defaults;

import java.time.Instant;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.scheduler.CronSchedule;
import org.apache.zest.library.scheduler.OnceSchedule;
import org.apache.zest.library.scheduler.Schedule;
import org.apache.zest.library.scheduler.ScheduleFactory;
import org.apache.zest.library.scheduler.SchedulerService;
import org.apache.zest.library.scheduler.Task;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScheduleFactoryMixin
    implements ScheduleFactory
{
    private static final Logger logger = LoggerFactory.getLogger( ScheduleFactory.class );

    @Structure
    private UnitOfWorkFactory uowf;

    @Service
    private SchedulerService scheduler;

    @Service
    private UuidIdentityGeneratorService uuid;

    @Override
    public CronSchedule newCronSchedule( Task task, String cronExpression, Instant start )
    {
        return newPersistentCronSchedule( task, cronExpression, start );
    }

    @Override
    public Schedule newOnceSchedule( Task task, Instant runAt )
    {
        return newPersistentOnceSchedule( task, runAt );
    }

    private CronSchedule newPersistentCronSchedule( Task task, String cronExpression, Instant start )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<CronSchedule> builder = uow.newEntityBuilder( CronSchedule.class );
        CronSchedule instance = builder.instance();
        instance.task().set( task );
        instance.start().set( start );
        instance.identity().set( uuid.generate( CronSchedule.class ) );
        instance.cronExpression().set( cronExpression );
        CronSchedule schedule = builder.newInstance();
        logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
        return schedule;
    }

    private Schedule newPersistentOnceSchedule( Task task, Instant runAt )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        EntityBuilder<OnceSchedule> builder = uow.newEntityBuilder( OnceSchedule.class );
        OnceSchedule builderInstance = builder.instance();
        builderInstance.task().set( task );
        builderInstance.start().set( runAt );
        builderInstance.identity().set( uuid.generate( OnceSchedule.class ) );
        OnceSchedule schedule = builder.newInstance();
        logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
        return schedule;
    }
}
