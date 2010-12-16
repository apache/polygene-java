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
package org.qi4j.library.scheduler.schedule;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.task.Task;
import org.qi4j.library.scheduler.timeline.TimelineRecorderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Merlin
 */
public class ScheduleRunner
        implements Runnable
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private final String scheduleIdentity;
    @Structure
    private UnitOfWorkFactory uowf;
    @Optional
    @Service
    private TimelineRecorderService timelineRecorder;

    public ScheduleRunner( @Uses String scheduleIdentity )
    {
        this.scheduleIdentity = scheduleIdentity;
    }

    // TODO Watch this code, see if we can do better, maybe leverage @UnitOfWorkRetry when it will be done
    public void run()
    {
        boolean taskRan = false;
        try {

            UnitOfWork uow = uowf.newUnitOfWork();

            ScheduleEntity schedule = uow.get( ScheduleEntity.class, scheduleIdentity );
            Task task = schedule.task().get();

            try {

                task.run();
                recordSuccess( task );

                taskRan = true;

            } catch ( Throwable ex ) {

                recordFailure( task, ex );

            }

            schedule.running().set( false );

            uow.complete();

        } catch ( UnitOfWorkCompletionException ex ) {

            if ( taskRan ) {

                LOGGER.warn( "Task ran but UoW did not complete, setting the Schedule as not running: {}", ex.getMessage(), ex );
                UnitOfWork uow = uowf.newUnitOfWork();
                ScheduleEntity schedule = uow.get( ScheduleEntity.class, scheduleIdentity );
                schedule.running().set( false );
                recordSuccess( schedule.task().get() );
                try {
                    uow.complete();
                } catch ( UnitOfWorkCompletionException fault ) {
                    LOGGER.error( "Task ran but UoW did not complete, was unable to set the Schedule as not running, this must be serious", fault );
                }

            } else {

                LOGGER.warn( "Task raised an exception and UoW did not complete, setting the Schedule as not running: {}", ex.getMessage(), ex );
                UnitOfWork uow = uowf.newUnitOfWork();
                ScheduleEntity schedule = uow.get( ScheduleEntity.class, scheduleIdentity );
                schedule.running().set( false );
                recordFailure( schedule.task().get(), ex );
                try {
                    uow.complete();
                } catch ( UnitOfWorkCompletionException fault ) {
                    LOGGER.error( "Task raised an exception and UoW did not complete, was unable to set the Schedule as not running, this must be serious", fault );
                }

            }

        }
    }

    private void recordSuccess( Task task )
    {
        if ( timelineRecorder != null ) {
            timelineRecorder.recordSuccess( task );
        }
    }

    private void recordFailure( Task task, Throwable cause )
    {
        if ( timelineRecorder != null ) {
            timelineRecorder.recordFailure( task, cause );
        }
    }

    public void oldrun()
    {
        try {

            UnitOfWork uow = uowf.newUnitOfWork();
            ScheduleEntity schedule = uow.get( ScheduleEntity.class, scheduleIdentity );

            schedule.task().get().run();

            schedule.running().set( false );

            if ( timelineRecorder != null ) {
                timelineRecorder.recordSuccess( schedule.task().get() );
            }

            uow.complete();

        } catch ( Throwable ex ) {
            LOGGER.error( "Unable to complete task: {}", ex.getMessage(), ex );
            if ( timelineRecorder != null ) {
                try {
                    UnitOfWork uow = uowf.newUnitOfWork();
                    ScheduleEntity schedule = uow.get( ScheduleEntity.class, scheduleIdentity );
                    timelineRecorder.recordFailure( schedule.task().get(), ex );
                    uow.complete();
                } catch ( UnitOfWorkCompletionException ignored ) {
                    LOGGER.error( "Unable to record failure of task: {}", ignored.getMessage(), ignored );
                }
            }
        }
    }

}
