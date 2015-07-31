/*
 * Copyright 2009-2010 Rickard Öberg AB
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

package org.qi4j.library.eventsourcing.application.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.functional.Iterables;
import org.qi4j.io.Input;
import org.qi4j.io.Inputs;
import org.qi4j.io.Output;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;
import org.qi4j.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.synchronizedList;

/**
 * Base implementation for ApplicationEventStores.
 */
public abstract class AbstractApplicationEventStoreMixin
        implements ApplicationEventStore, ApplicationEventStream, ApplicationEventStoreActivation
{
    @This
    protected Identity identity;

    protected Logger logger;
    protected ValueType domainEventType;
    protected ValueType transactionEventsType;

    protected Lock lock = new ReentrantLock();

    @Structure
    protected Module module;

    @Structure
    private ValueBuilderFactory vbf;

    private ExecutorService transactionNotifier;

    final private List<Output<TransactionApplicationEvents, ? extends Throwable>> listeners = synchronizedList( new ArrayList<Output<TransactionApplicationEvents, ? extends Throwable>>() );

    private long lastTimestamp = 0;

    @Override
    public void activateApplicationEventStore()
            throws Exception
    {
        logger = LoggerFactory.getLogger( identity.identity().get() );

        domainEventType = module.valueDescriptor( ApplicationEvent.class.getName() ).valueType();
        transactionEventsType = module.valueDescriptor( TransactionApplicationEvents.class.getName() ).valueType();

        transactionNotifier = Executors.newSingleThreadExecutor();
    }

    @Override
    public void passivateApplicationEventStore()
            throws Exception
    {
        transactionNotifier.shutdown();
        transactionNotifier.awaitTermination( 10000, TimeUnit.MILLISECONDS );
    }

    // This is how transactions are put into the store
    @Override
    public TransactionApplicationEvents storeEvents( Iterable<ApplicationEvent> events ) throws IOException
    {
        // Create new TransactionApplicationEvents
        ValueBuilder<TransactionApplicationEvents> builder = vbf.newValueBuilder( TransactionApplicationEvents.class );
        Iterables.addAll( builder.prototype().events().get(), events );
        builder.prototype().timestamp().set( getCurrentTimestamp() );

        final TransactionApplicationEvents transactionDomain = builder.newInstance();

        // Lock store so noone else can interrupt
        lock();
        try
        {
            storeEvents( transactionDomain );
        } finally
        {
            lock.unlock();
        }

        // Notify listeners
        transactionNotifier.submit( new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (listeners)
                {
                    Input<TransactionApplicationEvents, RuntimeException> input = Inputs.iterable( Collections.singleton( transactionDomain ) );
                    for (Output<TransactionApplicationEvents, ? extends Throwable> listener : listeners)
                    {
                        try
                        {
                            input.transferTo( listener );
                        } catch (Throwable e)
                        {
                            logger.warn( "Could not notify event listener", e );
                        }
                    }
                }
            }
        } );

        return transactionDomain;
    }

    // EventStream implementation


    @Override
    public void registerListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
    {
        listeners.add( listener );
    }


    @Override
    public void unregisterListener( Output<TransactionApplicationEvents, ? extends Throwable> listener )
    {
        listeners.remove( listener );
    }

    abstract protected void rollback()
            throws IOException;

    abstract protected void commit()
            throws IOException;

    abstract protected void storeEvents( TransactionApplicationEvents transactionDomain )
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

    private synchronized long getCurrentTimestamp()
    {
        long timestamp = System.currentTimeMillis();
        if (timestamp <= lastTimestamp)
            timestamp = lastTimestamp + 1; // Increase by one to ensure uniqueness
        lastTimestamp = timestamp;
        return timestamp;
    }
}
