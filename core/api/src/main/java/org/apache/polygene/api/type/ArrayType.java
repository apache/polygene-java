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
import java.util.Objects;
import org.apache.polygene.api.util.Classes;

/**
 * Array ValueType.
 * <p>This handles arrays of primitives and values</p>
 */
public class ArrayType extends ValueType
{
    public static boolean isArray( Type type )
    {
        return Classes.RAW_CLASS.apply( type ).isArray();
    }

    public static ArrayType of( Class<?> arrayType )
    {
        return new ArrayType( arrayType, ValueType.of( arrayType.getComponentType() ) );
    }

    private ValueType collectedType;

    public ArrayType( Class<?> type, ValueType collectedType )
    {
        super( type );
        this.collectedType = collectedType;
        if( !isArray( type ) )
        {
            throw new IllegalArgumentException( type + " is not an array" );
        }
    }

    public ValueType collectedType()
    {
        return collectedType;
    }

    public boolean isArrayOfPrimitives()
    {
        return hasType( boolean[].class )
               || hasType( char[].class )
               || hasType( short[].class )
               || hasType( int[].class )
               || hasType( byte[].class )
               || hasType( long[].class )
               || hasType( float[].class )
               || hasType( double[].class );
    }

    public boolean isArrayOfPrimitiveBytes()
    {
        return hasType( byte[].class );
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o ) { return true; }
        if( o == null || getClass() != o.getClass() ) { return false; }
        if( !super.equals( o ) ) { return false; }
        ArrayType that = (ArrayType) o;
        return Objects.equals( collectedType, that.collectedType );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( super.hashCode(), collectedType );
    }

    @Override
    public String toString()
    {
        return collectedType + "[]";
    }
}
