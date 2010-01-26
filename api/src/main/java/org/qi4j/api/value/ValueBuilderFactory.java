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

import org.qi4j.api.common.ConstructionException;

/**
 * Factory for Values and ValueBuilders.
 */
public interface ValueBuilderFactory
{
    /**
     * Create a builder for creating new Values that implements the given Value type.
     *
     * @param valueType an interface that describes the Composite to be instantiated
     *
     * @return a ValueBuilder for creation of ValueComposites implementing the interface
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     */
    <T> ValueBuilder<T> newValueBuilder( Class<T> valueType )
        throws NoSuchValueException;

    /**
     * Instantiate a Value of the given type.
     *
     * @param valueType the Value type to instantiate
     *
     * @return a new Value instance
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     * @throws org.qi4j.api.common.ConstructionException
     *                              if the value could not be instantiated
     */
    <T> T newValue( Class<T> valueType )
        throws NoSuchValueException, ConstructionException;

    <T> T newValueFromJSON( Class<T> valueType, String jsonValue );
}
