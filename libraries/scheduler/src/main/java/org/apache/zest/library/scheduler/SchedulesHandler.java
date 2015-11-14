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

import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.library.scheduler.schedule.Schedules;

@Mixins(SchedulesHandler.SchedulesHandlerMixin.class)
public interface SchedulesHandler
{
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY)
    Schedules getActiveSchedules();

    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY)
    Schedules getCancelledSchedules();

    class SchedulesHandlerMixin implements SchedulesHandler
    {
        @This
        private Identity me;

        @Structure
        private Module module;

        @Override
        public Schedules getActiveSchedules()
        {
            return getOrCreateSchedules(getActiveSchedulesIdentity());
        }

        @Override
        public Schedules getCancelledSchedules()
        {
            return getOrCreateSchedules(getCancelledSchedulesIdentity());
        }

        public String getActiveSchedulesIdentity()
        {
            return "Schedules-Active:" + me.identity().get();
        }

        public String getCancelledSchedulesIdentity()
        {
            return "Schedules-Cancelled:" + me.identity().get();
        }

        private Schedules getOrCreateSchedules( String identity ){
            UnitOfWork uow = module.currentUnitOfWork();
            Schedules schedules;
            try
            {
                schedules = uow.get( Schedules.class, identity );
            }
            catch( NoSuchEntityException e )
            {
                // Create a new Schedules entity for keeping track of them all.
                schedules = uow.newEntity( Schedules.class, identity );
            }
            return schedules;

        }

    }
}
