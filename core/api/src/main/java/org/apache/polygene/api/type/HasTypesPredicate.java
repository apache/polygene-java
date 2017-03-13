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
package org.apache.polygene.api.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.polygene.api.util.Classes.interfacesOf;

public abstract class HasTypesPredicate<T extends HasTypes> implements Predicate<T>
{
    protected final List<Type> matchTypes;

    protected HasTypesPredicate( List<Type> types )
    {
        matchTypes = types;
    }

    @Override
    public final boolean test( T hasTypes )
    {
        for( Type matchType : matchTypes )
        {
            if( matchType instanceof Class )
            {
                if( hasTypes.types().anyMatch( matchPredicate( matchType ) ) )
                {
                    return true;
                }
            }
            else
            {
                if( matchType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) matchType;
                    Type rawType = parameterizedType.getRawType();

                    if( hasTypes.types().anyMatch( matchPredicate( rawType ) ) )
                    {
                        // Then check Bar
                        if( interfacesOf( hasTypes.types() ).anyMatch( intf -> intf.equals( matchType ) ) )
                        {
                            return true;
                        }
                    }
                }
                else if( matchType instanceof WildcardType )
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract Predicate<Type> matchPredicate( Type candidate );
}
