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

package org.qi4j.library.eventsourcing.domain.api;

/**
 * This gives access to the current domain event. This is only usable within methods
 * marked with &#64;DomainEvent annotation.
 */
public class DomainEvents
{
    private static ThreadLocal<DomainEventValue> event = new ThreadLocal<DomainEventValue>();

    public static DomainEventValue currentEvent()
    {
        return event.get();
    }

    /**
     * This is called by the EventSourcing library, either during creation or replay.
     * Don't use in application code.
     *
     * @param currentEvent new current event
     */
    public static void setCurrentEvent(DomainEventValue currentEvent)
    {
        event.set( currentEvent );
    }
}
