/*
 * Copyright (c) 2010-2014, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.scheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceActivation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( { SchedulerMixin.class, SchedulerService.ThreadFactory.class, SchedulerService.RejectionHandler.class } )
public interface SchedulerService
    extends Scheduler, ServiceActivation, Identity
{
    class RejectionHandler
        implements RejectedExecutionHandler
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( SchedulerService.class );

        @Override
        public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
        {
            LOGGER.error( "Runnable [" + r + "] was rejected by executor [" + executor + "]" );
        }
    }

    class ThreadFactory
        implements java.util.concurrent.ThreadFactory
    {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger( 1 );
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger( 1 );
        private final String namePrefix;

        protected ThreadFactory( @This SchedulerService me )
        {
            SecurityManager sm = System.getSecurityManager();
            group = ( sm != null ) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = me.identity().get() + "-P" + POOL_NUMBER.getAndIncrement() + "W";
        }

        @Override
        public Thread newThread( Runnable runnable )
        {
            Thread thread = new Thread( group, runnable, namePrefix + threadNumber.getAndIncrement(), 0 );
            if( thread.isDaemon() )
            {
                thread.setDaemon( false );
            }
            if( thread.getPriority() != Thread.NORM_PRIORITY )
            {
                thread.setPriority( Thread.NORM_PRIORITY );
            }
            return thread;
        }
    }

}
