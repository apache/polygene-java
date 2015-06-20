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
package org.qi4j.library.struts2.util;

import java.util.Arrays;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;

public final class ClassNames
{
    private ClassNames()
    {
    }

    public static String classNameInDotNotation( Class<?> type )
    {
        Function<Class<?>, String> mapper = ClassNameFilters.passThruMapper;
        Iterable<String> map = Iterables.map( mapper, Arrays.asList( type ) );
        return ClassNames.camelCaseToDotNotation( map );
    }

    public static String classNameInDotNotation( Iterable<Class<?>> type, ClassNameMapper mapper )
    {
        Iterable<String> map = Iterables.map( mapper, type );
        return ClassNames.camelCaseToDotNotation( map );
    }

    public static String camelCaseToDotNotation( Iterable<String> names )
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for( String name : names )
        {
            if( count++ > 0 )
            {
                sb.append( "," );
            }
            sb.append( camelCaseToDotNotation( name ) );
        }
        if( count == 1 )
        {
            return sb.toString();
        }
        sb.append( "]" );
        return "[" + sb.toString();
    }

    private static String camelCaseToDotNotation( String name )
    {
        StringBuilder sb = new StringBuilder( name.length() );
        sb.append( toLowerCase( name.charAt( 0 ) ) );
        for( int i = 1; i < name.length(); i++ )
        {
            char c = name.charAt( i );
            if( isLowerCase( c ) )
            {
                sb.append( c );
            }
            else
            {
                sb.append( '.' );
                sb.append( toLowerCase( c ) );
            }
        }
        return sb.toString();
    }
}
