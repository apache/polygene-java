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
package org.apache.polygene.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Useful methods for handling Methods.
 */
public class Methods
{
    public static final Predicate<Type> HAS_METHODS =
        item -> Classes.RAW_CLASS.apply( item ).getDeclaredMethods().length > 0;

    public static final Function<Type, Stream<Method>> METHODS_OF = Classes.forTypes( type ->
        Stream.of( type ).map( Classes.RAW_CLASS ).flatMap( clazz -> Arrays.stream( clazz.getDeclaredMethods() ) )
    );

    public static final BiFunction<Class<?>, String, Method> METHOD_NAMED = ( clazz, name ) ->
        METHODS_OF.apply( clazz ).filter( Classes.memberNamed( name ) ).findFirst().orElse( null );


    public static Stream<Method> methodsOf( Type type )
    {
        return Stream.of(type).flatMap( METHODS_OF );
    }
}
