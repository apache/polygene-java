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

import java.lang.annotation.Retention;
import org.apache.zest.api.constraint.Constraint;
import org.apache.zest.api.constraint.ConstraintDeclaration;
import org.apache.zest.api.constraint.Constraints;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.constraints.annotation.InstanceOf;
import org.apache.zest.library.constraints.annotation.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Mixins( CronSchedule.CronScheduleMixin.class )
public interface CronSchedule
    extends Schedule
{
    /**
     * The Cron expression indicating when the Schedule is to be run.
     * The Schedule can NOT be changed once it is set. If this is needed, delete this Schedule and attach the Task
     * to a new Schedule.
     *
     * @return The cron expression that will be used on {@link org.apache.zest.api.unitofwork.UnitOfWork} completion to compute next run
     */
    @CronExpression
    @Immutable
    Property<String> cronExpression();

    abstract class CronScheduleMixin
        implements CronSchedule
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( Schedule.class );

        @Override
        public void taskStarting()
        {
        }

        @Override
        public void taskCompletedSuccessfully()
        {
        }

        @Override
        public void taskCompletedWithException( Throwable ex )
        {
        }

        @Override
        public String presentationString()
        {
            return cronExpression().get();
        }

        @Override
        public long nextRun( long from )
        {
            long actualFrom = from;
            long firstRun = start().get().getMillis();
            if( firstRun > from )
            {
                actualFrom = firstRun;
            }
            // TODO:PM cron "next run" handling mismatch with the underlying cron library
            Long nextRun = createCron().firstRunAfter( actualFrom + 1000 );
            LOGGER.info( "CronSchedule::nextRun({}) is {}", from, firstRun );
            return nextRun;
        }

        private org.codeartisans.sked.cron.CronSchedule createCron()
        {
            return new org.codeartisans.sked.cron.CronSchedule( cronExpression().get() );
        }
    }

    @ConstraintDeclaration
    @Retention( RUNTIME )
    @NotEmpty
    @InstanceOf( String.class )
    @Constraints( CronExpressionConstraint.class )
    @interface CronExpression
    {
    }

    class CronExpressionConstraint
        implements Constraint<CronExpression, String>
    {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isValid( CronExpression annotation, String cronExpression )
        {
            return org.codeartisans.sked.cron.CronSchedule.isExpressionValid( cronExpression );
        }
    }
}
