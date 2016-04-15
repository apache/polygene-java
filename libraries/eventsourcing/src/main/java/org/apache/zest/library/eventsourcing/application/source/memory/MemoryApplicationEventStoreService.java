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
package org.apache.zest.library.eventsourcing.application.source.memory;

import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.library.eventsourcing.application.api.TransactionApplicationEvents;
import org.apache.zest.library.eventsourcing.application.source.*;

import java.io.IOException;
import java.util.*;

/**
 * In-Memory ApplicationEventStore. Mainly used for testing.
 */
@Mixins( MemoryApplicationEventStoreService.MemoryStoreMixin.class )
@Activators( ApplicationEventStoreActivation.Activator.class )
public interface MemoryApplicationEventStoreService
    extends ApplicationEventSource, ApplicationEventStore, ApplicationEventStream, ApplicationEventStoreActivation, ServiceComposite
{
    abstract class MemoryStoreMixin
        extends AbstractApplicationEventStoreMixin
        implements ApplicationEventSource, ApplicationEventStoreActivation
    {
        // This list holds all transactions
        private LinkedList<TransactionApplicationEvents> store = new LinkedList<TransactionApplicationEvents>();

        @Override
        public Input<TransactionApplicationEvents, IOException> transactionsAfter( final long afterTimestamp, final long maxTransactions )
        {
            return new Input<TransactionApplicationEvents, IOException>()
            {
                @Override
                public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super TransactionApplicationEvents, ReceiverThrowableType> output )
                    throws IOException, ReceiverThrowableType
                {
                    // Lock store first
                    lock.lock();
                    try
                    {
                        output.receiveFrom( new Sender<TransactionApplicationEvents, IOException>()
                        {
                            @Override
                            public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super TransactionApplicationEvents, ReceiverThrowableType> receiver )
                                throws ReceiverThrowableType, IOException
                            {
                                Iterator<TransactionApplicationEvents> iterator = store.iterator();

                                long count = 0;

                                while( iterator.hasNext() && count < maxTransactions )
                                {
                                    TransactionApplicationEvents next = iterator.next();
                                    if( next.timestamp().get() > afterTimestamp )
                                    {
                                        receiver.receive( next );
                                        count++;
                                    }
                                }
                            }
                        });
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            };
        }

        @Override
        public Input<TransactionApplicationEvents, IOException> transactionsBefore( final long beforeTimestamp, final long maxTransactions )
        {
            return new Input<TransactionApplicationEvents, IOException>()
            {
                @Override
                public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super TransactionApplicationEvents, ReceiverThrowableType> output )
                    throws IOException, ReceiverThrowableType
                {
                    // Lock store first
                    lock.lock();
                    try
                    {
                        output.receiveFrom( new Sender<TransactionApplicationEvents, IOException>()
                        {
                            @Override
                            public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super TransactionApplicationEvents, ReceiverThrowableType> receiver )
                                throws ReceiverThrowableType, IOException
                            {
                                Iterator<TransactionApplicationEvents> iterator = store.descendingIterator();

                                long count = 0;

                                while( iterator.hasNext() && count < maxTransactions )
                                {
                                    TransactionApplicationEvents next = iterator.next();
                                    if( next.timestamp().get() < beforeTimestamp )
                                    {
                                        receiver.receive( next );
                                        count++;
                                    }
                                }
                            }
                        });
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            };
        }

        @Override
        protected void storeEvents( TransactionApplicationEvents transactionDomain ) throws IOException
        {
            store.add( transactionDomain );
        }
    }
}
