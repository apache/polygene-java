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

    public void beforeCompletion()
            throws UnitOfWorkCompletionException
    {
        if ( !me.running().get() ) {
            // Compute it even if it's not null, cronExpression could have been changed
            String cronExpression = me.cronExpression().get();
            long now = System.currentTimeMillis();
            Long nextRun = new CronSchedule( cronExpression ).firstRunAfter( now );
            if ( nextRun < now ) {
                me.nextRun().set( null );
            }
            me.nextRun().set( nextRun );
        }
    }

    public void afterCompletion( UnitOfWorkStatus status )
    {
        // NOOP
    }

}
