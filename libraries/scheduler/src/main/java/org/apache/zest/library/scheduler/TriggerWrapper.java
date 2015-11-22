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

import java.util.Date;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;

@Mixins( TriggerWrapper.OperableMixin.class)
public interface TriggerWrapper extends OperableTrigger
{
    enum State {waiting, aquired }
    Property<Trigger> trigger();

    Property<Long> nextTime();

    Property<State> state();

    abstract class OperableMixin
        implements OperableTrigger{

        @Override
        public void triggered( Calendar calendar )
        {

        }

        @Override
        public Date computeFirstFireTime( Calendar calendar )
        {
            return null;
        }

        @Override
        public CompletedExecutionInstruction executionComplete( JobExecutionContext context,
                                                                JobExecutionException result
        )
        {
            return null;
        }

        @Override
        public void updateAfterMisfire( Calendar cal )
        {

        }

        @Override
        public void updateWithNewCalendar( Calendar cal, long misfireThreshold )
        {

        }

        @Override
        public void validate()
            throws SchedulerException
        {

        }

        @Override
        public void setFireInstanceId( String id )
        {

        }

        @Override
        public String getFireInstanceId()
        {
            return null;
        }

        @Override
        public void setNextFireTime( Date nextFireTime )
        {

        }

        @Override
        public void setPreviousFireTime( Date previousFireTime )
        {

        }

        @Override
        public void setKey( TriggerKey key )
        {

        }

        @Override
        public void setJobKey( JobKey key )
        {

        }

        @Override
        public void setDescription( String description )
        {

        }

        @Override
        public void setCalendarName( String calendarName )
        {

        }

        @Override
        public void setJobDataMap( JobDataMap jobDataMap )
        {

        }

        @Override
        public void setPriority( int priority )
        {

        }

        @Override
        public void setStartTime( Date startTime )
        {

        }

        @Override
        public void setEndTime( Date endTime )
        {

        }

        @Override
        public void setMisfireInstruction( int misfireInstruction )
        {

        }

        @Override
        public int compareTo( Trigger other )
        {
            return -1;
        }

        @Override
        public Object clone()
        {
            throw new UnsupportedOperationException( "This operation is not supported." );
        }
    }
}
