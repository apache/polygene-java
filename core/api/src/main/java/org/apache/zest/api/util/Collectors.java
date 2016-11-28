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
package org.apache.zest.api.util;

import java.util.Optional;
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
    public static <T> Collector<T, ?, T> single()
        throws IllegalArgumentException
    {
        Supplier<T> thrower = () ->
        {
            throw new IllegalArgumentException( "No or more than one element in stream" );
        };
        return java.util.stream.Collectors.collectingAndThen( singleOrEmpty(),
                                                              optional -> optional.orElseGet( thrower ) );
    }

    /**
     * Collect an optional single element.
     * @param <T> Element type
     * @return An optional single element, empty if no or more than one element
     */
    public static <T> Collector<T, ?, Optional<T>> singleOrEmpty()
    {
        return java.util.stream.Collectors.reducing( ( a, b ) -> null );
    }

    private Collectors() {}
}
