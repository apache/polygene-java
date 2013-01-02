/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.value;

import java.io.OutputStream;
import org.qi4j.functional.Function;

/**
 * Use a ValueSerializer to serialize values state.
 *
 * <p>
 *     Serialized object must be one of a ValueComposite, a Collection or a Map.
 * </p>
 * <p>
 *     Nested plain values, EntityReferences, Collections, Maps, ValueComposites and Entities are supported.
 *     EntityComposites are serialized as EntityReferences.
 * </p>
 */
public interface ValueSerializer
{

    /**
     * Factory method for a serialize function.
     *
     * @param <T> the parametrized function input type
     * @return a serialization function.
     */
    <T> Function<T, String> serialize();

    /**
     * Factory method for a serialize function.
     *
     * @param <T> the parametrized function input type
     * @param includeTypeInfo if type information should be included in the output
     * @return a serialization function.
     */
    <T> Function<T, String> serialize( boolean includeTypeInfo );

    /**
     * Serialize the state of a value without type information.
     *
     * @param object an Object to serialize
     * @return the state
     * @throws ValueSerializationException if the Value serialization failed
     */
    String serialize( Object object )
        throws ValueSerializationException;

    /**
     * Serialize the state of a value.
     *
     * @param object an Object to serialize
     * @param includeTypeInfo if type information should be included in the output
     * @return the state
     * @throws ValueSerializationException if the Value serialization failed
     */
    String serialize( Object object, boolean includeTypeInfo )
        throws ValueSerializationException;

    /**
     * Serialize the state of a value without type information.
     *
     * @param object an Object to serialize
     * @param output that will be used as output
     * @throws ValueSerializationException if the Value serialization failed
     */
    void serialize( Object object, OutputStream output )
        throws ValueSerializationException;

    /**
     * Serialize the state of a value.
     *
     * @param object an Object to serialize
     * @param output that will be used as output
     * @param includeTypeInfo if type information should be included in the output
     * @throws ValueSerializationException if the Value serialization failed
     */
    void serialize( Object object, OutputStream output, boolean includeTypeInfo )
        throws ValueSerializationException;
}
