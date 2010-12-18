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
package org.qi4j.library.scheduler.bootstrap;

import static org.qi4j.api.common.Visibility.module;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.scheduler.SchedulerConfiguration;
import org.qi4j.library.scheduler.SchedulerService;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleFactory;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.qi4j.library.scheduler.schedule.ScheduleRunner;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.slaves.SchedulerPulse;
import org.qi4j.library.scheduler.slaves.SchedulerWorkQueue;
import org.qi4j.library.scheduler.timeline.TimelineRecordEntity;
import org.qi4j.library.scheduler.timeline.TimelineRecordValue;
import org.qi4j.library.scheduler.timeline.TimelineRecorderService;
import org.qi4j.library.scheduler.timeline.TimelineService;

public class SchedulerAssembler
        implements Assembler
{

    private Visibility visibility = module;
    private ModuleAssembly configAssembly;
    private Integer pulseRhythm;
    private Integer garbageCollectorRhythm;
    private boolean timeline;

    public SchedulerAssembler visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public SchedulerAssembler withConfigAssembly( ModuleAssembly configAssembly )
    {
        this.configAssembly = configAssembly;
        return this;
    }

    /**
     * @param pulseRhythm   Scheduler pulse rhythm in seconds
     */
    public SchedulerAssembler withPulseRhythm( Integer pulseRhythm )
    {
        this.pulseRhythm = pulseRhythm;
        return this;
    }

    /**
     * @param garbageCollectorRhythm    Scheduler garbage collector rhythm in seconds
     */
    public SchedulerAssembler withGarbageCollectorRhythm( Integer garbageCollectorRhythm )
    {
        this.garbageCollectorRhythm = garbageCollectorRhythm;
        return this;
    }

    public SchedulerAssembler withTimeline()
    {
        timeline = true;
        return this;
    }

    public void assemble( ModuleAssembly assembly )
            throws AssemblyException
    {
        assembleInternals( assembly );
        assembleExposed( assembly );
        if ( configAssembly != null ) {
            assembleConfig( configAssembly );
        }
        if ( timeline ) {
            assembleTimeline( assembly );
        }
    }

    private void assembleInternals( ModuleAssembly assembly )
            throws AssemblyException
    {
        assembly.addObjects( SchedulerPulse.class,
                             SchedulerGarbageCollector.class,
                             SchedulerWorkQueue.class,
                             ScheduleRunner.class ).
                visibleIn( module );

        assembly.addServices( ScheduleFactory.class,
                              ScheduleRepository.class ).
                visibleIn( module );
    }

    private void assembleExposed( ModuleAssembly assembly )
            throws AssemblyException
    {
        assembly.addServices( SchedulerService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();

        assembly.addEntities( ScheduleEntity.class ).
                visibleIn( visibility );

    }

    private void assembleConfig( ModuleAssembly configAssembly )
            throws AssemblyException
    {
        configAssembly.addEntities( SchedulerConfiguration.class );
        if ( pulseRhythm != null || garbageCollectorRhythm != null ) {
            SchedulerConfiguration config = configAssembly.forMixin( SchedulerConfiguration.class ).declareDefaults();
            if ( pulseRhythm != null ) {
                config.pulseRhythmSeconds().set( pulseRhythm );
            }
            if ( garbageCollectorRhythm != null ) {
                config.garbageCollectorRhythmSeconds().set( garbageCollectorRhythm );
            }
        }
    }

    // FIXME Timeline assembly do not take care of visibility
    private void assembleTimeline( ModuleAssembly assembly )
            throws AssemblyException
    {
        assembly.addValues( TimelineRecordValue.class );

        assembly.addEntities( TimelineRecordEntity.class );

        assembly.addServices( TimelineService.class,
                              TimelineRecorderService.class );
    }

}
