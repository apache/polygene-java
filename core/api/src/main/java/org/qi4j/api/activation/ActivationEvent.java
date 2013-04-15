/*
 * Copyright (c) 2011, Rickard Öberg.
 * Copyright (c) 2012, Niclas Hedhman.
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
package org.qi4j.api.activation;

/**
 * ActivationEvents are fired during activation and passivation of instances in Qi4j.
 */
public final class ActivationEvent
{
    public enum EventType
    {
        ACTIVATING, ACTIVATED, PASSIVATING, PASSIVATED
    }

    private long timestamp;
    private Object source;
    private EventType type;

    public ActivationEvent( Object source, EventType type )
    {
        this.timestamp = System.currentTimeMillis();
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
        return message();
    }
}