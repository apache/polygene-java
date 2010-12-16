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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;

/**
 * This one is for pruning non-"persistent accross jvm runs" Schedules and unused CronExpressions.
 *
 * @author Paul Merlin
 */
public class SchedulerGarbageCollector
        extends AbstractRhythmedSchedulerSlave
{

    @Structure
    private UnitOfWorkFactory uowf;
    @Service
    private ScheduleRepository scheduleRepository;

    public SchedulerGarbageCollector( @Uses Long rhythm )
    {
        super( "GC", rhythm );
    }

    @Override
    void cycle()
            throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        Query<ScheduleEntity> toDelQuery = scheduleRepository.findNotDurableWithoutNextRun();
        long toDelCount = toDelQuery.count();
        if ( toDelCount > 0 ) {
            LOGGER.debug( "GC found {} not durable schedules without next run, removing them", toDelCount );
            for ( ScheduleEntity eachToDel : toDelQuery ) {
                uow.remove( eachToDel );
            }
        }
        uow.complete();
    }

}
