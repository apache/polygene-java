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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkRetry;
import org.apache.zest.library.scheduler.schedule.Schedule;
import org.apache.zest.library.scheduler.schedule.ScheduleTime;

@Mixins( Execution.ExecutionMixin.class )
@Concerns( UnitOfWorkConcern.class )
public interface Execution
{

    void dispatchForExecution( Schedule schedule );

    void start()
        throws Exception;

    void stop()
        throws Exception;

    @UnitOfWorkPropagation
    @UnitOfWorkRetry( retries = 3 )
    void updateNextTime( ScheduleTime schedule );

    class ExecutionMixin
        implements Execution, Runnable
    {
        private static final ThreadGroup TG = new ThreadGroup( "Zest Scheduling" );

        @Structure
        private Module module;

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
        private Thread scheduleThread;

        @Override
        @UnitOfWorkPropagation
        public void run()
        {
            synchronized( this )
            {
                running = true;
                while( running )
                {
                    try
                    {
                        if( timingQueue.size() > 0 )
                        {
                            ScheduleTime scheduleTime = timingQueue.first();
                            waitFor( scheduleTime );
                            timingQueue.remove( scheduleTime );
                            updateNextTime( scheduleTime );
                            submitTaskForExecution( scheduleTime );
                        }
                        else
                        {
                            this.wait( 100 );
                        }
                    }
                    catch( InterruptedException e )
                    {
                        // Ignore. Used to signal "Hey, wake up. Time to work..."
                    }
                }
            }
        }

        private void waitFor( ScheduleTime scheduleTime )
            throws InterruptedException
        {
            long now = System.currentTimeMillis();
            long waitingTime = scheduleTime.nextTime() - now;
            if( waitingTime > 0 )
            {
                this.wait( waitingTime );
            }
        }

        @Override
        public void updateNextTime( ScheduleTime scheduleTime )
        {
            long now = System.currentTimeMillis();

            try (UnitOfWork uow = module.newUnitOfWork())
            {
                try
                {
                    Schedule schedule = uow.get( Schedule.class, scheduleTime.scheduleIdentity() );
                    long nextTime = schedule.nextRun( now );
                    if( nextTime != Long.MIN_VALUE )
                    {
                        scheduleTime = new ScheduleTime( schedule.identity().get(), nextTime );
                        timingQueue.add( scheduleTime );
                    }
                }
                catch( NoSuchEntityException e )
                {
                    // Schedule has been removed.
                    scheduler.cancelSchedule( scheduleTime.scheduleIdentity() );
                }
                uow.complete();
            }
            catch( UnitOfWorkCompletionException e )
            {
                throw new UndeclaredThrowableException( e );
            }
        }

        private void submitTaskForExecution( ScheduleTime scheduleTime )
        {
            Runnable taskRunner = module.newTransient( Runnable.class, scheduleTime );
            this.taskExecutor.submit( taskRunner );
        }

        public void dispatchForExecution( Schedule schedule )
        {
            long now = System.currentTimeMillis();
            synchronized( this )
            {
                long nextRun = schedule.nextRun( now );
                if( nextRun > 0 )
                {
                    timingQueue.add( new ScheduleTime( schedule.identity().get(), nextRun ) );
                    scheduleThread.interrupt();
                }
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

            scheduleThread = new Thread( TG, this, "Scheduler" );
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
