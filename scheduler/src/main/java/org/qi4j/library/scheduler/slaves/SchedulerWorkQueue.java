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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.schedule.ScheduleRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul Merlin
 */
public class SchedulerWorkQueue
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private ThreadPoolExecutor executor;
    @Structure
    private ObjectBuilderFactory obf;

    public SchedulerWorkQueue( @Uses String schedulerIdentity, @Uses Integer workersCount, @Uses Integer workQueueSize )
    {
        executor = new ThreadPoolExecutor( workersCount, workersCount,
                                           0, TimeUnit.MILLISECONDS,
                                           new LinkedBlockingQueue( workQueueSize ),
                                           new SchedulerThreadFactory( schedulerIdentity ) );
        executor.prestartAllCoreThreads();
    }

    void enqueue( String scheduleIdentity )
    {
        ScheduleRunner scheduleRunner = obf.newObjectBuilder( ScheduleRunner.class ).use( scheduleIdentity ).newInstance();
        executor.execute( scheduleRunner );
        LOGGER.debug( "Enqueued Task identity to be run immediatly: {}", scheduleIdentity );
    }

}
