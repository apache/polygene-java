/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore.map;

import java.io.Serializable;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

/**
 * JAVADOC
 */
public final class UnitOfWorkEventsEntry
    implements Serializable
{
    private String identity;
    private long timeStamp;
    private Iterable<UnitOfWorkEvent> events;
    private String previous;

    public UnitOfWorkEventsEntry( String identity, long timeStamp, Iterable<UnitOfWorkEvent> events, String previous )
    {
        this.identity = identity;
        this.timeStamp = timeStamp;
        this.events = events;
        this.previous = previous;
    }

    public String identity()
    {
        return identity;
    }

    public long timeStamp()
    {
        return timeStamp;
    }

    public Iterable<UnitOfWorkEvent> events()
    {
        return events;
    }

    public String previous()
    {
        return previous;
    }
}
