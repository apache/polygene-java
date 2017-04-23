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
 */
package org.apache.polygene.api.serialization;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.common.Optional;

/**
 * Serializer.
 *
 * All implementations must handle all {@link Options}, they might extend them to provide more control.
 * See their respective documentation for the details.
 */
public interface Serializer
{
    void serialize( Options options, Writer writer, @Optional Object object );

    void serialize( Options options, OutputStream output, @Optional Object object );

    void serialize( Writer writer, @Optional Object object );

    void serialize( OutputStream output, @Optional Object object );

    String serialize( Options options, @Optional Object object );

    String serialize( @Optional Object object );

    <T> Function<T, String> serializeFunction( Options options );

    <T> Function<T, String> serializeFunction();

    Stream<String> serializeEach( Options options, Iterable<Object> objects );

    Stream<String> serializeEach( Iterable<Object> objects );

    Stream<String> serializeEach( Options options, Object... objects );

    Stream<String> serializeEach( Object... objects );

    byte[] toBytes( Options options, @Optional Object object );

    byte[] toBytes( @Optional Object object );

    <T> Function<T, byte[]> toBytesFunction( Options options );

    <T> Function<T, byte[]> toBytesFunction();

    Stream<byte[]> toBytesEach( Options options, Iterable<Object> objects );

    Stream<byte[]> toBytesEach( Iterable<Object> objects );

    Stream<byte[]> toBytesEach( Options options, Object... objects );

    Stream<byte[]> toBytesEach( Object... objects );

    /**
     * State serializer options.
     *
     * Use {@link #builder()} to create new instances.
     *
     * All options provided by the builder are safe to use with all serialization extensions. Serialization extensions
     * might provide more options, see {@link #option(String)} and the respective extension documentation.
     */
    interface Options
    {
        /**
         * Default state serializer options.
         *
         * {@link #rootTypeInfo()} set to {@literal false}.
         * {@link #nestedTypeInfo()} set to {@literal true}.
         */
        Options DEFAULT = builder().withoutRootTypeInfo().withNestedTypeInfo().build();

        /**
         * State serializer options with both {@link #rootTypeInfo()} and {@link #nestedTypeInfo()}
         * set to {@literal false}.
         */
        Options NO_TYPE_INFO = builder().withoutRootTypeInfo().withoutNestedTypeInfo().build();

        /**
         * State serializer options with both {@link #rootTypeInfo()} and {@link #nestedTypeInfo()}
         * set to {@literal true}.
         */
        Options ALL_TYPE_INFO = builder().withRootTypeInfo().withNestedTypeInfo().build();

        boolean rootTypeInfo();

        /**
         * Include type information in the serialized form of nested values.
         *
         * Boolean flag to include type information in the serialized form of nested values types.
         * Each serialization extension is free to chose how to represent this type information.
         *
         * This is enabled by default to allow for polymorphic deserialization.
         *
         * Let's say you have a type hierarchy of values as follows ;
         * <code>
         *     interface Parent { Property&lt;String&gt; something(); }
         *     interface Child1 { Property&lt;Long&gt; number(); }
         *     interface Child2 { Property&lt;Duration&gt; duration(); }
         * </code>
         * and want to serialize and deserialize a set of values like this one ;
         * <code>
         *     interface MyValue { Property&lt;Parent&gt; polymorphicValue(); }
         * </code>
         * that is a view of a value composite that also has the following fragments ;
         * <code>
         *     interface MyValueWithNumber { Property&lt;Child1&gt; polymorphicValue(); }
         *     interface MyValueWithDuration { Property&lt;Child2&gt; polymorphicValue(); }
         * </code>
         * when deserializing, the {@link Deserializer} needs a way to know which specialization
         * type to use to deserialize the value state, {@literal Child1} or {@literal Child2} in our example.
         *
         * If the deserializer can't know it use the type information from the
         * {@link org.apache.polygene.api.type.ValueType} provided at deserialization time.
         *
         * Disable it using {@link Builder#withoutNestedTypeInfo()} if you are sure you don't need this.
         *
         * @return {@literal true} if type information must be included in the serialized form of nested values,
         *         {@literal false} otherwise
         */
        boolean nestedTypeInfo();

        /**
         * Query for an option's value.
         *
         * @param option the option
         * @return the option's value, {@literal} null if absent
         */
        String option( String option );

        /**
         * Create a new builder of {@link Options}
         *
         * @return a new builder
         */
        static Builder builder()
        {
            return new Builder();
        }

        /**
         * Builder for {@link Options}.
         *
         * This builder is mutable, built instances are not.
         */
        final class Builder
        {
            private static final String ROOT_TYPE_INFO = "rootTypeInfo";
            private static final String NESTED_TYPE_INFO = "nestedTypeInfo";

            private static class Instance implements Options
            {
                private final Map<String, String> options;

                private Instance( Map<String, String> options )
                {
                    this.options = options;
                }

                @Override
                public boolean rootTypeInfo()
                {
                    return "true".equals( options.get( ROOT_TYPE_INFO ) );
                }

                @Override
                public boolean nestedTypeInfo()
                {
                    return "true".equals( options.get( NESTED_TYPE_INFO ) );
                }

                @Override
                public String option( String option )
                {
                    return options.get( option );
                }
            }

            private final Map<String, String> options = new HashMap<String, String>()
            {{
                put( ROOT_TYPE_INFO, "false" );
                put( NESTED_TYPE_INFO, "true" );
            }};

            public Builder withRootTypeInfo()
            {
                return withOption( ROOT_TYPE_INFO, "true" );
            }

            public Builder withoutRootTypeInfo()
            {
                return withOption( ROOT_TYPE_INFO, "false" );
            }

            /**
             * Include type information in the serialized form of nested values.
             *
             * @return this builder
             */
            public Builder withNestedTypeInfo()
            {
                return withOption( NESTED_TYPE_INFO, "true" );
            }

            /**
             * Do not include type information in the serialized form of nested values.
             *
             * <strong>WARNING</strong>
             * Without this, {@link Deserializer}s will use the provided
             * {@link org.apache.polygene.api.type.ValueType} for instantiation potentially breaking polymorphism,
             * see {@link Options#nestedTypeInfo()}.
             *
             * @return this builder
             * @see Builder#withNestedTypeInfo()
             */
            public Builder withoutNestedTypeInfo()
            {
                return withOption( NESTED_TYPE_INFO, "false" );
            }

            /**
             * Set extension specific option.
             *
             * @param option the option to add
             * @param value it's value
             * @return this builder
             */
            public Builder withOption( String option, String value )
            {
                options.put( option, value );
                return this;
            }

            /**
             * Remove extension specific option.
             *
             * @param option the option to remove
             * @return this builder
             */
            public Builder withoutOption( String option )
            {
                options.remove( option );
                return this;
            }

            /**
             * Build the options.
             *
             * @return a new immutable instance of {@link Options}.
             */
            public Options build()
            {
                return new Instance( Collections.unmodifiableMap( options ) );
            }
        }
    }
}
