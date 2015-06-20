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

public final class ClassNameFilters
{
    private ClassNameFilters()
    {
    }

    public static ClassNameMapper passThruMapper = new ClassNameMapper()
    {
        @Override
        public String map( Class<?> type )
        {
            return type.getName();
        }
    };

    public static ClassNameMapper removeSuffixes( final String... suffixes )
    {
        return new ClassNameMapper()
        {
            @Override
            public String map( Class<?> type )
            {
                String className = type.getName();
                for( String suffix : suffixes )
                {
                    if( className.endsWith( suffix ) )
                    {
                        return className.substring( 0, className.length() - suffix.length() );
                    }
                }
                return className;
            }
        };
    }
}
