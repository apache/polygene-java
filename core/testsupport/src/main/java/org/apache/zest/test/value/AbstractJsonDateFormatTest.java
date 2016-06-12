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
import java.util.List;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.value.ValueDeserializer;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that a JSON ValueDeserializer support various date formats.
 */
@SuppressWarnings( "ProtectedField" )
public class AbstractJsonDateFormatTest
    extends AbstractZestTest
{

    private final ValueType offsetDateTimeType = new ValueType( OffsetDateTime.class );
    private final ValueType zonedDateTimeType = new ValueType( ZonedDateTime.class );
    private final ValueType localDateTimeType = new ValueType( LocalDateTime.class );
    private final ValueType localTimeType = new ValueType( LocalTime.class );
    private final ValueType localDateType = new ValueType( LocalDate.class );
    private final ValueType instantType = new ValueType( Instant.class );
    private final ValueType durationType = new ValueType( Duration.class );
    private final ValueType periodType = new ValueType( Period.class );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Service
    protected ValueDeserializer valueDeserializer;

    @Test
    public void givenLocalDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, localDateTimeType );
        List<LocalDateTime> value = valueDeserializer.deserialize( module, collectionType, "[\"2009-08-12T14:54:27\"]" );
        LocalDateTime expected = LocalDateTime.of( 2009, 8, 12, 14, 54, 27 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenLocalDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, localDateType );
        List<LocalDate> value = valueDeserializer.deserialize( module, collectionType, "[\"2009-08-12\"]" );
        LocalDate expected = LocalDate.of( 2009, 8, 12 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenLocalTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, localTimeType );
        List<LocalTime> value = valueDeserializer.deserialize( module, collectionType, "[\"14:54:27\"]" );
        LocalTime expected = LocalTime.of( 14, 54, 27 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenOffsetDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, offsetDateTimeType );
        List<OffsetDateTime> value = valueDeserializer.deserialize( module, collectionType, "[\"2009-08-12T14:54:27.895+08:00\"]" );
        OffsetDateTime expected = OffsetDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000, ZoneOffset.ofHours( 8 ) );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenZonedDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, zonedDateTimeType );
        List<ZonedDateTime> value = valueDeserializer.deserialize( module, collectionType, "[\"2009-08-12T14:54:27.895+02:00[CET]\"]" );
        ZonedDateTime expected = ZonedDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000, ZoneId.of( "CET" ) );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenInstantFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, instantType);
        List<Instant> value = valueDeserializer.deserialize( module, collectionType, "[\"2016-06-11T08:47:12.620Z\"]" );
        Instant expected = Instant.parse("2016-06-11T08:47:12.620Z" );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenDurationFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, durationType );
        List<Duration> value = valueDeserializer.deserialize( module, collectionType, "[\"PT3.5S\"]" );
        Duration expected = Duration.ofMillis( 3500 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenPeriodFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, periodType );
        List<ZonedDateTime> value = valueDeserializer.deserialize( module, collectionType, "[\"P3Y5M13D\"]" );
        Period expected = Period.of( 3, 5, 13);
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

}
