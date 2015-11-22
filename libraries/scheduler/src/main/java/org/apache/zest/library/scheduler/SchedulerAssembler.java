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

import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;

public class SchedulerAssembler
    implements Assembler
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( JobStore.class ).withMixins( JobStoreMixin.class ).withConcerns( UnitOfWorkConcern.class );
        module.services( SchedulerService.class ).instantiateOnStartup();
        module.entities( JobWrapper.class );
        module.entities( SchedulerConfiguration.class );
        defineDefaults( module );
        module.entities( TriggerWrapper.class );
        module.entities( CalendarWrapper.class );
        module.entities( Calendars.class );
        module.entities( TriggersGroup.class );
        module.entities( TriggersGroups.class );
        module.entities( JobsGroup.class );
        module.entities( JobsGroups.class );
        module.values( ZestJobDetail.class );
        module.objects( ZestJobFactory.class );
    }

    private void defineDefaults( ModuleAssembly module )
    {
        SchedulerConfiguration defaults = module.forMixin( SchedulerConfiguration.class ).declareDefaults();

        defaults.idleWaitTime().set( 1000L );
        defaults.interrupOnShutdown().set( true );
        defaults.interrupOnShutdownWithWait().set( true );
        defaults.batchTimeWindow().set( 15000L );
        defaults.threadCount().set( 2 );
        defaults.threadPriority().set( Thread.NORM_PRIORITY );
    }
}
