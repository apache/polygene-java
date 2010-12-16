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

import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import org.qi4j.library.scheduler.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rhythmed slave run at a given rhythm.
 * A rhythmed slave starts after a delay depending on the rhythm.
 * Rhythmed slaves triggered on clock synchronized systems with the same rhythm are in sync.
 * Clock skew due to multithreading is canceled on each cycle based on epoch.
 *
 * To compute the initial delay we take a common starting point of time: epoch.
 *
 * A rhythmed slave cycle must be as short as possible.
 *
 * When a cycle is longer than the rhythm itself, the next cycle will use the starting time
 * of the previous cycle when fetching runnable Tasks.
 *
 * What happens when a slave is locked?
 *  TODO Provide a configurable timeout and simply kill/miss the task issueing an error log.
 * 
 * @author Paul Merlin
 */
@SuppressWarnings( "ProtectedField" )
abstract class AbstractRhythmedSchedulerSlave
        implements Runnable
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( Scheduler.class );
    private final String name;
    protected final Long rhythm;
    protected Boolean commitSuicide = false;

    AbstractRhythmedSchedulerSlave( String name, Long rhythm )
    {
        this.name = name;
        this.rhythm = rhythm;
    }

    @SuppressWarnings( "SleepWhileHoldingLock" )
    public void run()
    {
        long initialDelay = rhythm - System.currentTimeMillis() % rhythm;
        LOGGER.debug( "{} will initialy wait {}ms to be synched with epoch", name, initialDelay );

        try {
            Thread.sleep( initialDelay );
        } catch ( InterruptedException ex ) {
            LOGGER.warn( "{} initial delay interrupted, stopping early ? if not the first cycle will start with a clock skew", name, ex );
        }

        while ( !commitSuicide ) {

            try {
                LOGGER.debug( "{} Cycle! {}", name, System.currentTimeMillis() );
                cycle();
            } catch ( Exception ex ) {
                LOGGER.error( "{} cycle thrown an exception", name, ex );
            }

            try {
                // We recompute the delay each time to mitigate any clock skew
                Thread.sleep( rhythm - System.currentTimeMillis() % rhythm );
            } catch ( InterruptedException ex ) {
                LOGGER.warn( "Delay between {} cycles interrupted, stopping ? if not clock skew might get chaotic", name, ex );
            }

        }
    }

    public final void suicideAfterCurrentCycle()
    {
        commitSuicide = true;
    }

    abstract void cycle()
            throws UnitOfWorkCompletionException;

}
