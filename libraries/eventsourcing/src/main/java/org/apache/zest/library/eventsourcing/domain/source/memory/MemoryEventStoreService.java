/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.library.eventsourcing.domain.source.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.library.eventsourcing.domain.source.AbstractEventStoreMixin;
import org.apache.zest.library.eventsourcing.domain.source.EventSource;
import org.apache.zest.library.eventsourcing.domain.source.EventStore;
import org.apache.zest.library.eventsourcing.domain.source.EventStoreActivation;
import org.apache.zest.library.eventsourcing.domain.source.EventStream;

/**
 * In-Memory EventStore. Mainly used for testing.
 */
@Mixins(MemoryEventStoreService.MemoryEventStoreMixin.class)
@Activators( EventStoreActivation.Activator.class )
public interface MemoryEventStoreService
        extends EventSource, EventStore, EventStream, EventStoreActivation, ServiceComposite
{
    abstract class MemoryEventStoreMixin
            extends AbstractEventStoreMixin
            implements EventSource, EventStoreActivation
    {
        // This list holds all transactions
        private LinkedList<UnitOfWorkDomainEventsValue> store = new LinkedList<UnitOfWorkDomainEventsValue>();

        private long currentCount = 0;

        public Input<UnitOfWorkDomainEventsValue, IOException> events( final long offset, final long limit )
        {
            if (offset < 0 || offset > count())
                throw new IllegalArgumentException( "Offset must be between 0 and current number of events in the store" );

            if (limit <= 0 )
                throw new IllegalArgumentException( "Limit must be above 0" );

            return new Input<UnitOfWorkDomainEventsValue, IOException>()
            {
                @Override
                public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> output ) throws IOException, ReceiverThrowableType
                {
                    // Lock store first
                    lock.lock();
                    try
                    {
                        output.receiveFrom( new Sender<UnitOfWorkDomainEventsValue, IOException>()
                        {
                            @Override
                            public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, IOException
                            {
                                ListIterator<UnitOfWorkDomainEventsValue> iterator = store.listIterator( (int) offset );

                                long count = 0;

                                while( iterator.hasNext() && count < limit )
                                {
                                    UnitOfWorkDomainEventsValue next = iterator.next();
                                    receiver.receive( next );
                                    count++;
                                }
                            }
                        } );
                    } finally
                    {
                        lock.unlock();
                    }
                }
            };
        }

        @Override
        public long count()
        {
            return currentCount;
        }

        @Override
        protected Output<UnitOfWorkDomainEventsValue, IOException> storeEvents0()
        {
            return new Output<UnitOfWorkDomainEventsValue, IOException>()
            {
                @Override
                public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends UnitOfWorkDomainEventsValue, SenderThrowableType> sender ) throws IOException, SenderThrowableType
                {
                    final List<UnitOfWorkDomainEventsValue> newEvents = new ArrayList<UnitOfWorkDomainEventsValue>(  );
                    sender.sendTo( new Receiver<UnitOfWorkDomainEventsValue, IOException>()
                    {
                        @Override
                        public void receive( UnitOfWorkDomainEventsValue item ) throws IOException
                        {
                            newEvents.add( item );
                        }
                    });
                    store.addAll( newEvents );
                    currentCount += newEvents.size();
                }
            };
        }
    }
}