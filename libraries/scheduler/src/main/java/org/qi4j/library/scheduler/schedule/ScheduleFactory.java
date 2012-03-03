/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.scheduler.schedule;

import org.joda.time.DateTime;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.scheduler.SchedulerService;
import org.qi4j.library.scheduler.Task;
import org.qi4j.library.scheduler.schedule.cron.CronSchedule;
import org.qi4j.library.scheduler.schedule.once.OnceSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( ScheduleFactory.Mixin.class )
public interface ScheduleFactory extends ServiceComposite
{

    Schedule newCronSchedule( Task task, String cronExpression, DateTime start, boolean durable );

    Schedule newOnceSchedule( Task task, DateTime runAt, boolean durable );

    abstract class Mixin
        implements ScheduleFactory
    {
        private static final Logger logger = LoggerFactory.getLogger( ScheduleFactory.class );

        @Structure
        private Module module;

        @Service
        private SchedulerService scheduler;

        @Override
        public CronSchedule newCronSchedule( Task task, String cronExpression, DateTime start, boolean durable )
        {
            if( durable )
            {
                return newPersistentCronSchedule( task, cronExpression, start );
            }
            return newTransientCronSchedule( task, cronExpression, start );
        }

        @Override
        public Schedule newOnceSchedule( Task task, DateTime runAt, boolean durable )
        {
            if( durable )
            {
                return newPersistentOnceSchedule( task, runAt );
            }
            return newTransientOnceSchedule( task, runAt );
        }

        private CronSchedule newTransientCronSchedule( Task task, String cronExpression, DateTime start )
        {
            ValueBuilder<CronSchedule> builder = module.newValueBuilder( CronSchedule.class );
            CronSchedule prototype = builder.prototype();
            prototype.task().set( task );
            prototype.start().set( start );
            prototype.cronExpression().set( cronExpression );
            CronSchedule schedule = builder.newInstance();
            logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
            return schedule;
        }

        private CronSchedule newPersistentCronSchedule( Task task, String cronExpression, DateTime start )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<CronSchedule> builder = uow.newEntityBuilder( CronSchedule.class );
            CronSchedule builderInstance = builder.instance();
            builderInstance.task().set( task );
            builderInstance.start().set( start );
            builderInstance.cronExpression().set( cronExpression );
            CronSchedule schedule = builder.newInstance();
            logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
            return schedule;
        }

        private Schedule newTransientOnceSchedule( Task task, DateTime runAt )
        {
            ValueBuilder<OnceSchedule> builder = module.newValueBuilder( OnceSchedule.class );
            OnceSchedule builderInstance = builder.prototype();
            builderInstance.task().set( task );
            builderInstance.start().set( runAt );
            OnceSchedule schedule = builder.newInstance();
            logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
            return schedule;
        }

        private Schedule newPersistentOnceSchedule( Task task, DateTime runAt )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<OnceSchedule> builder = uow.newEntityBuilder( OnceSchedule.class );
            OnceSchedule builderInstance = builder.instance();
            builderInstance.task().set( task );
            builderInstance.start().set( runAt );
            OnceSchedule schedule = builder.newInstance();
            logger.info( "Schedule {} created: {}", schedule.presentationString(), schedule.identity().get() );
            return schedule;
        }
    }
}
