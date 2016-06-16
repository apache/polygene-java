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
package org.apache.zest.api.value;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.zest.api.composite.AmbiguousTypeException;

/**
 * Use a ValueSerializer to serialize values state.
 *
 * <p>
 *     Serialized object must be one of:
 * </p>
 * <ul>
 *     <li>a ValueComposite,</li>
 *     <li>an EntityComposite or EntityReference,</li>
 *     <li>an Iterable,</li>
 *     <li>a Map,</li>
 *     <li>a Plain Value.</li>
 * </ul>
 * <p>
 *     Nested plain values, EntityReferences, Iterables, Maps, ValueComposites and EntityComposites are supported.
 *     EntityComposites and EntityReferences are serialized as their identity string.
 * </p>
 * <p>
 *     Plain values can be one of:
 * </p>
 * <ul>
 *     <li>String,</li>
 *     <li>Character or char,</li>
 *     <li>Boolean or boolean,</li>
 *     <li>Integer or int,</li>
 *     <li>Long or long,</li>
 *     <li>Short or short,</li>
 *     <li>Byte or byte,</li>
 *     <li>Float or float,</li>
 *     <li>Double or double,</li>
 *     <li>BigInteger,</li>
 *     <li>BigDecimal,</li>
 *     <li>ZonedDateTime</li>
 *     <li>OffsetDateTime</li>
 *     <li>LocalDateTime</li>
 *     <li>LocalDate</li>
 *     <li>LocalTime</li>
 *     <li>Duration</li>
 *     <li>Period</li>
 *     <li>Instant</li>
 * </ul>
 * <p>
 *     Values of unknown types and all arrays are considered as {@link java.io.Serializable} and by so are serialized to
 *     base64 encoded bytes using pure Java serialization. If it happens that the value is not Serializable, a
 *     ValueSerializationException is thrown.
 * </p>
 * <p>
 *     Having type information in the serialized payload allows to keep actual ValueComposite types and by so
 *     circumvent {@link AmbiguousTypeException} when deserializing.
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
     * @param options ValueSerializer Options
     * @return a serialization function.
     */
    <T> Function<T, String> serialize( Options options );

    /**
     * Serialize the state of a value with type information.
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
     * @param options ValueSerializer Options
     * @param object an Object to serialize
     * @return the state
     * @throws ValueSerializationException if the Value serialization failed
     */
    String serialize( Options options, Object object )
        throws ValueSerializationException;

    /**
     * Serialize the state of a value with type information.
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
     * @param options ValueSerializer Options
     * @param object an Object to serialize
     * @param output that will be used as output
     * @throws ValueSerializationException if the Value serialization failed
     */
    void serialize( Options options, Object object, OutputStream output )
        throws ValueSerializationException;

    /**
     * Serialization options.
     */
    final class Options
    {
        public final boolean includeTypeInfo;

        public Options( boolean includeTypeInfo ){
            this.includeTypeInfo = includeTypeInfo;
        }
    }
}
