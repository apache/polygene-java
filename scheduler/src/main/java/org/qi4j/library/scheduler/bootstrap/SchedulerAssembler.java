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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.scheduler.SchedulerService;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleFactory;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.qi4j.library.scheduler.schedule.ScheduleRunner;
import org.qi4j.library.scheduler.slaves.SchedulerGarbageCollector;
import org.qi4j.library.scheduler.slaves.SchedulerPulse;
import org.qi4j.library.scheduler.slaves.SchedulerWorkQueue;

/**
 * @author Paul Merlin
 */
public class SchedulerAssembler
        implements Assembler
{

    private final Visibility visibility;

    public SchedulerAssembler()
    {
        this( Visibility.module );
    }

    public SchedulerAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly schedulerAssembly )
            throws AssemblyException
    {
        schedulerAssembly.addObjects( SchedulerPulse.class,
                                      SchedulerGarbageCollector.class,
                                      SchedulerWorkQueue.class,
                                      ScheduleRunner.class ).
                visibleIn( Visibility.module );

        schedulerAssembly.addServices( ScheduleFactory.class,
                                       ScheduleRepository.class ).
                visibleIn( Visibility.module );

        schedulerAssembly.addServices( SchedulerService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();

        schedulerAssembly.addEntities( ScheduleEntity.class ).
                visibleIn( visibility );
    }

}
