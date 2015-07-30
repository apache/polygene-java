/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import org.apache.zest.functional.Function;

import static org.apache.zest.functional.Iterables.iterable;

/**
 * Useful methods for handling Constructors.
 */
public final class Constructors
{
    public static final Function<Type, Iterable<Constructor<?>>> CONSTRUCTORS_OF = Classes.forClassHierarchy( new Function<Class<?>, Iterable<Constructor<?>>>()
    {
        @Override
        public Iterable<Constructor<?>> map( Class<?> type )
        {
            return iterable( type.getDeclaredConstructors() );
        }
    } );
}
