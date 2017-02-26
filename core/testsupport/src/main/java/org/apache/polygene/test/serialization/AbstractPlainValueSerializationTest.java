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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Assert that ValueSerialization behaviour on plain values is correct.
 */
public abstract class AbstractPlainValueSerializationTest extends AbstractPolygeneTest
{
    @Service
    protected Serialization stateSerialization;

    @Override
    public void assemble( ModuleAssembly module )
    {
    }

    protected abstract String getSingleStringRawState( String state ) throws Exception;

    @Test
    public void givenNullValueWhenSerializingAndDeserializingExpectNull()
    {
        String output = stateSerialization.serialize( null );
        System.out.println( output );

        assertThat( stateSerialization.deserialize( module, ValueType.of( Integer.class ), output ), nullValue() );
        assertThat( stateSerialization.deserialize( module, ValueType.of( String.class ), output ), nullValue() );
        assertThat( stateSerialization.deserialize( module, ValueType.of( SomeEnum.class ), output ), nullValue() );
    }

    @Test
    public void givenEnumValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String output = stateSerialization.serialize( SomeEnum.BÆR );
        System.out.println( output );
        assertThat( getSingleStringRawState( output ), equalTo( "BÆR" ) );

        SomeEnum value = stateSerialization.deserialize( module, EnumType.of( SomeEnum.class ), output );
        assertThat( value, is( SomeEnum.BÆR ) );
    }

    @Test
    public void givenPrimitiveValueWhenSerializingAndDeserializingUsingPrimitiveAndBoxedTypesExpectEquals()
    {
        assertPrimitiveBoxedDeserializationEquals( char.class, Character.class, '€' );
        assertPrimitiveBoxedDeserializationEquals( boolean.class, Boolean.class, true );
        assertPrimitiveBoxedDeserializationEquals( short.class, Short.class, (short) 23 );
        assertPrimitiveBoxedDeserializationEquals( int.class, Integer.class, 23 );
        assertPrimitiveBoxedDeserializationEquals( byte.class, Byte.class, (byte) 23 );
        assertPrimitiveBoxedDeserializationEquals( long.class, Long.class, 23L );
        assertPrimitiveBoxedDeserializationEquals( float.class, Float.class, 23F );
        assertPrimitiveBoxedDeserializationEquals( double.class, Double.class, 23D );
    }

    private <P, B> void assertPrimitiveBoxedDeserializationEquals( Class<P> primitiveType, Class<B> boxedType, P value )
    {
        String serialized = stateSerialization.serialize( value );
        System.out.println( serialized );

        B boxed = stateSerialization.deserialize( module, boxedType, serialized );
        P primitive = stateSerialization.deserialize( module, primitiveType, serialized );
        assertThat( "Primitive/Boxed", boxed, equalTo( primitive ) );
    }

    @Test
    public void givenCharacterValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( '∫' );
        System.out.println( serialized );

        Character deserialized = stateSerialization.deserialize( module, Character.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( '∫' ) );

        deserialized = stateSerialization.deserialize( module, char.class, serialized );
        assertThat( "Deserialized", deserialized, is( '∫' ) );
    }

    @Test
    public void givenEmptyStringValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( "" );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "" ) );

        String deserialized = stateSerialization.deserialize( module, String.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( "" ) );
    }

    @Test
    public void givenStringValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( "Å∫" );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "Å∫" ) );

        String deserialized = stateSerialization.deserialize( module, String.class, serialized );
        assertThat( deserialized, equalTo( "Å∫" ) );
    }

    @Test
    public void givenBooleanValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( true );
        System.out.println( serialized );

        Boolean deserialized = stateSerialization.deserialize( module, Boolean.class, serialized );
        assertThat( deserialized, equalTo( Boolean.TRUE ) );
    }

    @Test
    public void givenIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42 );
        System.out.println( serialized );

        Integer deserialized = stateSerialization.deserialize( module, Integer.class, serialized );
        assertThat( deserialized, equalTo( 42 ) );
    }

    @Test
    public void givenLongValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42L );
        System.out.println( serialized );

        Long deserialized = stateSerialization.deserialize( module, Long.class, serialized );
        assertThat( deserialized, equalTo( 42L ) );
    }

    @Test
    public void givenShortValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( (short) 42 );
        System.out.println( serialized );

        Short deserialized = stateSerialization.deserialize( module, Short.class, serialized );
        assertThat( deserialized, equalTo( (short) 42 ) );
    }

    @Test
    public void givenByteValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( (byte) 42 );
        System.out.println( serialized );

        Byte deserialized = stateSerialization.deserialize( module, Byte.class, serialized );
        assertThat( deserialized, equalTo( (byte) 42 ) );
    }

    @Test
    public void givenFloatValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42F );
        System.out.println( serialized );

        Float deserialized = stateSerialization.deserialize( module, Float.class, serialized );
        assertThat( deserialized, equalTo( 42F ) );
    }

    @Test
    public void givenDoubleValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42D );
        System.out.println( serialized );

        Double deserialized = stateSerialization.deserialize( module, Double.class, serialized );
        assertThat( deserialized, equalTo( 42D ) );
    }

    @Test
    public void givenBigIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigInteger bigInteger = new BigInteger( "42424242424242424242424242" );
        assertThat( bigInteger, not( equalTo( BigInteger.valueOf( bigInteger.longValue() ) ) ) );

        String serialized = stateSerialization.serialize( bigInteger );
        System.out.println( serialized );

        BigInteger deserialized = stateSerialization.deserialize( module, BigInteger.class, serialized );
        assertThat( deserialized, equalTo( bigInteger ) );
    }

    @Test
    public void givenBigDecimalValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigDecimal bigDecimal = new BigDecimal( "42.2376931348623157e+309" );
        assertThat( bigDecimal.doubleValue(), equalTo( Double.POSITIVE_INFINITY ) );

        String serialized = stateSerialization.serialize( bigDecimal );
        System.out.println( serialized );

        BigDecimal deserialized = stateSerialization.deserialize( module, BigDecimal.class, serialized );
        assertThat( deserialized, equalTo( bigDecimal ) );
    }

    @Test
    public void givenLocalDateTimeValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        // Serialized without TimeZone
        String serialized = stateSerialization.serialize( LocalDateTime.of( 2020, 3, 4, 13, 23, 12 ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "2020-03-04T13:23:12" ) );

        LocalDateTime deserialized = stateSerialization.deserialize( module, LocalDateTime.class, serialized );
        assertThat( deserialized, equalTo( LocalDateTime.of( 2020, 3, 4, 13, 23, 12 ) ) );
    }

    @Test
    public void givenLocalDateValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( LocalDate.of( 2020, 3, 4 ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "2020-03-04" ) );

        LocalDate deserialized = stateSerialization.deserialize( module, LocalDate.class, serialized );
        assertThat( deserialized, equalTo( LocalDate.of( 2020, 3, 4 ) ) );
    }

    @Test
    public void givenLocalTimeValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( LocalTime.of( 14, 54, 27 ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "14:54:27" ) );

        LocalTime deserialized = stateSerialization.deserialize( module, LocalTime.class, serialized );
        assertThat( deserialized, equalTo( LocalTime.of( 14, 54, 27 ) ) );
    }

    @Test
    public void givenOffsetDateTimeValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( OffsetDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000,
                                                                             ZoneOffset.ofHours( 8 ) ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "2009-08-12T14:54:27.895+08:00" ) );

        OffsetDateTime deserialized = stateSerialization.deserialize( module, OffsetDateTime.class, serialized );
        assertThat( deserialized, equalTo( OffsetDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000,
                                                              ZoneOffset.ofHours( 8 ) ) ) );
    }

    @Test
    public void givenZonedDateTimeValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( ZonedDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000,
                                                                            ZoneId.of( "CET" ) ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "2009-08-12T14:54:27.895+02:00[CET]" ) );

        ZonedDateTime deserialized = stateSerialization.deserialize( module, ZonedDateTime.class, serialized );
        assertThat( deserialized, equalTo( ZonedDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000,
                                                             ZoneId.of( "CET" ) ) ) );
    }

    @Test
    public void givenInstantValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( Instant.parse( "2016-06-11T08:47:12.620Z" ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "2016-06-11T08:47:12.620Z" ) );

        Instant deserialized = stateSerialization.deserialize( module, Instant.class, serialized );
        assertThat( deserialized, equalTo( Instant.parse( "2016-06-11T08:47:12.620Z" ) ) );
    }

    @Test
    public void givenDurationValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( Duration.ofMillis( 3500 ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "PT3.5S" ) );

        Duration deserialized = stateSerialization.deserialize( module, Duration.class, serialized );
        assertThat( deserialized, equalTo( Duration.ofMillis( 3500 ) ) );
    }

    @Test
    public void givenPeriodValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( Period.of( 3, 5, 13 ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "P3Y5M13D" ) );

        Period deserialized = stateSerialization.deserialize( module, Period.class, serialized );
        assertThat( deserialized, equalTo( Period.of( 3, 5, 13 ) ) );
    }

    @Test
    public void givenEntityReferenceValueWhenSerializingAndDeserializingExpectEquals() throws Exception
    {
        String serialized = stateSerialization.serialize( EntityReference.parseEntityReference( "ABCD-1234" ) );
        System.out.println( serialized );
        assertThat( getSingleStringRawState( serialized ), equalTo( "ABCD-1234" ) );

        EntityReference deserialized = stateSerialization.deserialize( module, EntityReference.class, serialized );
        assertThat( deserialized, equalTo( EntityReference.parseEntityReference( "ABCD-1234" ) ) );
    }

    private enum SomeEnum
    {
        BÆR,
        BAZAR
    }
}
