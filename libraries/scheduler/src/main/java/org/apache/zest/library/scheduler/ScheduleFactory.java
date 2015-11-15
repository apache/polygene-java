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

import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.library.scheduler.Schedule;
import org.apache.zest.library.scheduler.defaults.DefaultScheduleFactoryMixin;
import org.joda.time.DateTime;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.library.scheduler.Task;

import static org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.MANDATORY;

@Mixins( DefaultScheduleFactoryMixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface ScheduleFactory
{
    @UnitOfWorkPropagation( MANDATORY)
    Schedule newCronSchedule( Task task, String cronExpression, DateTime start );

    @UnitOfWorkPropagation( MANDATORY)
    Schedule newOnceSchedule( Task task, DateTime runAt );
}
