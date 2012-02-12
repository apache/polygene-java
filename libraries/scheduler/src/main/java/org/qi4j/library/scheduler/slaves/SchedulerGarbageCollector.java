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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.scheduler.SchedulerService;
import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Continuously prune non-used and non-durable {@link ScheduleEntity}s.
 */
public class SchedulerGarbageCollector
    implements Runnable
{
    public static final Logger logger = LoggerFactory.getLogger( SchedulerGarbageCollector.class );

    @Structure
    private Module module;

    @Service
    private SchedulerService scheduler;

    @Service
    private ScheduleRepository scheduleRepository;

    @Override
    public void run()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            Query<ScheduleEntity> toDelQuery = scheduleRepository.findNotDurableWithoutNextRun( scheduler.identity()
                                                                                                    .get() );
            long toDelCount = toDelQuery.count();
            if( toDelCount > 0 )
            {
                logger.debug( "GC found {} not durable schedules without next run, removing them", toDelCount );
                for( ScheduleEntity eachToDel : toDelQuery )
                {
                    uow.remove( eachToDel );
                }
            }
            uow.complete();
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
