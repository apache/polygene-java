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

package org.apache.zest.library.eventsourcing.domain.factory;

import java.lang.reflect.Method;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.concern.GenericConcern;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.library.eventsourcing.domain.api.DomainEvent;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.DomainEvents;

/**
 * Generate event for event method
 */
@AppliesTo(DomainEvent.class)
public class DomainEventCreationConcern
        extends GenericConcern
{
    @This
    private EntityComposite entity;

    @Service
    private DomainEventFactory domainEventFactory;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        if (DomainEvents.currentEvent() == null)
        {
            // Create eventValue
            DomainEventValue eventValue = domainEventFactory.createEvent( entity, method.getName(), args );
            DomainEvents.setCurrentEvent( eventValue );
            try
            {
                return next.invoke( proxy, method, args );
            } finally
            {
                DomainEvents.setCurrentEvent( null );
            }

        } else
        {
            // This is probably a replay call
            return next.invoke( proxy, method, args );
        }
    }
}