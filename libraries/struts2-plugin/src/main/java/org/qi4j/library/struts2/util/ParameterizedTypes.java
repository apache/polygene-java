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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ParameterizedTypes
{
    private ParameterizedTypes()
    {
    }

    public static <S, T extends S> Type[] findTypeVariables( Class<T> type, Class<S> searchType )
    {
        return ParameterizedTypes.findParameterizedType( type, searchType ).getActualTypeArguments();
    }

    public static <S, T extends S> ParameterizedType findParameterizedType( Class<T> type, Class<S> searchType )
    {
        return ParameterizedTypes.findParameterizedType( (Type) type, searchType );
    }

    static ParameterizedType findParameterizedType( Type type, Type searchType )
    {
        if( type instanceof ParameterizedType && ( (ParameterizedType) type ).getRawType().equals( searchType ) )
        {
            return (ParameterizedType) type;
        }
        Type[] parents = ( (Class<?>) type ).getGenericInterfaces();
        for( Type parent : parents )
        {
            ParameterizedType foundType = findParameterizedType( parent, searchType );
            if( foundType != null )
            {
                return foundType;
            }
        }
        return null;
    }
}
