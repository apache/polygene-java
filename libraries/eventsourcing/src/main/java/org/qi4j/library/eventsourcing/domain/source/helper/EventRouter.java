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

package org.qi4j.library.eventsourcing.domain.source.helper;

import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.functional.Specification;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

/**
 * Event handling router. Add specification->receiver routes. When an event comes in
 * the router will ask each specification if it matches, and if so, delegate to the
 * receiver and return whether it successfully handled it or not. If no routes match,
 * delegate to the default receiver
 */
public class EventRouter<T extends Throwable>
        implements Output<DomainEventValue, T>, Receiver<UnitOfWorkDomainEventsValue, T>
{
    private Map<Specification<DomainEventValue>, Receiver<DomainEventValue, T>> routeEvent = new LinkedHashMap<Specification<DomainEventValue>, Receiver<DomainEventValue, T>>();

    private Receiver<DomainEventValue, T> defaultReceiver = new Receiver<DomainEventValue, T>()
    {
        @Override
        public void receive( DomainEventValue item ) throws T
        {
            // Do nothing;
        }
    };

    public EventRouter route( Specification<DomainEventValue> specification, Receiver<DomainEventValue, T> receiver )
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
                for( Map.Entry<Specification<DomainEventValue>, Receiver<DomainEventValue, T>> specificationReceiverEntry : routeEvent.entrySet() )
                {
                    if( specificationReceiverEntry.getKey().satisfiedBy( item ) )
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
