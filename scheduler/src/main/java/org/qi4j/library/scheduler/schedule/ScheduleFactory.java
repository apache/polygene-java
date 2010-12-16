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
package org.qi4j.library.scheduler.schedule;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import org.qi4j.library.scheduler.task.Task;

/**
 * @author Paul Merlin
 */
@Mixins( ScheduleFactory.Mixin.class )
public interface ScheduleFactory
        extends ServiceComposite
{

    ScheduleEntity newSchedule( Task task, String cronExpression, long start );

    abstract class Mixin
            implements ScheduleFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public ScheduleEntity newSchedule( Task task, String cronExpression, long start )
        {
            EntityBuilder<ScheduleEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( ScheduleEntity.class );
            ScheduleEntity schedule = builder.instance();
            schedule.task().set( task );
            schedule.cronExpression().set( cronExpression );
            schedule.start().set( start );
            return builder.newInstance();
        }

    }

}
