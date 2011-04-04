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
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.io.Transforms;
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


    @Override
    public Output<UnitOfWorkDomainEventsValue, IOException> storeEvents()
    {
        final Output<UnitOfWorkDomainEventsValue, IOException> storeOutput = storeEvents0();

        return new Output<UnitOfWorkDomainEventsValue, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<? extends UnitOfWorkDomainEventsValue, SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                final List<UnitOfWorkDomainEventsValue> events = new ArrayList<UnitOfWorkDomainEventsValue>(  );
                lock();
                try
                {
                    storeOutput.receiveFrom(new Sender<UnitOfWorkDomainEventsValue, SenderThrowableType>()
                    {
                        @Override
                        public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, SenderThrowableType
                        {
                            sender.sendTo( new Receiver<UnitOfWorkDomainEventsValue, ReceiverThrowableType>()
                            {
                                @Override
                                public void receive( UnitOfWorkDomainEventsValue item ) throws ReceiverThrowableType
                                {
                                    receiver.receive( item );
                                    events.add( item );
                                }
                            });
                        }
                    });

                } finally
                {
                    lock.unlock();
                }

                // Notify listeners
                transactionNotifier.submit( new Runnable()
                {
                    public void run()
                    {
                        synchronized(listeners)
                        {
                            for( UnitOfWorkEventsListener listener : listeners )
                            {
                                try
                                {
                                    listener.notifyTransactions( events );
                                } catch( Exception e )
                                {
                                    logger.warn( "Could not notify event listener", e );
                                }
                            }
                        }
                    }
                } );
            }
        };
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

    abstract protected Output<UnitOfWorkDomainEventsValue, IOException> storeEvents0();

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
