/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.library.scheduler.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.scheduler.Schedule;
import org.apache.zest.library.scheduler.Scheduler;
import org.apache.zest.library.scheduler.SchedulerConfiguration;

/**
 * This composite handles the Execution of Schedules.
 *
 * The composite is internal and should never be used by clients.
 */
@Mixins( Execution.ExecutionMixin.class )
public interface Execution
{
    void dispatchForExecution( Schedule schedule );

    void start()
        throws Exception;

    void stop()
        throws Exception;

    void updateNextTime( ScheduleTime schedule );   // This method is public, only because the UnitOfWorkConcern is wanted.

    class ExecutionMixin
        implements Execution, Runnable
    {
        public static final ThreadGroup TG = new ThreadGroup( "Zest Scheduling" );

        private final Object lock = new Object();

        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private TransientBuilderFactory tbf;

        @This
        private Scheduler scheduler;

        @This
        private Configuration<SchedulerConfiguration> config;

        @This
        private ThreadFactory threadFactory;

        @This
        private RejectedExecutionHandler rejectionHandler;

        private final SortedSet<ScheduleTime> timingQueue = new TreeSet<>();
        private volatile boolean running;
        private ThreadPoolExecutor taskExecutor;
        private volatile Thread scheduleThread;

        @Override
        public void run()
        {
            running = true;
            while( running )
            {
                try
                {
                    ScheduleTime scheduleTime = timing();
                    if( scheduleTime != null )
                    {
                        waitFor( scheduleTime );

                        if( isTime( scheduleTime ) ) // We might have been awakened to reschedule
                        {
                            updateNextTime( scheduleTime );
                        }
                    }
                    else
                    {
                        waitFor( 100 );
                    }
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
        }

        private ScheduleTime timing()
        {
            synchronized( lock )
            {
                if( timingQueue.size() == 0 )
                {
                    return null;
                }
                return timingQueue.first();
            }
        }

        private boolean isTime( ScheduleTime scheduleTime )
        {
            return scheduleTime.nextTime().isBefore( Instant.now() );
        }

        private void waitFor( ScheduleTime scheduleTime )
            throws InterruptedException
        {
            Duration waitingTime = Duration.between( Instant.now(), scheduleTime.nextTime() );
            waitFor( waitingTime.toMillis() );
        }

        private void waitFor( long waitingTime )
        {
            if( waitingTime > 0 )
            {
                synchronized( lock )
                {
                    try
                    {
                        lock.wait( waitingTime );
                    }
                    catch( InterruptedException e )
                    {
                        // should be ignored.
                    }
                }
            }
        }

        @Override
        public void updateNextTime( ScheduleTime oldScheduleTime )
        {
            try (UnitOfWork uow = uowf.newUnitOfWork()) // This will discard() the UoW when block is exited. We are only doing reads, so fine.
            {
                submitTaskForExecution( oldScheduleTime );
                Schedule schedule = uow.get( Schedule.class, oldScheduleTime.scheduleIdentity() );
                Instant nextTime = schedule.nextRun( Instant.now() );
                if( nextTime.isAfter( Instant.MIN ) )
                {
                    ScheduleTime newScheduleTime = new ScheduleTime( oldScheduleTime.scheduleIdentity(), nextTime );
                    synchronized( lock )
                    {
                        // Re-add to the Timing Queue, to re-position the sorting.
                        timingQueue.remove( oldScheduleTime );
                        timingQueue.add( newScheduleTime );
                    }
                }
                else
                {
                    synchronized( lock )
                    {
                        timingQueue.remove( oldScheduleTime );
                    }
                }
            }
            catch( NoSuchEntityException e )
            {
                e.printStackTrace();
//                scheduler.cancelSchedule( oldScheduleTime.scheduleIdentity() );
            }
        }

        private void submitTaskForExecution( ScheduleTime scheduleTime )
        {
            Runnable taskRunner = tbf.newTransient( Runnable.class, scheduleTime );
            this.taskExecutor.submit( taskRunner );
        }

        @Override
        public void dispatchForExecution( Schedule schedule )
        {
            Instant nextRun = schedule.nextRun( Instant.now() );
            if( nextRun.equals( Instant.MIN ) )
            {
                return;
            }
            synchronized( lock )
            {
                timingQueue.add( new ScheduleTime( schedule.identity().get(), nextRun ) );
                lock.notifyAll();
            }
        }

        @Override
        public void start()
            throws Exception
        {
            SchedulerConfiguration configuration = config.get();
            Integer workersCount = configuration.workersCount().get();
            Integer workQueueSize = configuration.workQueueSize().get();
            createThreadPoolExecutor( workersCount, workQueueSize );
            taskExecutor.prestartAllCoreThreads();

            SecurityManager sm = System.getSecurityManager();
            ThreadGroup threadGroup = sm != null ? sm.getThreadGroup() : TG;
            scheduleThread = new Thread( threadGroup, this, "Scheduler" );
            scheduleThread.start();
        }

        private void createThreadPoolExecutor( Integer workersCount, Integer workQueueSize )
        {
            int corePoolSize = 2;
            if( workersCount > 4 )
            {
                corePoolSize = workersCount / 4 + 1;
            }
            if( corePoolSize > 50 )
            {
                corePoolSize = 20;
            }
            if( workersCount > 200 )
            {
                workersCount = 200;
            }
            taskExecutor = new ThreadPoolExecutor( corePoolSize, workersCount,
                                                   0, TimeUnit.MILLISECONDS,
                                                   new LinkedBlockingQueue<>( workQueueSize ),
                                                   threadFactory, rejectionHandler );
        }

        @Override
        public void stop()
            throws Exception
        {
            running = false;
            synchronized( this )
            {
                scheduleThread.interrupt();
            }
            taskExecutor.shutdown();
            try
            {
                taskExecutor.awaitTermination( 5, TimeUnit.SECONDS );
            }
            catch( InterruptedException e )
            {
                e.printStackTrace();
            }
            taskExecutor.shutdownNow();
        }
    }
}
