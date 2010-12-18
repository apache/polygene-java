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

import org.codeartisans.sked.crontab.schedule.CronSchedule;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

public abstract class ScheduleEntityMixin
        implements ScheduleEntity
{

    @This
    private ScheduleEntity me;

    public Long firstRunAfter( Long start )
    {
        Long firstRun = new CronSchedule( me.cronExpression().get() ).firstRunAfter( start );
        return firstRun >= start ? firstRun : null; // FIXME CronExpression return -1 where it should return null, fix it there
    }

    public void beforeCompletion()
            throws UnitOfWorkCompletionException
    {
        if ( !me.running().get() ) {
            me.nextRun().set( firstRunAfter( System.currentTimeMillis() ) );
        }
    }

    public void afterCompletion( UnitOfWorkStatus status )
    {
        // NOOP
    }

}
