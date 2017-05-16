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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import org.apache.polygene.api.util.Classes;

/**
 * Map ValueType.
 * <p>This handles instances of Map.</p>
 */
public final class MapType
    extends ValueType
{
    public static boolean isMap( Type type )
    {
        Class<?> cl = Classes.RAW_CLASS.apply( type );
        return Map.class.isAssignableFrom( cl );
    }

    public static MapType of( Class<?> mapType, ValueType keyType, ValueType valueType )
    {
        return new MapType( mapType, keyType, valueType );
    }

    public static MapType of( Class<?> mapType, Class<?> keyType, Class<?> valueType )
    {
        return of( mapType, ValueType.of( keyType ), ValueType.of( valueType ) );
    }

    public static MapType of( ValueType keyType, ValueType valueType )
    {
        return new MapType( Map.class, keyType, valueType );
    }

    public static MapType of( Class<?> keyType, Class<?> valueType )
    {
        return of( ValueType.of( keyType ), ValueType.of( valueType ) );
    }

    private ValueType keyType;
    private ValueType valueType;

    public MapType( Class<?> type, ValueType keyType, ValueType valueType )
    {
        super( type );
        this.keyType = keyType;
        this.valueType = valueType;
        if( !isMap( type ) )
        {
            throw new IllegalArgumentException( type + " is not a Map." );
        }
    }

    public ValueType keyType()
    {
        return keyType;
    }

    public ValueType valueType()
    {
        return valueType;
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o ) { return true; }
        if( o == null || getClass() != o.getClass() ) { return false; }
        if( !super.equals( o ) ) { return false; }
        MapType mapType = (MapType) o;
        return Objects.equals( keyType, mapType.keyType ) &&
               Objects.equals( valueType, mapType.valueType );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( super.hashCode(), keyType, valueType );
    }

    @Override
    public String toString()
    {
        return super.toString() + "<" + keyType + "," + valueType + ">";
    }
}
