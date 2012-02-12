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
package org.qi4j.library.scheduler.slaves;

import java.util.ArrayList;
import java.util.Collection;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.SchedulerService;
import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.qi4j.library.scheduler.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heartbeat of the {@link Scheduler}, load runnable {@link Task}s and enqueue them in the {@link Scheduler}.
 */
public class SchedulerPulse
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger( SchedulerPulse.class );

    @Structure
    private Module module;

    @Service
    private SchedulerService scheduler;

    @Service
    private ScheduleRepository scheduleRepository;

    private long lastCycleEnd = -1;

    @Override
    public void run()
    {
        long cycleEnd = System.currentTimeMillis() + 1;

        UnitOfWork uow = module.newUnitOfWork();
        try
        {

            Query<ScheduleEntity> toRun = scheduleRepository.findRunnables( scheduler.identity()
                                                                                .get(), lastCycleEnd + 1, cycleEnd );
            Collection<String> schedulesIdentities = new ArrayList<String>();
            for( ScheduleEntity eachSchedule : toRun )
            {
                schedulesIdentities.add( eachSchedule.identity().get() );
                eachSchedule.running().set( true );
            }

            uow.complete();

            for( String eachScheduleIdentity : schedulesIdentities )
            {
                logger.info( "Queueing " + eachScheduleIdentity );
                scheduler.enqueue( eachScheduleIdentity );
            }

            lastCycleEnd = cycleEnd;
        }
        catch( ConcurrentEntityModificationException e )
        {
            logger.info( "Garbage Collection of Schedules failed. Should recover by itself." );
        }
        catch( UnitOfWorkCompletionException e )
        {
            logger.info( "Garbage Collection of Schedules failed. Should recover by itself." );
        }
        finally
        {
            if( uow.isOpen() )
            {
                uow.discard();
            }
        }
    }
}
