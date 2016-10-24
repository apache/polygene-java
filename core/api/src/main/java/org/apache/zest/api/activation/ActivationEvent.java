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
package org.apache.zest.api.activation;

import java.time.Instant;
import org.apache.zest.api.time.SystemTime;

/**
 * ActivationEvents are fired during activation and passivation of instances in Zest.
 */
public final class ActivationEvent
{
    public enum EventType
    {
        ACTIVATING, ACTIVATED, PASSIVATING, PASSIVATED
    }

    private final Instant timestamp;
    private final Object source;
    private final EventType type;

    public ActivationEvent( Object source, EventType type )
    {
        this.timestamp = SystemTime.now();
        this.source = source;
        this.type = type;
    }

    /**
     * @return the source of the Activation event
     */
    public Object source()
    {
        return source;
    }

    /**
     * @return the type of the Activation event
     */
    public EventType type()
    {
        return type;
    }


    /**
     *
     * @return The instant that this event was created.
     */
    public Instant eventTime()
    {
        return timestamp;
    }

    /**
     * @return an informative message describing the event
     */
    public String message()
    {
        switch( type )
        {
        case ACTIVATING:
            return "Activating " + source;
        case ACTIVATED:
            return "Activated " + source;
        case PASSIVATING:
            return "Passivating " + source;
        case PASSIVATED:
            return "Passivated " + source;
        }
        return "";
    }

    /**
     * @see #message()
     */
    @Override
    public String toString()
    {
        return message() + "@" + timestamp;
    }
}