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
package org.apache.polygene.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Implementations of {@link Collector} missing from the JDK.
 */
public class Collectors
{
    /**
     * Collect a single element.
     *
     * The Collector throws {@link IllegalArgumentException} if no or more than one element.
     *
     * @param <T> Element type
     * @return The single element collector
     */
    public static <T>
    Collector<T, ?, T> single()
        throws IllegalArgumentException
    {
        Supplier<T> thrower = () ->
        {
            throw new IllegalArgumentException( "No or more than one element in stream" );
        };
        return java.util.stream.Collectors.collectingAndThen( java.util.stream.Collectors.reducing( ( a, b ) -> null ),
                                                              optional -> optional.orElseGet( thrower ) );
    }

    /**
     * Eventually collect a single element.
     *
     * The Collector throws {@link IllegalArgumentException} if more than one element.
     *
     * @param <T> Element type
     * @return The optional single element collector
     */
    public static <T>
    Collector<T, ?, Optional<T>> singleOrEmpty()
    {
        return java.util.stream.Collectors.reducing(
            ( left, right ) ->
            {
                if( left != null && right != null )
                {
                    throw new IllegalArgumentException( "More than one element in stream" );
                }
                if( left != null )
                {
                    return left;
                }
                return right;
            } );
    }

    /**
     * Collect map entries into a {@link HashMap}.
     *
     * The Collector throws {@link NullPointerException} if one entry has a {@literal null} value.
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @return a {@code Collector} which collects elements into a {@code Map}
     */
    public static <T extends Map.Entry<K, U>, K, U>
    Collector<T, ?, Map<K, U>> toMap()
    {
        return toMap( Map.Entry::getKey, Map.Entry::getValue, HashMap::new );
    }

    /**
     * Collect map entries into a map.
     *
     * The Collector throws {@link NullPointerException} if one entry has a {@literal null} value.
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * @param <M> the type of the resulting {@code Map}
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @param mapSupplier a function which returns a new, empty {@code Map} into
     *                    which the results will be inserted
     * @return The map collector
     */
    public static <T extends Map.Entry<K, U>, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMap( Supplier<M> mapSupplier )
    {
        return toMap( Map.Entry::getKey, Map.Entry::getValue, mapSupplier );
    }

    /**
     * Collect map entries into a map.
     *
     * The Collector throws {@link NullPointerException} if one entry has a {@literal null} value.
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * @param <M> the type of the resulting {@code Map}
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mapSupplier a function which returns a new, empty {@code Map} into
     *                    which the results will be inserted
     * @return The map collector
     */
    public static <T, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMap( Function<? super T, ? extends K> keyMapper,
                              Function<? super T, ? extends U> valueMapper,
                              Supplier<M> mapSupplier )
    {
        return java.util.stream.Collectors.toMap( keyMapper,
                                                  valueMapper,
                                                  throwingMerger(),
                                                  mapSupplier );
    }

    /**
     * Collect map entries into a {@link HashMap}, allowing null values.
     *
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * See https://bugs.openjdk.java.net/browse/JDK-8148463
     *
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @return The map collector
     */
    public static <T extends Map.Entry<K, U>, K, U>
    Collector<T, ?, Map<K, U>> toMapWithNullValues()
    {
        return toMapWithNullValues( Map.Entry::getKey, Map.Entry::getValue, HashMap::new );
    }

    /**
     * Collect map entries into a map, allowing null values.
     *
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * See https://bugs.openjdk.java.net/browse/JDK-8148463
     *
     * @param <M> the type of the resulting {@code Map}
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @param mapSupplier a function which returns a new, empty {@code Map} into
     *                    which the results will be inserted
     * @return The map collector
     */
    public static <T extends Map.Entry<K, U>, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMapWithNullValues( Supplier<M> mapSupplier )
    {
        return toMapWithNullValues( Map.Entry::getKey, Map.Entry::getValue, mapSupplier );
    }

    /**
     * Collect map entries into a map, allowing null values.
     *
     * The Collector throws {@link IllegalStateException} if duplicate keys are found.
     *
     * See https://bugs.openjdk.java.net/browse/JDK-8148463
     *
     * @param <M> the type of the resulting {@code Map}
     * @param <T> the Map entry type
     * @param <K> the collected map key type
     * @param <U> the collected map value type
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mapSupplier a function which returns a new, empty {@code Map} into
     *                    which the results will be inserted
     * @return The map collector
     */
    public static <T, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMapWithNullValues( Function<? super T, ? extends K> keyMapper,
                                            Function<? super T, ? extends U> valueMapper,
                                            Supplier<M> mapSupplier )
    {
        return Collector
            .of( mapSupplier,
                 ( map, entry ) -> map.put( keyMapper.apply( entry ),
                                            valueMapper.apply( entry ) ),
                 ( left, right ) ->
                 {
                     M result = mapSupplier.get();
                     result.putAll( left );
                     for( Map.Entry<K, U> entry : right.entrySet() )
                     {
                         K key = entry.getKey();
                         if( result.containsKey( key ) )
                         {
                             throw new IllegalStateException( String.format( "Duplicate key %s", key ) );
                         }
                         result.put( key, entry.getValue() );
                     }
                     return result;
                 } );
    }

    private static <T> BinaryOperator<T> throwingMerger()
    {
        return ( left, right ) ->
        {
            throw new IllegalStateException( String.format( "Duplicate key %s", left ) );
        };
    }

    private Collectors() {}
}
