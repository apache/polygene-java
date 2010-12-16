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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import org.qi4j.library.scheduler.schedule.ScheduleEntity;
import org.qi4j.library.scheduler.schedule.ScheduleRepository;

/**
 * Heartbeat of the Scheduler, load runnable Tasks and enqueue them in the work queue.
 * 
 * @author Paul Merlin
 */
public class SchedulerPulse
        extends AbstractRhythmedSchedulerSlave
{

    @Structure
    private UnitOfWorkFactory uowf;
    @Service
    private ScheduleRepository scheduleRepository;
    private final SchedulerWorkQueue workQueue;
    private long lastRun = -1;

    public SchedulerPulse( @Uses Long rhythm, @Uses SchedulerWorkQueue workQueue )
    {
        super( "Pulse", rhythm );
        this.workQueue = workQueue;
    }

    @Override
    void cycle()
            throws UnitOfWorkCompletionException
    {
        long now = System.currentTimeMillis();
        if ( lastRun == -1 ) {
            lastRun = now - 1;
        }

        UnitOfWork uow = uowf.newUnitOfWork();

        Query<ScheduleEntity> toRun = scheduleRepository.findRunnables( lastRun + 1, now + rhythm );
        Collection<String> schedulesIdentities = new ArrayList<String>();
        for ( ScheduleEntity eachSchedule : toRun ) {
            schedulesIdentities.add( eachSchedule.identity().get() );
            eachSchedule.running().set( true );
        }

        uow.complete();

        for ( String eachScheduleIdentity : schedulesIdentities ) {
            workQueue.enqueue( eachScheduleIdentity );
        }

        lastRun = now;
    }

}
