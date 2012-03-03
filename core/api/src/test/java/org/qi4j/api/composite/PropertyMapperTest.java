/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.api.composite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyMapperTest
{
    private final static Method MAP_TO_TYPE;

    static
    {
        try
        {
            MAP_TO_TYPE = PropertyMapper.class.getDeclaredMethod( "mapToType", Type.class, Object.class );
            MAP_TO_TYPE.setAccessible( true );
        }
        catch( NoSuchMethodException e )
        {
            InternalError error = new InternalError();
            error.initCause( e );
            throw error;
        }
    }

    @Test
    public void testMappingOfInteger()
        throws Exception
    {
        assertEquals( 5, mapToType( Integer.class, "5" ) );
        assertEquals( -5, mapToType( Integer.class, "-5" ) );
        assertEquals( Integer.class, mapToType( Integer.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfLong()
        throws Exception
    {
        assertEquals( 5L, mapToType( Long.class, "5" ) );
        assertEquals( 5876328476238746238L, mapToType( Long.class, "5876328476238746238" ) );
        assertEquals( Long.class, mapToType( Long.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfBoolean()
        throws Exception
    {
        assertEquals( false, mapToType( Boolean.class, "false" ) );
        assertEquals( true, mapToType( Boolean.class, "true" ) );
        assertEquals( Boolean.class, mapToType( Boolean.class, "false" ).getClass() );
    }

    @Test
    public void testMappingOfFloat()
        throws Exception
    {
        assertEquals( 5.1234f, mapToType( Float.class, "5.1234" ) );
        assertEquals( 5876328476.6238f, mapToType( Float.class, "5876328476.6238" ) );
        assertEquals( Float.class, mapToType( Float.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfDouble()
        throws Exception
    {
        assertEquals( 5.1234, mapToType( Double.class, "5.1234" ) );
        assertEquals( 5876328476.623823, mapToType( Double.class, "5876328476.623823" ) );
        assertEquals( Double.class, mapToType( Double.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfBigDecimal()
        throws Exception
    {
        assertEquals( new BigDecimal( 3 ), mapToType( BigDecimal.class, "3" ) );
        assertEquals( new BigDecimal( "12345.67891011" ), mapToType( BigDecimal.class, "12345.67891011" ) );
        assertEquals( BigDecimal.class, mapToType( BigDecimal.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfBigInteger()
        throws Exception
    {
        assertEquals( new BigInteger( "20", 16 ), mapToType( BigInteger.class, "32" ) );
        assertEquals( new BigInteger( "1234567891011" ), mapToType( BigInteger.class, "1234567891011" ) );
        assertEquals( BigInteger.class, mapToType( BigInteger.class, "5" ).getClass() );
    }

    @Test
    public void testMappingOfEnum()
        throws Exception
    {
        assertEquals( TestEnum.FIRST, mapToType( TestEnum.class, "FIRST" ) );
        assertEquals( TestEnum.SECOND, mapToType( TestEnum.class, "SECOND" ) );
        assertEquals( TestEnum.class, mapToType( TestEnum.class, "SECOND" ).getClass() );
    }

    @Test
    public void testMappingOfIntegerArray()
        throws Exception
    {
        Object[] value = (Object[]) mapToType( Integer[].class, "5,4 , 3   ,2,1" );
        assertEquals( 5, value.length );
        assertEquals( 5, value[ 0 ] );
        assertEquals( 4, value[ 1 ] );
        assertEquals( 3, value[ 2 ] );
        assertEquals( 2, value[ 3 ] );
        assertEquals( 1, value[ 4 ] );
    }

    @Test
    public void testMappingOfStringArray()
        throws Exception
    {
        {
            Object[] value = (Object[]) mapToType( String[].class, "5,4 , 3   ,2,1" );
            assertEquals( 5, value.length );
            assertEquals( "5", value[ 0 ] );
            assertEquals( "4 ", value[ 1 ] );
            assertEquals( " 3   ", value[ 2 ] );
            assertEquals( "2", value[ 3 ] );
            assertEquals( "1", value[ 4 ] );
        }
        {
            Object[] value = (Object[]) mapToType( String[].class, "5,4 ,\" 3,   \",  \" 2\" ,1" );
            assertEquals( "5", value[ 0 ] );
            assertEquals( "4 ", value[ 1 ] );
            assertEquals( " 3,   ", value[ 2 ] );
            assertEquals( " 2", value[ 3 ] );
            assertEquals( "1", value[ 4 ] );
            assertEquals( 5, value.length );
        }
    }

    @Test
    public void testMappingOfBooleanArray()
        throws Exception
    {
        Object[] value = (Object[]) mapToType( Boolean[].class, " true,false,  false, true ,true,false" );
        assertEquals( true, value[ 0 ] );
        assertEquals( false, value[ 1 ] );
        assertEquals( false, value[ 2 ] );
        assertEquals( true, value[ 3 ] );
        assertEquals( true, value[ 4 ] );
        assertEquals( false, value[ 5 ] );
        assertEquals( 6, value.length );
    }

    @Test
    public void testMappingOfList()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "list" ).getGenericReturnType();
        List<String> value = (List<String>) mapToType( type, "5,4 ,\" 3,   \",  \" 2\" ,1" );
        assertEquals( "5", value.get( 0 ) );
        assertEquals( "4 ", value.get( 1 ) );
        assertEquals( " 3,   ", value.get( 2 ) );
        assertEquals( " 2", value.get( 3 ) );
        assertEquals( "1", value.get( 4 ) );
        assertEquals( 5, value.size() );
    }

    @Test
    public void testMappingOfSet()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "set" ).getGenericReturnType();
        Set<String> value = (Set<String>) mapToType( type, "5,4 ,\" 3,   \",  \" 2\" ,1" );
        assertTrue( value.contains( "5" ) );
        assertTrue( value.contains( "4 " ) );
        assertTrue( value.contains( " 3,   " ) );
        assertTrue( value.contains( " 2" ) );
        assertTrue( value.contains( "1" ) );
        assertEquals( 5, value.size() );
    }

    @Test
    public void testMappingOfMap()
        throws Exception
    {
        Type type = Testing.class.getDeclaredMethod( "map" ).getGenericReturnType();
        Map<String, String> value = (Map<String, String>) mapToType( type, "first:5,second:4 , third:\" 3,   \", fourth:  \" 2\" ,fifth : 1" );
        assertEquals( "5", value.get( "first" ) );
        assertEquals( "4 ", value.get( "second" ) );
        assertEquals( " 3,   ", value.get( " third" ) );
        assertEquals( " 2", value.get( " fourth" ) );
        assertEquals( " 1", value.get( "fifth " ) );
        assertEquals( 5, value.size() );
    }

    private Object mapToType( Type propertyType, Object value )
        throws IllegalAccessException, InvocationTargetException
    {
        return MAP_TO_TYPE.invoke( null, propertyType, value );
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
