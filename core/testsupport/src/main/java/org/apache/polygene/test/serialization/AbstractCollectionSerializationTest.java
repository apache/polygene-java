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
package org.apache.polygene.test.serialization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Assert that Serialization behaviour on Collections and Maps is correct.
 */
public class AbstractCollectionSerializationTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( SomeValue.class );
    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected Serialization serialization;

    @Test
    public void givenPrimitiveArrayWithIntsWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        int[] primitiveArray = new int[]
            {
                23, 42, -23, -42
            };
        String output = serialization.serialize( primitiveArray );
        System.out.println( output );
        int[] deserialized = serialization.deserialize( module, int[].class, output );
        assertArrayEquals( primitiveArray, deserialized );
    }

    @Test
    public void givenArrayWithByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        Byte[] array = new Byte[]
            {
                9, null, -12, -12, 127, -128, 73
            };
        String output = serialization.serialize( array );
        System.out.println( output );
        Byte[] deserialized = serialization.deserialize( module, Byte[].class, output );
        assertArrayEquals( array, deserialized );
    }

    @Test
    public void givenIterableTypeWithByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( new AdHocIterable<>( byteCollection() ) );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.BYTE );
        List<Byte> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( byteCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( byteCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.setOf( ValueType.BYTE );
        Set<Byte> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( new LinkedHashSet<>( byteCollection() ) ) );
    }

    @Test
    public void givenCollectionTypeWithCharacterAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( characterCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.CHARACTER );
        List<Character> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( characterCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithShortAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( shortCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.SHORT );
        List<Short> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( shortCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithIntegerAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( integerCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.INTEGER );
        List<Integer> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( integerCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithLongAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( longCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.LONG );
        List<Long> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( longCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithFloatAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( floatCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.FLOAT );
        List<Float> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( floatCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithDoubleAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        String output = serialization.serialize( doubleCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.DOUBLE );
        List<Double> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( doubleCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithBigIntegerAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( bigIntegerCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.BIG_INTEGER );
        List<BigInteger> list = serialization.deserialize( module, collectionType, output );
        assertThat( list, equalTo( bigIntegerCollection() ) );
    }

    @Test
    public void givenCollectionTypeWithBigDecimalAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( bigDecimalCollection() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.collectionOf( ValueType.BIG_DECIMAL );
        Collection<BigDecimal> collection = serialization.deserialize( module, collectionType, output );
        assertThat( collection, equalTo( bigDecimalCollection() ) );
    }

    @Test
    public void givenMapOfStringByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( stringByteMap() );
        System.out.println( output );
        MapType mapType = MapType.of( ValueType.STRING, ValueType.BYTE );
        Map<String, Byte> value = serialization.deserialize( module, mapType, output );
        assertThat( value, equalTo( stringByteMap() ) );
    }

    @Test
    public void givenMapOfStringListStringAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( stringMultiMap() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( ValueType.STRING );
        MapType mapType = MapType.of( ValueType.STRING, collectionType );
        Map<String, List<String>> value = serialization.deserialize( module, mapType, output );
        assertThat( value, equalTo( stringMultiMap() ) );
    }

    @Test
    public void givenListOfMapStringStringAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( stringListOfMaps() );
        System.out.println( output );
        CollectionType collectionType = CollectionType.listOf( MapType.of( ValueType.STRING, ValueType.STRING ) );
        List<Map<String, String>> value = serialization.deserialize( module, collectionType, output );
        assertThat( value, equalTo( stringListOfMaps() ) );
    }

    @Test
    public void givenListOfValueCompositesAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = serialization.serialize( valueCompositesList() );
        System.out.println( output );
        ValueCompositeType valueType = module.valueDescriptor( SomeValue.class.getName() ).valueType();
        CollectionType collectionType = CollectionType.listOf( valueType );
        List<SomeValue> value = serialization.deserialize( module, collectionType, output );
        assertThat( value, equalTo( valueCompositesList() ) );
    }

    @Test
    public void givenEnumSetWhenSerializingAndDeserializingExpectEquals()
    {
        Set<SomeEnum> enumSet = EnumSet.allOf( SomeEnum.class );
        String output = serialization.serialize( enumSet );
        System.out.println( output );
        CollectionType valueType = CollectionType.setOf( EnumType.of( SomeEnum.class ) );
        Set<SomeEnum> value = serialization.deserialize( module, valueType, output );
        assertThat( value, equalTo( enumSet ) );
    }

    @Test
    public void givenEnumMapWhenSerializingAndDeserializingExpectEquals()
    {
        EnumMap<SomeEnum, Number> enumMap = new EnumMap<>( SomeEnum.class );
        for( SomeEnum value : SomeEnum.values() )
        {
            enumMap.put( value, 23 );
        }
        String output = serialization.serialize( enumMap );
        System.out.println( output );
        MapType valueType = MapType.of( EnumType.of( SomeEnum.class ), ValueType.of( Integer.class ) );
        Map<SomeEnum, Number> value = serialization.deserialize( module, valueType, output );
        assertThat( value, equalTo( enumMap ) );
    }

    private ArrayList<Byte> byteCollection()
    {
        ArrayList<Byte> value = new ArrayList<>();
        value.add( (byte) 9 );
        value.add( null );
        value.add( (byte) -12 );
        value.add( (byte) -12 );
        value.add( (byte) 127 );
        value.add( (byte) -128 );
        value.add( (byte) 73 );
        return value;
    }

    private List<Character> characterCollection()
    {
        List<Character> value = new ArrayList<>();
        value.add( 'Q' );
        value.add( 'i' );
        value.add( null );
        value.add( '4' );
        value.add( 'j' );
        return value;
    }

    private Collection<Short> shortCollection()
    {
        Collection<Short> value = new ArrayList<>();
        value.add( (short) -32768 );
        value.add( (short) 32767 );
        value.add( (short) -82 );
        value.add( null );
        return value;
    }

    private Collection<Integer> integerCollection()
    {
        Collection<Integer> value = new ArrayList<>();
        value.add( Integer.MAX_VALUE );
        value.add( -283 );
        value.add( null );
        value.add( Integer.MIN_VALUE );
        value.add( 238 );
        return value;
    }

    private Collection<Long> longCollection()
    {
        Collection<Long> value = new ArrayList<>();
        value.add( 98239723L );
        value.add( -1298233L );
        value.add( -1L );
        value.add( 0L );
        value.add( null );
        value.add( 1L );
        value.add( Long.MAX_VALUE );
        value.add( Long.MIN_VALUE );
        return value;
    }

    private Collection<Float> floatCollection()
    {
        Collection<Float> value = new ArrayList<>();
        value.add( -1f );
        value.add( 1f );
        value.add( 1f );
        value.add( 0f );
        value.add( Float.MAX_VALUE );
        value.add( Float.MIN_VALUE );
        value.add( null );
        value.add( 0.123456f );
        value.add( -0.232321f );
        return value;
    }

    private Collection<Double> doubleCollection()
    {
        Collection<Double> value = new ArrayList<>();
        value.add( -1.0 );
        value.add( 1.0 );
        value.add( 0.0 );
        value.add( Double.MAX_VALUE );
        value.add( null );
        value.add( Double.MIN_VALUE );
        value.add( 0.123456 );
        value.add( -0.232321 );
        return value;
    }

    private Collection<BigInteger> bigIntegerCollection()
    {
        Collection<BigInteger> value = new ArrayList<>();
        value.add( new BigInteger( "-1" ) );
        value.add( BigInteger.ZERO );
        value.add( BigInteger.ONE );
        value.add( null );
        value.add( BigInteger.TEN );
        value.add( new BigInteger( "-1827368263823729372397239829332" ) );
        value.add( new BigInteger( "2398723982982379827373972398723" ) );
        return value;
    }

    private Collection<BigDecimal> bigDecimalCollection()
    {
        Collection<BigDecimal> value = new ArrayList<>();
        value.add( new BigDecimal( "1.2" ) );
        value.add( new BigDecimal( "3.4" ) );
        value.add( null );
        value.add( new BigDecimal( "5.6" ) );
        return value;
    }

    private Map<String, Byte> stringByteMap()
    {
        Map<String, Byte> value = new LinkedHashMap<>();
        value.put( "a", (byte) 9 );
        value.put( "b", null );
        value.put( "c", (byte) -12 );
        return value;
    }

    private Map<String, List<String>> stringMultiMap()
    {
        Map<String, List<String>> value = new LinkedHashMap<>();
        List<String> list = new ArrayList<>();
        list.add( "foo" );
        list.add( "bar" );
        list.add( null );
        list.add( "cathedral" );
        list.add( "bazar" );
        value.put( "alpha", list );
        value.put( "beta", null );
        value.put( "gamma", Collections.emptyList() );
        return value;
    }

    private List<Map<String, String>> stringListOfMaps()
    {
        List<Map<String, String>> value = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put( "foo", "bar" );
        map.put( "cathedral", "bazar" );
        map.put( "yield", null );
        map.put( "42", "23" );
        value.add( map );
        value.add( null );
        value.add( Collections.emptyMap() );
        return value;
    }

    private List<SomeValue> valueCompositesList()
    {
        List<SomeValue> list = new ArrayList<>();
        list.add( newSomeValue( "", "bazar" ) );
        list.add( null );
        list.add( newSomeValue( "bar", null ) );
        return list;
    }

    public interface SomeValue
    {
        Property<String> foo();

        @Optional
        Property<String> cathedral();
    }

    private SomeValue newSomeValue( String foo, String cathedral )
    {
        ValueBuilder<SomeValue> builder = module.instance().newValueBuilder( SomeValue.class );
        SomeValue value = builder.prototype();
        value.foo().set( foo );
        if( cathedral != null )
        {
            value.cathedral().set( cathedral );
        }
        return builder.newInstance();
    }

    private static class AdHocIterable<T> implements Iterable<T>
    {
        private final Iterable<T> delegate;

        private AdHocIterable( Iterable<T> delegate )
        {
            this.delegate = delegate;
        }

        @Override
        public Iterator<T> iterator()
        {
            return delegate.iterator();
        }
    }

    private enum SomeEnum
    {
        FOO,
        BAR,
        BAZAR,
        CATHEDRAL
    }
}
