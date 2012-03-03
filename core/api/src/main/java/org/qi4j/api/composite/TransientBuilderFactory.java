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
package org.qi4j.api.composite;

import org.qi4j.api.common.ConstructionException;

/**
 * This factory creates TransientComposites and the TransientBuilders.
 *
 * TransientComposite instances are very flexible in what it can reference, but are restricted in where they
 * can be used. So, TransientComposites are mainly recommended where Values, Entities and Services can not be used,
 * but they can also not be used to store state, be serialized across a network or have automatic equals/hashCode
 * calculations.
 */
public interface TransientBuilderFactory
{
    /**
     * Create a builder for creating new TransientComposites that implements the given TransientComposite type.
     *
     * @param mixinType an interface that describes the TransientComposite to be instantiated
     *
     * @return a TransientBuilder for creation of TransientComposites implementing the interface
     *
     * @throws NoSuchTransientException if no composite extending the mixinType has been registered
     */
    <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientException;

    /**
     * Instantiate a TransientComposite of the given type.
     *
     * @param mixinType the TransientComposite type to instantiate
     *
     * @return a new TransientComposite instance
     *
     * @throws NoSuchTransientException if no composite extending the mixinType has been registered
     * @throws org.qi4j.api.common.ConstructionException
     *                                  if the composite could not be instantiated
     */
    <T> T newTransient( Class<T> mixinType, Object... uses )
        throws NoSuchTransientException, ConstructionException;
}
