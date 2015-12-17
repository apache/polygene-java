/*
 * Copyright (c) 2011, Niclas Hehdman. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.test.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.MapType;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.functional.Iterables;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Test;

import static org.apache.zest.io.Inputs.iterable;
import static org.apache.zest.io.Inputs.text;
import static org.apache.zest.io.Outputs.collection;
import static org.apache.zest.io.Outputs.text;
import static org.apache.zest.io.Transforms.map;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Assert that ValueSerialization behaviour on Collections and Maps is correct.
 */
// TODO How to assert that given a collection of valuecomposites when serializing and deserializing we have to OOME?
public class AbstractCollectionSerializationTest
    extends AbstractZestTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class );
    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

    @Test
    public void testIOString()
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        iterable( byteCollection() ).transferTo( map( valueSerialization.serialize(), text( sb ) ) );
        String output = sb.toString();

        List<Byte> list = new ArrayList<>();
        text( output ).transferTo( map( valueSerialization.deserialize( module, Byte.class ), collection( list ) ) );
        assertEquals( byteCollection(), list );
    }

    @Test
    public void givenPrimitiveArrayWithIntsWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        int[] primitiveArray = new int[]
        {
            23, 42, -23, -42
        };
        String output = valueSerialization.serialize( primitiveArray );
        int[] deserialized = valueSerialization.deserialize( module, int[].class, output );
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
        String output = valueSerialization.serialize( array );
        Byte[] deserialized = valueSerialization.deserialize( module, Byte[].class, output );
        assertArrayEquals( array, deserialized );
    }

    @Test
    public void givenIterableTypeWithByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( Iterables.iterable( byteCollection().toArray() ) );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Byte.class ) );
        List<Byte> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( byteCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( byteCollection() );
        CollectionType collectionType = new CollectionType( Set.class, new ValueType( Byte.class ) );
        Set<Byte> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( new LinkedHashSet<>( byteCollection() ), list );
    }

    @Test
    public void givenCollectionTypeWithCharacterAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( characterCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Character.class ) );
        List<Character> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( characterCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithShortAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( shortCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Short.class ) );
        List<Short> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( shortCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithIntegerAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( integerCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Integer.class ) );
        List<Integer> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( integerCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithLongAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( longCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Long.class ) );
        List<Long> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( longCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithFloatAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( floatCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Float.class ) );
        List<Float> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( floatCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithDoubleAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        String output = valueSerialization.serialize( doubleCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( Double.class ) );
        List<Double> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( doubleCollection(), list );

    }

    @Test
    public void givenCollectionTypeWithBigIntegerAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( bigIntegerCollection() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( BigInteger.class ) );
        List<BigInteger> list = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( bigIntegerCollection(), list );
    }

    @Test
    public void givenCollectionTypeWithBigDecimalAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( bigDecimalCollection() );
        CollectionType collectionType = new CollectionType( Collection.class, new ValueType( BigDecimal.class ) );
        Collection<BigDecimal> collection = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( bigDecimalCollection(), collection );
    }

    @Test
    public void givenMapOfStringByteAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( stringByteMap() );
        MapType mapType = new MapType( Map.class, new ValueType( String.class ), new ValueType( Byte.class ) );
        Map<String, Byte> value = valueSerialization.deserialize( module, mapType, output );
        assertEquals( stringByteMap(), value );
    }

    @Test
    public void givenMapOfStringListStringAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( stringMultiMap() );
        CollectionType collectionType = new CollectionType( List.class, new ValueType( String.class ) );
        MapType mapType = new MapType( Map.class, new ValueType( String.class ), collectionType );
        Map<String, List<String>> value = valueSerialization.deserialize( module, mapType, output );
        assertEquals( stringMultiMap(), value );
    }

    @Test
    public void givenListOfMapStringStringAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( stringListOfMaps() );
        ValueType stringType = new ValueType( String.class );
        CollectionType collectionType = new CollectionType( List.class, new MapType( Map.class, stringType, stringType ) );
        List<Map<String, String>> value = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( stringListOfMaps(), value );
    }

    @Test
    public void givenListOfValueCompositesAndNullElementWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        String output = valueSerialization.serialize( valueCompositesList() );
        ValueCompositeType valueType = module.valueDescriptor( SomeValue.class.getName() ).valueType();
        CollectionType collectionType = new CollectionType( List.class, valueType );
        List<SomeValue> value = valueSerialization.deserialize( module, collectionType, output );
        assertEquals( valueCompositesList(), value );
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
        value.put( "gamma", Collections.<String>emptyList() );
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
        value.add( Collections.<String, String>emptyMap() );
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
}
