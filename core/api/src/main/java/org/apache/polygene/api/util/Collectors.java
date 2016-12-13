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

import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Collectors
{
    /**
     * Collect a single element.
     * @param <T> Element type
     * @return The single element
     * @throws IllegalArgumentException if no or more than one element
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
     * @param <T> Element type
     * @return The single element, optional
     * @throws IllegalArgumentException if more than one element
     */
    public static <T>
    Collector<T, ?, Optional<T>> singleOrEmpty()
        throws IllegalArgumentException
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


    public static <T extends Map.Entry<K, U>, K, U>
    Collector<T, ?, Map<K, U>> toMap()
    {
        return java.util.stream.Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue );
    }

    public static <T extends Map.Entry<K, U>, K, U, M extends Map<K, U>>
    Collector<T, ?, M> toMap( Supplier<M> mapSupplier )
    {
        return toMap( Map.Entry::getKey, Map.Entry::getValue, mapSupplier );
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
