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
package org.apache.zest.test.value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.test.AbstractZestTest;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.DateTimeZone.forOffsetHours;
import static org.junit.Assert.assertThat;

/**
 * Assert that ValueSerialization behaviour on plain values is correct.
 */
public abstract class AbstractPlainValueSerializationTest
    extends AbstractZestTest
{

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

    @Test
    public void givenCharacterValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( 'q' );
        assertThat( "Serialized", serialized, equalTo( "q" ) );

        Character deserialized = valueSerialization.deserialize( module, Character.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( 'q' ) );
    }

    @Test
    public void givenEmptyStringValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( "" );
        assertThat( "Serialized", serialized, equalTo( "" ) );

        String deserialized = valueSerialization.deserialize( module, String.class, serialized );
        assertThat( "Deserialized", deserialized, equalTo( "" ) );
    }

    @Test
    public void givenStringValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( "test" );
        assertThat( serialized, equalTo( "test" ) );

        String deserialized = valueSerialization.deserialize( module, String.class, serialized );
        assertThat( deserialized, equalTo( "test" ) );
    }

    @Test
    public void givenBooleanValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( Boolean.TRUE );
        assertThat( serialized, equalTo( "true" ) );

        Boolean deserialized = valueSerialization.deserialize( module, Boolean.class, serialized );
        assertThat( deserialized, equalTo( Boolean.TRUE ) );
    }

    @Test
    public void givenIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( 42 );
        assertThat( serialized, equalTo( "42" ) );
        Integer deserialized = valueSerialization.deserialize( module, Integer.class, serialized );
        assertThat( deserialized, equalTo( 42 ) );
    }

    @Test
    public void givenLongValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( 42L );
        assertThat( serialized, equalTo( "42" ) );

        Long deserialized = valueSerialization.deserialize( module, Long.class, serialized );
        assertThat( deserialized, equalTo( 42L ) );
    }

    @Test
    public void givenShortValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( (short) 42 );
        assertThat( serialized, equalTo( "42" ) );

        Short deserialized = valueSerialization.deserialize( module, Short.class, serialized );
        assertThat( deserialized, equalTo( (short) 42 ) );
    }

    @Test
    public void givenByteValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( (byte) 42 );
        assertThat( serialized, equalTo( "42" ) );
        Byte deserialized = valueSerialization.deserialize( module, Byte.class, serialized );
        assertThat( deserialized, equalTo( (byte) 42 ) );
    }

    @Test
    public void givenFloatValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( 42F );
        assertThat( serialized, equalTo( "42.0" ) );

        Float deserialized = valueSerialization.deserialize( module, Float.class, serialized );
        assertThat( deserialized, equalTo( 42F ) );
    }

    @Test
    public void givenDoubleValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( 42D );
        assertThat( serialized, equalTo( "42.0" ) );

        Double deserialized = valueSerialization.deserialize( module, Double.class, serialized );
        assertThat( deserialized, equalTo( 42D ) );
    }

    @Test
    public void givenBigIntegerValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigInteger bigInteger = new BigInteger( "42424242424242424242424242" );
        assertThat( bigInteger, not( equalTo( BigInteger.valueOf( bigInteger.longValue() ) ) ) );

        String serialized = valueSerialization.serialize( bigInteger );
        assertThat( serialized, equalTo( "42424242424242424242424242" ) );

        BigInteger deserialized = valueSerialization.deserialize( module, BigInteger.class, serialized );
        assertThat( deserialized, equalTo( bigInteger ) );
    }

    @Test
    public void givenBigDecimalValueWhenSerializingAndDeserializingExpectEquals()
    {
        BigDecimal bigDecimal = new BigDecimal( "42.2376931348623157e+309" );
        assertThat( bigDecimal.doubleValue(), equalTo( Double.POSITIVE_INFINITY ) );
        
        String serialized = valueSerialization.serialize( bigDecimal );
        assertThat( serialized, equalTo( "4.22376931348623157E+310" ) );

        BigDecimal deserialized = valueSerialization.deserialize( module, BigDecimal.class, serialized );
        assertThat( deserialized, equalTo( bigDecimal ) );
    }

    @Test
    public void givenDateValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( new DateTime( "2020-03-04T13:24:35", forID( "CET" ) ).toDate() );
        assertThat( serialized, equalTo( "2020-03-04T12:24:35.000Z" ) );

        Date deserialized = valueSerialization.deserialize( module, Date.class, serialized );
        assertThat( deserialized, equalTo( new DateTime( "2020-03-04T13:24:35", forID( "CET" ) ).toDate() ) );
        assertThat( deserialized, equalTo( new DateTime( "2020-03-04T12:24:35", UTC ).toDate() ) );
    }

    @Test
    public void givenDateTimeValueWhenSerializingAndDeserializingExpectEquals()
    {
        // We specify the TimeZone explicitely here so that serialized/deserialized is equals
        // See https://github.com/JodaOrg/joda-time/issues/106
        String serialized = valueSerialization.serialize( new DateTime( "2020-03-04T13:24:35", forOffsetHours( 1 ) ) );
        assertThat( serialized, equalTo( "2020-03-04T13:24:35.000+01:00" ) );
        DateTime deserialized = valueSerialization.deserialize( module, DateTime.class, serialized );
        assertThat( deserialized, equalTo( new DateTime( "2020-03-04T13:24:35", forOffsetHours( 1 ) ) ) );
    }

    @Test
    public void givenLocalDateTimeValueWhenSerializingAndDeserializingExpectEquals()
    {
        // Serialized without TimeZone
        String serialized = valueSerialization.serialize( new LocalDateTime( "2020-03-04T13:23:00", forID( "CET" ) ) );
        assertThat( serialized, equalTo( "2020-03-04T13:23:00.000" ) );

        LocalDateTime deserialized = valueSerialization.deserialize( module, LocalDateTime.class, serialized );
        assertThat( deserialized, equalTo( new LocalDateTime( "2020-03-04T13:23:00", UTC ) ) );
    }

    @Test
    public void givenLocalDateValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( new LocalDate( "2020-03-04" ) );
        assertThat( serialized, equalTo( "2020-03-04" ) );

        LocalDate deserialized = valueSerialization.deserialize( module, LocalDate.class, serialized );
        assertThat( deserialized, equalTo( new LocalDate( "2020-03-04" ) ) );
    }

    @Test
    public void givenEntityReferenceValueWhenSerializingAndDeserializingExpectEquals()
    {
        String serialized = valueSerialization.serialize( EntityReference.parseEntityReference( "ABCD-1234" ) );
        assertThat( serialized, equalTo( "ABCD-1234" ) );

        EntityReference deserialized = valueSerialization.deserialize( module, EntityReference.class, serialized );
        assertThat( deserialized, equalTo( EntityReference.parseEntityReference( "ABCD-1234" ) ) );
    }
}
