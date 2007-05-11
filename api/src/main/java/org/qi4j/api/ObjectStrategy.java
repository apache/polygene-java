/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api;

import org.qi4j.api.annotation.ImplementedBy;
import iop.runtime.ObjectStrategyDelegatorImpl;

/**
 * This factory creates proxies that implement the given
 * binding interfaces.
 */
@ImplementedBy( ObjectStrategyDelegatorImpl.class )
public interface ObjectStrategy
{
    /**
     * Create a new object that implements the given interface.
     *
     * @param anObjectType an interface that describes the object to be created
     * @return a new proxy object implementing the interface
     */
    <T> T newInstance( Class<T> anObjectType );

    /**
     * Create a new object that implements the given interface.
     * <p/>
     * The new object wraps the current object which provides mixin implementations
     * that should be reused for this new object.
     *
     * @param anObjectType an interface that describes the object to be created
     * @return a new proxy object implementing the interface
     */
    <T> T cast( Class<T> anObjectType );

    /**
     * Checks if the object can be cast() to the provided object type.
     *
     * @param anObjectType The object type we want to check the assignability to for this object.
     * @return true if a cast() is possible of this object to the provided object type.
     */
    boolean isInstance( Class anObjectType );
}
