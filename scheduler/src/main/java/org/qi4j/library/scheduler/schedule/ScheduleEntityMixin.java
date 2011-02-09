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

import org.qi4j.library.scheduler.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduleEntityMixin
        implements ScheduleEntity
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    @This
    private ScheduleEntity me;

    public Long firstRunAfter( Long start )
    {
        Long firstRun = new CronSchedule( me.cronExpression().get() ).firstRunAfter( start );
        LOGGER.trace( "Schedule.firstRunAfter({}) CronSchedule result is {}", start, firstRun );
        return firstRun;
    }

    public void beforeCompletion()
            throws UnitOfWorkCompletionException
    {
        if ( !me.running().get() ) {

            Long nextRun = firstRunAfter( System.currentTimeMillis() );
            LOGGER.trace( "Schedule.beforeUoWCompletion() Not running, nextRun was {} and will be {}",
                          me.nextRun().get(), nextRun );

            me.nextRun().set( nextRun );

        } else if ( LOGGER.isTraceEnabled() ) {

            LOGGER.trace( "Schedule.beforeUoWCompletion() Running, nextRun is {}", me.nextRun().get() );
        }
    }

    public void afterCompletion( UnitOfWorkStatus status )
    {
        // NOOP
    }

}
