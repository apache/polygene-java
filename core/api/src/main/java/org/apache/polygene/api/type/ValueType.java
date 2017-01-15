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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.util.NullArgumentException;

import static java.util.stream.Collectors.joining;

/**
 * Base class for types of values in ValueComposites and Properties.
 */
public class ValueType
    implements HasTypes
{

    public static ValueType of( Class<?> type )
    {
        return new ValueType( type );
    }

    /**
     * Check if a non-null object is of any of the Primitive Value Types or an array of them.
     * <p>
     *     String, Boolean, Integer, Double, Float, Long, Byte, Short and Character and their Java primitive types
     *     counterparts are considered as Primitive Value Types.
     * </p>
     * <p>
     *     Date, BigInteger, BigDecimal and JodaTime types are not considered as Primitive Value Types.
     * </p>
     *
     * @param object Object
     * @return true if object is a primitive value or an array of primitive values
     * @throws IllegalArgumentException if object is null
     */
    public static boolean isPrimitiveValue( Object object )
    {
        NullArgumentException.validateNotNull( "object", object );
        if( object instanceof String
            || object instanceof Character
            || object instanceof Boolean
            || object instanceof Integer
            || object instanceof Double
            || object instanceof Float
            || object instanceof Long
            || object instanceof Byte
            || object instanceof Short )
        {
            return true;
        }
        if( object.getClass().isArray() )
        {
            return isArrayOfPrimitiveValues( object );
        }
        return false;
    }

    public static boolean isIdentity( Object object )
    {
        return object instanceof Identity;
    }

    private static boolean isArrayOfPrimitiveValues( Object array )
    {
        if( array instanceof String[]
            || array instanceof char[] || array instanceof Character[]
            || array instanceof boolean[] || array instanceof Boolean[]
            || array instanceof int[] || array instanceof Integer[]
            || array instanceof double[] || array instanceof Double[]
            || array instanceof float[] || array instanceof Float[]
            || array instanceof long[] || array instanceof Long[]
            || array instanceof byte[] || array instanceof Byte[]
            || array instanceof short[] || array instanceof Short[] )
        {
            return true;
        }
        return false;
    }

    public static boolean isPrimitiveValueType( ValueType valueType )
    {
        return isPrimitiveValueType( valueType.primaryType() );
    }

    /**
     * @see ValueType#isPrimitiveValue(java.lang.Object)
     * @param type Type
     * @return true if object is a primitive value or an array of primitive values
     */
    public static boolean isPrimitiveValueType( Class<?> type )
    {
        NullArgumentException.validateNotNull( "type", type );
        if( String.class.isAssignableFrom( type ) )
        {
            return true;
        }
        if( type.isArray() )
        {
            return isPrimitiveValueType( type.getComponentType() );
        }
        return false;
    }
    protected final List<Class<?>> types;

    public ValueType( Class<?> type )
    {
        this( Collections.singletonList( type ) );
    }

    @SuppressWarnings( "unchecked" )
    public ValueType( List<Class<?>> types )
    {
        this.types = types;
    }

    public Class<?> primaryType()
    {
        return types.stream().findFirst().orElse( null );
    }

    @Override
    public Stream<Class<?>> types()
    {
        return types.stream();
    }

    @Override
    public String toString()
    {
        String name = types.stream().map( Class::getName ).collect( joining( "," ) );
        if( name.contains( "," ) )
        {
            name = "{" + name + "}";
        }
        return name;
    }
}