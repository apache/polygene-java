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

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import org.codeartisans.sked.crontab.schedule.CronSchedule;

/**
 * Behavior of a Schedule.
 *
 * A Schedule computes its next run based on its cron expression.
 * Cron expression syntax is documented in {@link CronExpressionConstraint}
 * 
 * @author Paul Merlin
 */
@Mixins( ScheduleBehavior.Mixin.class )
public interface ScheduleBehavior
{

    void setAsRunning();

    /**
     * @return Next run or null.
     */
    Long computeNextRun();

    class Mixin
            implements ScheduleBehavior
    {

        @This
        private ScheduleState state;

        public void setAsRunning()
        {
            state.running().set( true );
        }

        public Long computeNextRun()
        {
            String cronExpression = state.cronExpression().get();
            long now = System.currentTimeMillis();
            Long nextRun = new CronSchedule( cronExpression ).firstRunAfter( now );
            if ( nextRun < now ) {
                return null;
            }
            return nextRun;
        }

    }

}
