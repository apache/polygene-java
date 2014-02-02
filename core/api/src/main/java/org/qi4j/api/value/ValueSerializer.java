/*
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
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
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.functional.Function;

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
 *     <li>Date,</li>
 *     <li>DateTime (JodaTime),</li>
 *     <li>LocalDateTime (JodaTime),</li>
 *     <li>LocalDate (JodaTime).</li>
 *     <li>Money (JodaMoney).</li>
 *     <li>BigMoney (JodaMoney).</li>
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
     * Factory method for a serialize function.
     *
     * @param <T> the parametrized function input type
     * @param includeTypeInfo if type information should be included in the output
     * @return a serialization function.
     */
    @Deprecated
    <T> Function<T, String> serialize( boolean includeTypeInfo );

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
     * Serialize the state of a value.
     *
     * @param object an Object to serialize
     * @param includeTypeInfo if type information should be included in the output
     * @return the state
     * @throws ValueSerializationException if the Value serialization failed
     */
    @Deprecated
    String serialize( Object object, boolean includeTypeInfo )
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
     * Serialize the state of a value.
     *
     * @param object an Object to serialize
     * @param output that will be used as output
     * @param includeTypeInfo if type information should be included in the output
     * @throws ValueSerializationException if the Value serialization failed
     */
    @Deprecated
    void serialize( Object object, OutputStream output, boolean includeTypeInfo )
        throws ValueSerializationException;

    /**
     * Serialization options.
     */
    final class Options
    {
        /**
         * Boolean flag to include type information.
         * Default to TRUE.
         */
        public static final String INCLUDE_TYPE_INFO = "includeTypeInfo";
        private final Map<String, String> options = new HashMap<>();

        /**
         * Create new default ValueSerializer Options.
         */
        public Options()
        {
            options.put( INCLUDE_TYPE_INFO, "true" );
        }

        /**
         * Create new ValueSerializer Options from given Map.
         * @param options Options to copy in the new Options instance
         */
        public Options( Map<String, String> options )
        {
            this();
            options.putAll( options );
        }

        /**
         * Set {@link #INCLUDE_TYPE_INFO} option to TRUE.
         * @return This
         */
        public Options withTypeInfo()
        {
            return put( INCLUDE_TYPE_INFO, true );
        }

        /**
         * Set {@link #INCLUDE_TYPE_INFO} option to FALSE.
         * @return This
         */
        public Options withoutTypeInfo()
        {
            return put( INCLUDE_TYPE_INFO, false );
        }

        /**
         * Get Boolean option value.
         * @param option The option
         * @return The boolean value of the option, or null if absent
         */
        public Boolean getBoolean( String option )
        {
            if( !options.containsKey( option ) )
            {
                return null;
            }
            return Boolean.valueOf( options.get( option ) );
        }

        /**
         * Get Integer option value.
         * @param option The option
         * @return The integer value of the option, or null if absent
         */
        public Integer getInteger( String option )
        {
            if( !options.containsKey( option ) )
            {
                return null;
            }
            return Integer.valueOf( options.get( option ) );
        }

        /**
         * Get String option value.
         * @param option The option
         * @return The string value of the option, or null if absent
         */
        public String getString( String option )
        {
            return options.get( option );
        }

        /**
         * Put an option String value.
         * @param option The option
         * @param value The value
         * @return This Options instance
         */
        public Options put( String option, String value )
        {
            if( value == null )
            {
                return remove( option );
            }
            options.put( option, value );
            return this;
        }

        /**
         * Put an option boolean value.
         * @param option The option
         * @param value The value
         * @return This Options instance
         */
        public Options put( String option, Boolean value )
        {
            if( value == null )
            {
                return remove( option );
            }
            options.put( option, Boolean.toString( value ) );
            return this;
        }

        /**
         * Put an option Integer value.
         * @param option The option
         * @param value The value
         * @return This Options instance
         */
        public Options put( String option, Integer value )
        {
            if( value == null )
            {
                return remove( option );
            }
            options.put( option, value.toString() );
            return this;
        }

        /**
         * Remove an option value.
         * @param option The option
         * @return This Options instance
         */
        public Options remove( String option )
        {
            options.remove( option );
            return this;
        }

        /**
         * Get all defined options as a Map.
         * @return All defined options in a new Map
         */
        public Map<String, String> toMap()
        {
            return new HashMap<>( options );
        }
    }

}
