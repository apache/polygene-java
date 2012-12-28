/*
 * Copyright (c) 2010-2012, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.scheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( { SchedulerMixin.class, SchedulerService.ThreadFactory.class, SchedulerService.RejectionHandler.class } )
@Activators( SchedulerActivation.Activator.class )
public interface SchedulerService extends Scheduler, SchedulerActivation, Configuration, ServiceComposite
{
    class RejectionHandler
        implements RejectedExecutionHandler
    {
        public static final Logger logger = LoggerFactory.getLogger( SchedulerService.class );

        @Override
        public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
        {
            logger.error( "Runnable [" + r + "] was rejected by executor [" + executor + "]" );
        }
    }

    class ThreadFactory
        implements java.util.concurrent.ThreadFactory
    {

        private static final AtomicInteger poolNumber = new AtomicInteger( 1 );
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger( 1 );
        private final String namePrefix;

        protected ThreadFactory( @This SchedulerService me )
        {
            SecurityManager sm = System.getSecurityManager();
            group = ( sm != null ) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = me.identity().get() + "-P" + poolNumber.getAndIncrement() + "W";
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
