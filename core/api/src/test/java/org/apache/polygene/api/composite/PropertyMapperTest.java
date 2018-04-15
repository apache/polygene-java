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

package org.apache.polygene.api.composite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.api.util.AccessibleObjects.accessible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class PropertyMapperTest
{
    private final static Method MAP_TO_TYPE;

    static
    {
        try
        {
            MAP_TO_TYPE = accessible( PropertyMapper.class.getDeclaredMethod( "mapToType", Composite.class, Type.class, Object.class ) );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( e );
        }
    }

    @Test
    public void testMappingOfInteger()
        throws Exception
    {
        assertThat( mapToType( null, Integer.class, "5" ), equalTo( 5 ) );
        assertThat( mapToType( null, Integer.class, "-5" ), equalTo( -5 ) );
        assertThat( mapToType( null, Integer.class, "5" ).getClass(), equalTo( Integer.class ) );
    }

    @Test
    public void testMappingOfLong()
        throws Exception
    {
        assertThat( mapToType( null, Long.class, "5" ), equalTo( 5L ) );
        assertThat( mapToType( null, Long.class, "5876328476238746238" ), equalTo( 5876328476238746238L ) );
        assertThat( mapToType( null, Long.class, "5" ).getClass(), equalTo( Long.class ) );
    }

    @Test
    public void testMappingOfBoolean()
        throws Exception
    {
        assertThat( mapToType( null, Boolean.class, "false" ), is( false ) );
        assertThat( mapToType( null, Boolean.class, "true" ), is( true ) );
        assertThat( mapToType( null, Boolean.class, "false" ).getClass(), equalTo( Boolean.class ) );
    }

    @Test
    public void testMappingOfFloat()
        throws Exception
    {
        assertThat( mapToType( null, Float.class, "5.1234" ), equalTo( 5.1234f ) );
        assertThat( mapToType( null, Float.class, "5876328476.6238" ), equalTo( 5876328476.6238f ) );
        assertThat( mapToType( null, Float.class, "5" ).getClass(), equalTo( Float.class ) );
    }

    @Test
    public void testMappingOfDouble()
        throws Exception
    {
        assertThat( mapToType( null, Double.class, "5.1234" ), equalTo( 5.1234 ) );
        assertThat( mapToType( null, Double.class, "5876328476.623823" ), equalTo( 5876328476.623823 ) );
        assertThat( mapToType( null, Double.class, "5" ).getClass(), equalTo( Double.class ) );
    }

    @Test
    public void testMappingOfBigDecimal()
        throws Exception
    {
        assertThat( mapToType( null, BigDecimal.class, "3" ), equalTo( new BigDecimal( 3 ) ) );
        assertThat( mapToType( null, BigDecimal.class, "12345.67891011" ), equalTo( new BigDecimal( "12345.67891011" ) ) );
        assertThat( mapToType( null, BigDecimal.class, "5" ).getClass(), equalTo( BigDecimal.class ) );
    }

    @Test
    public void testMappingOfBigInteger()
        throws Exception
    {
        assertThat( mapToType( null, BigInteger.class, "32" ), equalTo( new BigInteger( "20", 16 ) ) );
        assertThat( mapToType( null, BigInteger.class, "1234567891011" ), equalTo( new BigInteger( "1234567891011" ) ) );
        assertThat( mapToType( null, BigInteger.class, "5" ).getClass(), equalTo( BigInteger.class ) );
    }

    @Test
    public void testMappingOfEnum()
        throws Exception
    {
        assertThat( mapToType( null, TestEnum.class, "FIRST" ), is( TestEnum.FIRST ) );
        assertThat( mapToType( null, TestEnum.class, "SECOND" ), is( TestEnum.SECOND ) );
        assertThat( mapToType( null, TestEnum.class, "SECOND" ).getClass(), equalTo( TestEnum.class ) );
    }

    @Test
    public void testMappingOfIntegerArray()
        throws Exception
    {
        Object[] value = (Object[]) mapToType( null, Integer[].class, "5,4 , 3   ,2,1" );
        assertThat( value.length, equalTo( 5 ) );
        assertThat( value[ 0 ], equalTo( 5 ) );
        assertThat( value[ 1 ], equalTo( 4 ) );
        assertThat( value[ 2 ], equalTo( 3 ) );
        assertThat( value[ 3 ], equalTo( 2 ) );
        assertThat( value[ 4 ], equalTo( 1 ) );
    }

    @Test
    public void testMappingOfStringArray()
        throws Exception
    {
        {
            Object[] value = (Object[]) mapToType( null, String[].class, "5,4 , 3   ,2,1" );
            assertThat( value.length, equalTo( 5 ) );
            assertThat( value[ 0 ], equalTo( "5" ) );
            assertThat( value[ 1 ], equalTo( "4 " ) );
            assertThat( value[ 2 ], equalTo( " 3   " ) );
            assertThat( value[ 3 ], equalTo( "2" ) );
            assertThat( value[ 4 ], equalTo( "1" ) );
        }
        {
            Object[] value = (Object[]) mapToType( null, String[].class, "5,4 ,\" 3,   \",  \" 2\" ,1" );
            assertThat( value[ 0 ], equalTo( "5" ) );
            assertThat( value[ 1 ], equalTo( "4 " ) );
            assertThat( value[ 2 ], equalTo( " 3,   " ) );
            assertThat( value[ 3 ], equalTo( " 2" ) );
            assertThat( value[ 4 ], equalTo( "1" ) );
            assertThat( value.length, equalTo( 5 ) );
        }
    }

    @Test
    public void testMappingOfBooleanArray()
        throws Exception
    {
        Object[] value = (Object[]) mapToType( null, Boolean[].class, " true,false,  false, true ,true,false" );
        assertThat( value[ 0 ], equalTo( true ) );
        assertThat( value[ 1 ], equalTo( false ) );
        assertThat( value[ 2 ], equalTo( false ) );
        assertThat( value[ 3 ], equalTo( true ) );
        assertThat( value[ 4 ], equalTo( true ) );
        assertThat( value[ 5 ], equalTo( false ) );
        assertThat( value.length, equalTo( 6 ) );
    }

    @Test
    public void testMappingOfList()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "list" ).getGenericReturnType();
        List<String> value = (List<String>) mapToType( null, type, "5,4 ,\" 3,   \",  \" 2\" ,1" );
        assertThat( value.get( 0 ), equalTo( "5" ) );
        assertThat( value.get( 1 ), equalTo( "4 " ) );
        assertThat( value.get( 2 ), equalTo( " 3,   " ) );
        assertThat( value.get( 3 ), equalTo( " 2" ) );
        assertThat( value.get( 4 ), equalTo( "1" ) );
        assertThat( value.size(), equalTo( 5 ) );
    }

    @Test
    public void testMappingOfSet()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "set" ).getGenericReturnType();
        Set<String> value = (Set<String>) mapToType( null, type, "5,4 ,\" 3,   \",  \" 2\" ,1" );
        assertThat( value.contains( "5" ), is( true ) );
        assertThat( value.contains( "4 " ), is( true ) );
        assertThat( value.contains( " 3,   " ), is( true ) );
        assertThat( value.contains( " 2" ), is( true ) );
        assertThat( value.contains( "1" ), is( true ) );
        assertThat( value.size(), equalTo( 5 ) );
    }

    @Test
    public void testMappingOfMap()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "map" ).getGenericReturnType();
        Map<String, String> value = (Map<String, String>) mapToType( null, type, "first:5,second:4 , third:\" 3,   \", fourth:  \" 2\" ,fifth : 1" );
        assertThat( value.get( "first" ), equalTo( "5" ) );
        assertThat( value.get( "second" ), equalTo( "4 " ) );
        assertThat( value.get( " third" ), equalTo( " 3,   " ) );
        assertThat( value.get( " fourth" ), equalTo( " 2" ) );
        assertThat( value.get( "fifth " ), equalTo( " 1" ) );
        assertThat( value.size(), equalTo( 5 ) );
    }

    private Object mapToType( Composite composite, Type propertyType, Object value )
        throws IllegalAccessException, InvocationTargetException
    {
        return MAP_TO_TYPE.invoke( null, composite, propertyType, value );
    }

    interface Testing
    {
        List<String> list();

        Set<String> set();

        Map<String, String> map();
    }

    enum TestEnum
    {
        FIRST,
        SECOND
    }
}
