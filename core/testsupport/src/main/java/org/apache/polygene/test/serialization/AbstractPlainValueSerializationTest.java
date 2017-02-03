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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
public abstract class AbstractPlainValueSerializationTest
    extends AbstractPolygeneTest
{
    @Service
    protected Serialization stateSerialization;

    @Override
    public void assemble( ModuleAssembly module )
    {
    }

    @Test
    public void givenEmptyStateStringWhenDeserializingExpectSuccesses()
    {
        assertThat( stateSerialization.deserialize( module, ValueType.of( Integer.class ), "" ), is( 0 ) );
        assertThat( stateSerialization.deserialize( module, ValueType.of( String.class ), "" ), equalTo( "" ) );
    }

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
    public void givenEnumValueWhenSerializingAndDeserializingExpectEquals()
    {
        String output = stateSerialization.serialize( SomeEnum.BÆR );
        System.out.println( output );
        SomeEnum value = stateSerialization.deserialize( module, EnumType.of( SomeEnum.class ), output );
        assertThat( value, is( SomeEnum.BÆR ) );
    }

    @Test
    public void givenCharacterValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( '∫' );
        System.out.println(serialized);
        assertThat( "Serialized", serialized, equalTo( "∫" ) );

        Character deserialized = stateSerialization.deserialize( module, Character.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( '∫' ) );

        deserialized = stateSerialization.deserialize( module, char.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( '∫' ) );
    }

    @Test
    public void givenEmptyStringValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( "" );
        assertThat( "Serialized", serialized, equalTo( "" ) );

        String deserialized = stateSerialization.deserialize( module, String.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( "" ) );
    }

    @Test
    public void givenStringValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( "Å∫" );
        assertThat( serialized, equalTo( "Å∫" ) );

        String deserialized = stateSerialization.deserialize( module, String.class, serialized );
        assertThat( deserialized, equalTo( "Å∫" ) );
    }

    @Test
    public void givenBooleanValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( true );
        assertThat( serialized, equalTo( "true" ) );

        Boolean deserialized = stateSerialization.deserialize( module, Boolean.class, serialized );
        assertThat( deserialized, equalTo( Boolean.TRUE ) );
    }

    @Test
    public void givenIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42 );
        assertThat( serialized, equalTo( "42" ) );
        Integer deserialized = stateSerialization.deserialize( module, Integer.class, serialized );
        assertThat( deserialized, equalTo( 42 ) );
    }

    @Test
    public void givenLongValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42L );
        assertThat( serialized, equalTo( "42" ) );

        Long deserialized = stateSerialization.deserialize( module, Long.class, serialized );
        assertThat( deserialized, equalTo( 42L ) );
    }

    @Test
    public void givenShortValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( (short) 42 );
        assertThat( serialized, equalTo( "42" ) );

        Short deserialized = stateSerialization.deserialize( module, Short.class, serialized );
        assertThat( deserialized, equalTo( (short) 42 ) );
    }

    @Test
    public void givenByteValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( (byte) 42 );
        assertThat( serialized, equalTo( "42" ) );
        Byte deserialized = stateSerialization.deserialize( module, Byte.class, serialized );
        assertThat( deserialized, equalTo( (byte) 42 ) );
    }

    @Test
    public void givenFloatValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42F );
        assertThat( serialized, equalTo( "42.0" ) );

        Float deserialized = stateSerialization.deserialize( module, Float.class, serialized );
        assertThat( deserialized, equalTo( 42F ) );
    }

    @Test
    public void givenDoubleValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( 42D );
        assertThat( serialized, equalTo( "42.0" ) );

        Double deserialized = stateSerialization.deserialize( module, Double.class, serialized );
        assertThat( deserialized, equalTo( 42D ) );
    }

    @Test
    public void givenBigIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigInteger bigInteger = new BigInteger( "42424242424242424242424242" );
        assertThat( bigInteger, not( equalTo( BigInteger.valueOf( bigInteger.longValue() ) ) ) );

        String serialized = stateSerialization.serialize( bigInteger );
        assertThat( serialized, equalTo( "42424242424242424242424242" ) );

        BigInteger deserialized = stateSerialization.deserialize( module, BigInteger.class, serialized );
        assertThat( deserialized, equalTo( bigInteger ) );
    }

    @Test
    public void givenBigDecimalValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigDecimal bigDecimal = new BigDecimal( "42.2376931348623157e+309" );
        assertThat( bigDecimal.doubleValue(), equalTo( Double.POSITIVE_INFINITY ) );

        String serialized = stateSerialization.serialize( bigDecimal );
        assertThat( serialized, equalTo( "4.22376931348623157E+310" ) );

        BigDecimal deserialized = stateSerialization.deserialize( module, BigDecimal.class, serialized );
        assertThat( deserialized, equalTo( bigDecimal ) );
    }

    @Test
    public void givenDateTimeValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize(
            OffsetDateTime.of( 2020, 3, 4, 13, 24, 35, 123000000, ZoneOffset.ofHours( 1 ) ) );
        assertThat( serialized, equalTo( "2020-03-04T13:24:35.123+01:00" ) );
        ZonedDateTime deserialized = stateSerialization.deserialize( module, ZonedDateTime.class, serialized );
        assertThat( deserialized,
                    equalTo( ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 123000000, ZoneOffset.ofHours( 1 ) ) ) );
    }

    @Test
    public void givenLocalDateTimeValueWhenSerializingAndDeserializingExpectEquals()
    {
        // Serialized without TimeZone
        String serialized = stateSerialization.serialize( LocalDateTime.of( 2020, 3, 4, 13, 23, 12 ) );
        assertThat( serialized, equalTo( "2020-03-04T13:23:12" ) );

        LocalDateTime deserialized = stateSerialization.deserialize( module, LocalDateTime.class, serialized );
        assertThat( deserialized, equalTo( LocalDateTime.of( 2020, 3, 4, 13, 23, 12 ) ) );
    }

    @Test
    public void givenLocalDateValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( LocalDate.of( 2020, 3, 4 ) );
        assertThat( serialized, equalTo( "2020-03-04" ) );

        LocalDate deserialized = stateSerialization.deserialize( module, LocalDate.class, serialized );
        assertThat( deserialized, equalTo( LocalDate.of( 2020, 3, 4 ) ) );
    }

    @Test
    public void givenEntityReferenceValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = stateSerialization.serialize( EntityReference.parseEntityReference( "ABCD-1234" ) );
        assertThat( serialized, equalTo( "ABCD-1234" ) );

        EntityReference deserialized = stateSerialization.deserialize( module, EntityReference.class, serialized );
        assertThat( deserialized, equalTo( EntityReference.parseEntityReference( "ABCD-1234" ) ) );
    }

    private enum SomeEnum
    {
        BÆR,
        BAZAR
    }
}
