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

import java.io.InputStream;
import org.qi4j.api.type.ValueType;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;

/**
 * Use a ValueDeserializer to create new values instances from serialized state.
 *
 * <p>
 *     Serialized state must be one of:
 * </p>
 * <ul>
 *     <li>a ValueComposite,</li>
 *     <li>an EntityReference,</li>
 *     <li>a Collection,</li>
 *     <li>a Map,</li>
 *     <li>a Plain Value.</li>
 * </ul>
 * <p>
 *     Nested plain values, EntityReferences, Collections, Maps, ValueComposites are supported.
 *     EntityReferences are deserialized as their identity string.
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
 *     <li>Date,</li>
 *     <li>DateTime (JodaTime),</li>
 *     <li>LocalDateTime (JodaTime),</li>
 *     <li>LocalDate (JodaTime).</li>
 * </ul>
 * <p>
 *     Values of unknown types and all arrays are considered as {@link java.io.Serializable} and by so are deserialized
 *     from base64 encoded bytes using pure Java serialization. If it happens that the input is invalid, a
 *     ValueSerializationException is thrown.
 * </p>
 * <p>
 *     Having type information in the serialized payload allows to keep actual ValueComposite types and by so
 *     circumvent {@link AmbiguousTypeException} when deserializing.
 * </p>
 */
public interface ValueDeserializer
{

    /**
     * Factory method for a typed deserialize function.
     *
     * <p>The returned Function may throw {@link ValueSerializationException}.</p>
     *
     * @param type the value type
     * @param <T> the parametrized function return type
     * @return a deserialization function
     */
    <T> Function<String, T> deserialize( Class<T> type );

    /**
     * Factory method for a typed deserialize function.
     *
     * <p>The returned Function may throw {@link ValueSerializationException}.</p>
     *
     * @param valueType the value type
     * @param <T> the parametrized function return type
     * @return a deserialization function
     */
    <T> Function<String, T> deserialize( ValueType valueType );

    /**
     * Factory method for an untyped deserialize function.
     *
     * <p>The returned Function may throw {@link ValueSerializationException}.</p>
     *
     * @param <T> the parametrized function return type
     * @return a deserialization function
     */
    <T> Function2<ValueType, String, T> deserialize();

    /**
     * Deserialize a value from a state.
     *
     * @param <T> the parametrized returned type
     * @param type the value type
     * @param input the state
     * @return the value
     * @throws ValueSerializationException if the deserialization failed
     */
    <T> T deserialize( Class<?> type, String input )
        throws ValueSerializationException;

    /**
     * Deserialize a value from a state.
     *
     * @param <T> the parametrized returned type
     * @param valueType the value type
     * @param input the state
     * @return the value
     * @throws ValueSerializationException if the deserialization failed
     */
    <T> T deserialize( ValueType valueType, String input )
        throws ValueSerializationException;

    /**
     * Deserialize a value from a state.
     *
     * @param <T> the parametrized returned type
     * @param type the value type
     * @param input the state stream
     * @return the value
     * @throws ValueSerializationException if the deserialization failed
     */
    <T> T deserialize( Class<?> type, InputStream input )
        throws ValueSerializationException;

    /**
     * Deserialize a value from a state.
     *
     * @param <T> the parametrized returned type
     * @param valueType the value type
     * @param input the state stream
     * @return the value
     * @throws ValueSerializationException if the deserialization failed
     */
    <T> T deserialize( ValueType valueType, InputStream input )
        throws ValueSerializationException;
}
