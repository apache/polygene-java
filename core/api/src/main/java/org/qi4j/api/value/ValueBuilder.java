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

package org.qi4j.api.value;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.ConstructionException;

/**
 * Builder for Values.
 */
public interface ValueBuilder<T>
{
    AssociationStateHolder state();

    /**
     * Get a representation of the state for the new Value.
     * It is possible to access and update properties and associations,
     * even immutable ones since the builder represents the initial state.
     *
     * @return a mutable instance of the Value type
     */
    T prototype();

    /**
     * Get a representation of the state of the given type for the new ValueComposite.
     * This is primarily used if you want to provide state for a private mixin type.
     *
     * @param mixinType the mixin which you want to provide state for
     *
     * @return a proxy implementing the given mixin type
     */
    <K> K prototypeFor( Class<K> mixinType );

    /**
     * Create a new Composite instance.
     *
     * @return a new Composite instance
     *
     * @throws org.qi4j.api.common.ConstructionException
     *          thrown if it was not possible to instantiate the Composite
     */
    T newInstance()
        throws ConstructionException;
}
