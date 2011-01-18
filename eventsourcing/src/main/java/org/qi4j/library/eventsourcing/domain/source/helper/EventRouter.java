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

package org.qi4j.library.eventsourcing.domain.source.helper;

import org.qi4j.api.specification.Specification;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.source.EventVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Event handling router. Add specification->visitor routes. When an event comes in
 * the router will ask each specification if it matches, and if so, delegate to the
 * visitor and return whether it successfully handled it or not. If no routes match,
 * returns true.
 */
public class EventRouter
        implements EventVisitor
{
    Map<Specification<DomainEventValue>, EventVisitor> domainEventRoutes = new HashMap<Specification<DomainEventValue>, EventVisitor>();

    public EventRouter route( Specification<DomainEventValue> specification, EventVisitor visitor )
    {
        domainEventRoutes.put( specification, visitor );

        return this;
    }

    /**
     * Route an eventValue to a visitor whose specification matches it. If no
     * specification matches, then return true. Otherwise return the status
     * of the visitor that was matched.
     *
     * @param eventValue the eventValue
     * @return true if eventValue was handled successfully.
     */
    public boolean visit( DomainEventValue eventValue )
    {
        for (Specification<DomainEventValue> specification : domainEventRoutes.keySet())
        {
            if (specification.satisfiedBy( eventValue ))
            {
                EventVisitor eventVisitor = domainEventRoutes.get( specification );
                return eventVisitor.visit( eventValue );
            }
        }

        return true;
    }
}
