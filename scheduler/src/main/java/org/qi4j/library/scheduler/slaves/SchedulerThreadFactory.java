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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Paul Merlin
 */
class SchedulerThreadFactory
        implements ThreadFactory
{

    private static final AtomicInteger poolNumber = new AtomicInteger( 1 );
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger( 1 );
    private final String namePrefix;

    SchedulerThreadFactory( String schedulerIdentity )
    {
        SecurityManager sm = System.getSecurityManager();
        group = ( sm != null ) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = schedulerIdentity + "-P" + poolNumber.getAndIncrement() + "W";
    }

    public Thread newThread( Runnable runnable )
    {
        Thread thread = new Thread( group, runnable, namePrefix + threadNumber.getAndIncrement(), 0 );
        if ( thread.isDaemon() ) {
            thread.setDaemon( false );
        }
        if ( thread.getPriority() != Thread.NORM_PRIORITY ) {
            thread.setPriority( Thread.NORM_PRIORITY );
        }
        return thread;
    }

}
