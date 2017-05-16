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
package org.apache.polygene.api.metrics;

import org.apache.polygene.api.structure.Module;

import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * Metric names utilities.
 */
public class MetricNames
{
    /**
     * Build a Metric name for the given fragments.
     *
     * @param fragments Name fragments
     * @return Metric name
     */
    public static String nameFor( String... fragments )
    {
        StringJoiner joiner = new StringJoiner( "." );
        for( String fragment : fragments )
        {
            joiner.add( fragment );
        }
        return joiner.toString();
    }

    /**
     * Build a Metric name for the given Module, Type and optional fragments.
     *
     * @param module Module
     * @param type Type
     * @param fragments Name fragments
     * @return Metric name
     */
    public static String nameFor( Module module, Class<?> type, String... fragments )
    {
        StringJoiner joiner = new StringJoiner( "." )
                .add( module.layer().name() )
                .add( module.name() )
                .add( className( type ) );
        for( String fragment : fragments )
        {
            joiner.add( fragment );
        }
        return joiner.toString();
    }

    /**
     * Build a Metric name for the given Module, Method and optional fragments.
     *
     * @param module Module
     * @param method Method
     * @param fragments Name fragments
     * @return Metric name
     */
    public static String nameFor( Module module, Method method, String... fragments )
    {
        StringJoiner joiner = new StringJoiner( "." )
                .add( module.layer().name() )
                .add( module.name() )
                .add( className( method.getDeclaringClass() ) )
                .add( method.getName() );
        for( String fragment : fragments )
        {
            joiner.add( fragment );
        }
        return joiner.toString();
    }

    private static String className( Class<?> clazz )
    {
        return clazz.getName().substring( clazz.getName().lastIndexOf( '.' ) + 1 ).replace( '$', '.' );
    }
}
