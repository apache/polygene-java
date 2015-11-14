/*
 * Copyright (c) 2010-2012, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.scheduler.bootstrap;

import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.EntityDeclaration;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.ServiceDeclaration;
import org.apache.zest.bootstrap.ValueDeclaration;
import org.apache.zest.library.scheduler.SchedulerConfiguration;
import org.apache.zest.library.scheduler.SchedulerService;
import org.apache.zest.library.scheduler.TaskRunner;
import org.apache.zest.library.scheduler.schedule.ScheduleFactory;
import org.apache.zest.library.scheduler.schedule.Schedules;
import org.apache.zest.library.scheduler.schedule.cron.CronSchedule;
import org.apache.zest.library.scheduler.schedule.once.OnceSchedule;
import org.apache.zest.library.scheduler.timeline.Timeline;
import org.apache.zest.library.scheduler.timeline.TimelineForScheduleConcern;
import org.apache.zest.library.scheduler.timeline.TimelineRecord;
import org.apache.zest.library.scheduler.timeline.TimelineScheduleMixin;
import org.apache.zest.library.scheduler.timeline.TimelineSchedulerServiceMixin;

/**
 * Assembler for Scheduler.
 *
 * Use this Assembler to add the Scheduler service to your application.
 * This Assembler provide a fluent api to programmatically configure configuration defaults and
 * activate the Timeline service assembly that allow to browse in past and future Task runs.
 *
 * Here is a full example:
 * <pre>
 *      new SchedulerAssembler().
 *              visibleIn( Visibility.layer ).
 *              withConfig( configModuleAssembly, Visibility.application ).
 *              withTimeline().
 *              assemble( module );
 * </pre>
 */
public class SchedulerAssembler
    extends Assemblers.VisibilityConfig<SchedulerAssembler>
{

    private static final int DEFAULT_WORKERS_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private static final int DEFAULT_WORKQUEUE_SIZE = 10;

    private boolean timeline;

    /**
     * Activate the assembly of Timeline related services.
     *
     * @return SchedulerAssembler
     */
    public SchedulerAssembler withTimeline()
    {
        timeline = true;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly assembly )
        throws AssemblyException
    {
        assembly.services( ScheduleFactory.class );
        assembly.entities( Schedules.class );
        EntityDeclaration scheduleEntities = assembly.entities( CronSchedule.class, OnceSchedule.class );

        ValueDeclaration scheduleValues = assembly.values( CronSchedule.class, OnceSchedule.class );

        ServiceDeclaration schedulerDeclaration = assembly.services( SchedulerService.class )
            .visibleIn( visibility() )
            .instantiateOnStartup();

        assembly.transients( Runnable.class ).withMixins( TaskRunner.class ).withConcerns( UnitOfWorkConcern.class );

        if( timeline )
        {
            scheduleEntities.withTypes( Timeline.class )
                .withMixins( TimelineScheduleMixin.class )
                .withConcerns( TimelineForScheduleConcern.class );

            scheduleValues.withTypes( Timeline.class )
                .withMixins( TimelineScheduleMixin.class )
                .withConcerns( TimelineForScheduleConcern.class );

            // Internal
            assembly.values( TimelineRecord.class );
            schedulerDeclaration.withTypes( Timeline.class ).withMixins( TimelineSchedulerServiceMixin.class );
        }

        if( hasConfig() )
        {
            configModule().entities( SchedulerConfiguration.class )
                .visibleIn( configVisibility() );
            SchedulerConfiguration defaults = assembly.forMixin( SchedulerConfiguration.class ).declareDefaults();
            defaults.workersCount().set( DEFAULT_WORKERS_COUNT );
            defaults.workQueueSize().set( DEFAULT_WORKQUEUE_SIZE );
        }
    }
}
