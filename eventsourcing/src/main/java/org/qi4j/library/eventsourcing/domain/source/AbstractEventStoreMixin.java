/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.source;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.synchronizedList;

/**
 * Base implementation for EventStores.
 */
public abstract class AbstractEventStoreMixin
        implements EventStore, EventStream, Activatable
{
    @This
    protected Identity identity;

    protected Logger logger;
    protected ValueType domainEventType;
    protected ValueType eventsType;

    protected Lock lock = new ReentrantLock();

    @Structure
    protected ModuleSPI module;

    private ExecutorService transactionNotifier;

    final private List<UnitOfWorkEventsListener> listeners = synchronizedList( new ArrayList<UnitOfWorkEventsListener>() );

    public void activate() throws IOException
    {
        logger = LoggerFactory.getLogger( identity.identity().get() );

        domainEventType = module.valueDescriptor( DomainEventValue.class.getName() ).valueType();
        eventsType = module.valueDescriptor( UnitOfWorkDomainEventsValue.class.getName() ).valueType();

        transactionNotifier = Executors.newSingleThreadExecutor();
    }

    public void passivate() throws Exception
    {
        transactionNotifier.shutdown();
        transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
    }

    // UnitOfWorkEventsVisitor implementation
    // This is how transactions are put into the store


    public void storeEvents( final UnitOfWorkDomainEventsValue events )
            throws IOException
    {
        // Lock store so noone else can interrupt
        lock();
        try
        {
            storeEvents0( events );
        } finally
        {
            lock.unlock();
        }

        // Notify listeners
        transactionNotifier.submit( new Runnable()
        {
            public void run()
            {
                synchronized (listeners)
                {
                    for (UnitOfWorkEventsListener listener : listeners)
                    {
                        try
                        {
                            listener.notifyTransactions( Collections.singleton( events ) );
                        } catch (Exception e)
                        {
                            logger.warn( "Could not notify event listener", e );
                        }
                    }
                }
            }
        } );
    }

    // EventStream implementation
    public void registerListener( UnitOfWorkEventsListener subscriber )
    {
        listeners.add( subscriber );
    }

    public void unregisterListener( UnitOfWorkEventsListener subscriber )
    {
        listeners.remove( subscriber );
    }

    abstract protected void storeEvents0( UnitOfWorkDomainEventsValue unitOfWorkDomainValue )
            throws IOException;

    /**
     * Fix for this bug:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
     */
    protected void lock()
    {
        while (true)
        {
            try
            {
                lock.tryLock( 1000, TimeUnit.MILLISECONDS );
                break;
            } catch (InterruptedException e)
            {
                // Try again
            }
        }
    }
}
