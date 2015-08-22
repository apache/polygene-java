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
package org.apache.zest.api.common;

import java.io.Serializable;
import java.lang.reflect.Type;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.util.NullArgumentException;

/**
 * Represents a Type name.
 */
public final class TypeName
    implements Serializable, Comparable<TypeName>
{
    private final String name;

    public static TypeName nameOf( Class type )
    {
        NullArgumentException.validateNotNull( "type", type );
        return new TypeName( type.getName() );
    }

    public static TypeName nameOf( Type type )
    {
        return nameOf( Classes.RAW_CLASS.apply( type ) );
    }

    public static TypeName nameOf( String typeName )
    {
        return new TypeName( typeName );
    }

    private TypeName( String name )
    {
        NullArgumentException.validateNotEmpty( "name", name );
        this.name = name;
    }

    public String normalized()
    {
        return Classes.normalizeClassToURI( name );
    }

    public String toURI()
    {
        return Classes.toURI( name );
    }

    public String name()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean isClass( final Class<?> type )
    {
        return type.getName().equals( name );
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final TypeName other = (TypeName) o;

        return name.equals( other.name );
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public int compareTo( final TypeName typeName )
    {
        return this.name.compareTo( typeName.name );
    }
}

