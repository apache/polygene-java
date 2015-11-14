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

import java.lang.reflect.UndeclaredThrowableException;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.library.scheduler.schedule.Schedule;
import org.apache.zest.library.scheduler.schedule.ScheduleTime;

public class TaskRunner
    implements Runnable
{
    @Structure
    private Module module;

    @Uses
    private ScheduleTime schedule;

    @Override
    @UnitOfWorkPropagation( usecase = "Task Runner" )
    public void run()
    {
        try
        {
            UnitOfWork uow = module.currentUnitOfWork();
            Schedule schedule = uow.get( Schedule.class, this.schedule.scheduleIdentity() );
            Task task = schedule.task().get();
            try
            {
                schedule.taskStarting();
                task.run();
                schedule.taskCompletedSuccessfully();
            }
            catch( RuntimeException ex )
            {
                Throwable exception = ex;
                while(exception instanceof UndeclaredThrowableException)
                {
                    exception = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
                }
                schedule.taskCompletedWithException( exception );
                schedule.exceptionCounter().set( schedule.exceptionCounter().get() + 1 );
            }
            schedule.executionCounter().set( schedule.executionCounter().get() + 1 );
            uow.complete();
        }
        catch( Exception e )
        {
            throw new UndeclaredThrowableException( e );
        }
    }
}
