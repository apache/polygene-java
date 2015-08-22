/**
 *
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

package org.apache.zest.library.eventsourcing.domain.source.helper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

/**
 * Event handling router. Add specification-&gt;receiver routes. When an event comes in
 * the router will ask each specification if it matches, and if so, delegate to the
 * receiver and return whether it successfully handled it or not. If no routes match,
 * delegate to the default receiver
 */
public class EventRouter<T extends Throwable>
        implements Output<DomainEventValue, T>, Receiver<UnitOfWorkDomainEventsValue, T>
{
    private Map<Predicate<DomainEventValue>, Receiver<DomainEventValue, T>> routeEvent = new LinkedHashMap<Predicate<DomainEventValue>, Receiver<DomainEventValue, T>>();

    private Receiver<DomainEventValue, T> defaultReceiver = new Receiver<DomainEventValue, T>()
    {
        @Override
        public void receive( DomainEventValue item ) throws T
        {
            // Do nothing;
        }
    };

    public EventRouter route( Predicate<DomainEventValue> specification, Receiver<DomainEventValue, T> receiver )
    {
        routeEvent.put( specification, receiver );

        return this;
    }

    public EventRouter defaultReceiver( Receiver<DomainEventValue, T> defaultReceiver )
    {
        this.defaultReceiver = defaultReceiver;
        return this;
    }

    @Override
    public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends DomainEventValue, SenderThrowableType> sender ) throws T, SenderThrowableType
    {
        sender.sendTo( new Receiver<DomainEventValue, T>()
        {
            @Override
            public void receive( DomainEventValue item ) throws T
            {
                for( Map.Entry<Predicate<DomainEventValue>, Receiver<DomainEventValue, T>> specificationReceiverEntry : routeEvent.entrySet() )
                {
                    if( specificationReceiverEntry.getKey().test( item ) )
                    {
                        specificationReceiverEntry.getValue().receive( item );
                        return;
                    }
                }

                // No match, use default
                defaultReceiver.receive( item );
            }
        } );
    }

    @Override
    public void receive( final UnitOfWorkDomainEventsValue item ) throws T
    {
        receiveFrom( new Sender<DomainEventValue, T>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super DomainEventValue, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, T
            {

                for( DomainEventValue domainEventValue : item.events().get() )
                {
                    receiver.receive( domainEventValue );
                }
            }
        } );
    }
}
