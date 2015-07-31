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
package org.qi4j.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;
import org.qi4j.functional.Iterables;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Useful methods for handling Fields.
 */
public final class Fields
{
    public static final Function2<Class<?>, String, Field> FIELD_NAMED = new Function2<Class<?>, String, Field>()
    {
        @Override
        public Field map( Class<?> aClass, String name )
        {
            return Iterables.first( Iterables.filter( Classes.memberNamed( name ), FIELDS_OF.map( aClass ) ) );
        }
    };

    public static final Function<Type, Iterable<Field>> FIELDS_OF = Classes.forClassHierarchy( new Function<Class<?>, Iterable<Field>>()
    {
        @Override
        public Iterable<Field> map( Class<?> type )
        {
            return iterable( type.getDeclaredFields() );
        }
    } );
}
