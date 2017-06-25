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
package org.apache.polygene.api.composite;

import org.apache.polygene.api.common.ConstructionException;

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
     * @param <T> Transient type
     * @param mixinType an interface that describes the TransientComposite to be instantiated
     *
     * @return a TransientBuilder for creation of TransientComposites implementing the interface
     *
     * @throws NoSuchTransientTypeException if no composite extending the mixinType has been registered
     */
    <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientTypeException;

    /**
     * Instantiate a TransientComposite of the given type.
     *
     * @param <T> Transient type
     * @param mixinType the TransientComposite type to instantiate
     * @param uses The objects that can be injected into mixins
     *
     * @return a new TransientComposite instance
     *
     * @throws NoSuchTransientTypeException if no composite extending the mixinType has been registered
     * @throws org.apache.polygene.api.common.ConstructionException
     *                                  if the composite could not be instantiated
     */
    <T> T newTransient( Class<T> mixinType, Object... uses )
        throws NoSuchTransientTypeException, ConstructionException;
}
