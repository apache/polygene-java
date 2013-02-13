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

import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.functional.Function;

/**
 * Factory for Values and ValueBuilders.
 */
public interface ValueBuilderFactory
{

    /**
     * Instantiate a Value of the given type.
     *
     * @param valueType the Value type to instantiate
     *
     * @return a new Value instance
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     * @throws ConstructionException if the value could not be instantiated
     */
    <T> T newValue( Class<T> valueType )
        throws NoSuchValueException, ConstructionException;

    /**
     * Create a builder for creating new Values that implements the given Value type.
     * <p>The returned ValueBuilder can be reused to create several Values instances.</p>
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
     * Create a builder for creating a new Value starting with the given prototype.
     * <p>The returned ValueBuilder can only be used ONCE.</p>
     *
     * @param prototype a prototype the builder will use
     *
     * @return a ValueBuilder for creation of ValueComposites implementing the interface of the prototype
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     */
    <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype );

    /**
     * Create a builder for creating a new Value starting with the given state.
     * <p>The returned ValueBuilder can only be used ONCE.</p>
     *
     * @param mixinType an interface that describes the Composite to be instantiated
     * @param propertyFunction a function providing the state of properties
     * @param associationFunction a function providing the state of associations
     * @param manyAssociationFunction a function providing the state of many associations
     *
     * @return a ValueBuilder for creation of ValueComposites implementing the interface
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     */
    <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                  Function<PropertyDescriptor, Object> propertyFunction,
                                                  Function<AssociationDescriptor, EntityReference> associationFunction,
                                                  Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction );

    /**
     * Instantiate a Value of the given type using the serialized state given as String.
     *
     * @param valueType the Value type to instantiate
     * @param serializedState  the state of the Value
     *
     * @return a new Value instance
     *
     * @throws NoSuchValueException if no value extending the mixinType has been registered
     * @throws ConstructionException if the value could not be instantiated
     */
    <T> T newValueFromSerializedState( Class<T> valueType, String serializedState );

}
