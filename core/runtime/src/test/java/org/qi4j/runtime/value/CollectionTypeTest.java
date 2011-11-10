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

package org.qi4j.runtime.value;

import org.json.JSONArray;
import org.junit.Test;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.json.JSONObjectSerializer;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.ValueType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class CollectionTypeTest
{

    @Test
    public void givenCollectionTypeWithByteAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Byte.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<Byte> value = byteCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( byteJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithShortAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Short.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<Short> value = shortCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( shortJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithIntegerAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Integer.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<Integer> value = integerCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( integerJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithLongAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Long.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<Long> value = longCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( longJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithFloatAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Float.class );
        CollectionType collectionType = new CollectionType( Collection.class , collectedType );

        Collection<Float> value = floatCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( floatJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithDoubleAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Double.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<Double> value = doubleCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( doubleJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithBigIntegerAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigInteger.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<BigInteger> value = bigIntegerCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( bigIntegerJson(), json.toString() );
    }

    @Test
    public void givenCollectionTypeWithBigDecimalAndNullElementWhenSerializingExpectCorrectJsonOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigDecimal.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );

        Collection<BigDecimal> value = bigDecimalCollection();
        JSONObjectSerializer serializer = new JSONObjectSerializer();
        serializer.serialize( value, collectionType );
        Object json = serializer.getRoot();
        assertEquals( bigDecimalJson(), json.toString() );
    }

    @Test
    public void givenJsonOfByteListWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Byte.class );
        CollectionType collectionType = new CollectionType( List.class, collectedType );
        Object json = new JSONArray( byteJson() );
        List<Byte> result = (List<Byte>) new JSONDeserializer( null ).deserialize( json, collectionType );
        ArrayList<Byte> bytes = byteCollection();
        for( int i = 0; i < result.size(); i++ )
        {
            Byte resultByte = result.get( i );
            if( resultByte != null )
            {
                assertEquals( Byte.class, resultByte.getClass() );
            }
            assertEquals( bytes.get( i ), resultByte );
        }
    }

    @Test
    public void givenJsonOfByteSetWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Byte.class );
        CollectionType collectionType = new CollectionType( Set.class, collectedType );
        Object json = new JSONArray( byteJson() );
        Set<Byte> result = (Set<Byte>) new JSONDeserializer( null ).deserialize( json, collectionType );
        Set<Byte> bytes = new LinkedHashSet<Byte>( byteCollection() );
        assertEquals( bytes, result );
    }

    @Test
    public void givenJsonOfByteCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Byte.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( byteJson() );
        List<Byte> result = (List<Byte>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( byteCollection(), result );
    }

    @Test
    public void givenJsonOfShortCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Short.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( shortJson() );
        List<Short> result = (List<Short>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( shortCollection(), result );
    }

    @Test
    public void givenJsonOfIntegerCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Integer.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( integerJson() );
        List<Integer> result = (List<Integer>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( integerCollection(), result );
    }

    @Test
    public void givenJsonOfLongCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Long.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( longJson() );
        List<Long> result = (List<Long>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( longCollection(), result );
    }

    @Test
    public void givenJsonOfFloatCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Float.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( floatJson() );
        List<Float> result = (List<Float>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( floatCollection(), result );
    }

    @Test
    public void givenJsonOfDoubleCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( Double.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( doubleJson() );
        List<Double> result = (List<Double>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( doubleCollection(), result );
    }

    @Test
    public void givenJsonOfBigIntegerCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigInteger.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( bigIntegerJson() );
        List<BigInteger> result = (List<BigInteger>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( bigIntegerCollection(), result );
    }

    @Test
    public void givenJsonOfBigDecimalCollectionWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigDecimal.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( bigDecimalJson() );
        List<BigDecimal> result = (List<BigDecimal>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( bigDecimalCollection(), result );
    }

    @Test
    public void givenJsonOfBigIntegerCollectionWithQuotesWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigInteger.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( bigIntegerJsonWithQuotes() );
        List<BigDecimal> result = (List<BigDecimal>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( bigIntegerCollection(), result );
    }

    @Test
    public void givenJsonOfBigDecimalCollectionWithQuotesWhenDeserializingExpectCorrectValueOutput()
        throws Exception
    {
        ValueType collectedType = new ValueType( BigDecimal.class );
        CollectionType collectionType = new CollectionType( Collection.class, collectedType );
        Object json = new JSONArray( bigDecimalJsonWithQuotes() );
        List<BigDecimal> result = (List<BigDecimal>) new JSONDeserializer( null ).deserialize( json, collectionType );
        assertEquals( bigDecimalCollection(), result );
    }

    private String byteJson()
    {
        return "[9,null,-12,-12,127,-128,73]";
    }

    private ArrayList<Byte> byteCollection()
    {
        ArrayList<Byte> value = new ArrayList<Byte>();
        value.add( (byte) 9 );
        value.add( null );
        value.add( (byte) -12 );
        value.add( (byte) -12 );
        value.add( (byte) 127 );
        value.add( (byte) -128 );
        value.add( (byte) 73 );
        return value;
    }

    private String shortJson()
    {
        return "[-32768,32767,-82,null]";
    }

    private Collection<Short> shortCollection()
    {
        Collection<Short> value = new ArrayList<Short>();
        value.add( (short) -32768 );
        value.add( (short) 32767 );
        value.add( (short) -82 );
        value.add( null );
        return value;
    }

    private String integerJson()
    {
        return "[2147483647,-283,null,-2147483648,238]";
    }

    private Collection<Integer> integerCollection()
    {
        Collection<Integer> value = new ArrayList<Integer>();
        value.add( Integer.MAX_VALUE );
        value.add( -283 );
        value.add( null );
        value.add( Integer.MIN_VALUE );
        value.add( 238 );
        return value;
    }

    private String longJson()
    {
        return "[98239723,-1298233,-1,0,null,1,9223372036854775807,-9223372036854775808]";
    }

    private Collection<Long> longCollection()
    {
        Collection<Long> value = new ArrayList<Long>();
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

    private String floatJson()
    {
        return "[-1,1,1,0,3.4028235E38,1.4E-45,null,0.123456,-0.232321]";
    }

    private Collection<Float> floatCollection()
    {
        Collection<Float> value = new ArrayList<Float>();
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

    private String doubleJson()
    {
        return "[-1,1,0,1.7976931348623157E308,null,4.9E-324,0.123456,-0.232321]";
    }

    private Collection<Double> doubleCollection()
    {
        Collection<Double> value = new ArrayList<Double>();
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

    private String bigIntegerJson()
    {
        return "[-1,0,1,null,10,-1827368263823729372397239829332,2398723982982379827373972398723]";
    }

    private String bigIntegerJsonWithQuotes()
    {
        return "[\"-1\",\"0\",\"1\",null,\"10\",\"-1827368263823729372397239829332\",\"2398723982982379827373972398723\"]";
    }

    private Collection<BigInteger> bigIntegerCollection()
    {
        Collection<BigInteger> value = new ArrayList<BigInteger>();
        value.add( new BigInteger( "-1" ) );
        value.add( BigInteger.ZERO );
        value.add( BigInteger.ONE );
        value.add( null );
        value.add( BigInteger.TEN );
        value.add( new BigInteger( "-1827368263823729372397239829332" ) );
        value.add( new BigInteger( "2398723982982379827373972398723" ) );
        return value;
    }

    private String bigDecimalJson()
    {
        return "[1.2,3.4,null,5.6]";
    }

    private String bigDecimalJsonWithQuotes()
    {
        return "[\"1.2\",\"3.4\",null,\"5.6\"]";
    }

    private Collection<BigDecimal> bigDecimalCollection()
    {
        Collection<BigDecimal> value = new ArrayList<BigDecimal>();
        value.add( new BigDecimal( "1.2" ) );
        value.add( new BigDecimal( "3.4" ) );
        value.add( null );
        value.add( new BigDecimal( "5.6" ) );
        return value;
    }
}
