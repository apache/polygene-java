/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.entity;

import org.qi4j.api.common.ConstructionException;

/**
 * EntityBuilders are used to instantiate EntityComposites. They can be acquired from
 * {@link org.qi4j.api.unitofwork.UnitOfWork#newEntityBuilder(Class)} and allows the client
 * to provide additional settings before instantiating the Composite.
 *
 * After calling newInstance() the builder becomes invalid, and may not be called again.
 */
public interface EntityBuilder<T>
{
    /**
     * Get a representation of the state for the new Composite.
     * It is possible to access and update properties and associations,
     * even immutable ones since the builder represents the initial state.
     *
     * @return a proxy implementing the Composite type
     */
    T instance();

    /**
     * Get a representation of the state of the given type for the new Composite.
     * This is primarily used if you want to provide state for a private mixin type.
     *
     * @param mixinType the mixin which you want to provide state for
     *
     * @return a proxy implementing the given mixin type
     */
    <K> K instanceFor( Class<K> mixinType );

    /**
     * Create a new Entity instance.
     *
     * @return a new Entity instance
     *
     * @throws org.qi4j.api.common.ConstructionException
     *                            thrown if it was not possible to instantiate the Composite
     * @throws LifecycleException if the entity could not be created
     */
    T newInstance()
        throws ConstructionException, LifecycleException;
}