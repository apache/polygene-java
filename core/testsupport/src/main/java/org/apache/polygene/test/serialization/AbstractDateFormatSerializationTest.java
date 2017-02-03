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
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that a serialization support various date formats.
 */
@SuppressWarnings( "ProtectedField" )
public class AbstractDateFormatSerializationTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
    }

    @Service
    protected Serialization stateSerialization;

    @Test
    public void givenLocalDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.LOCAL_DATE_TIME );
        List<LocalDateTime> value = stateSerialization.deserialize( module, collectionType,
                                                                    "[\"2009-08-12T14:54:27\"]" );
        LocalDateTime expected = LocalDateTime.of( 2009, 8, 12, 14, 54, 27 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenLocalDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.LOCAL_DATE );
        List<LocalDate> value = stateSerialization.deserialize( module, collectionType, "[\"2009-08-12\"]" );
        LocalDate expected = LocalDate.of( 2009, 8, 12 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenLocalTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.LOCAL_TIME );
        List<LocalTime> value = stateSerialization.deserialize( module, collectionType, "[\"14:54:27\"]" );
        LocalTime expected = LocalTime.of( 14, 54, 27 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenOffsetDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.OFFSET_DATE_TIME );
        List<OffsetDateTime> value = stateSerialization.deserialize( module, collectionType,
                                                                     "[\"2009-08-12T14:54:27.895+08:00\"]" );
        OffsetDateTime expected = OffsetDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000, ZoneOffset.ofHours( 8 ) );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenZonedDateTimeFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.ZONED_DATE_TIME );
        List<ZonedDateTime> value = stateSerialization.deserialize( module, collectionType,
                                                                    "[\"2009-08-12T14:54:27.895+02:00[CET]\"]" );
        ZonedDateTime expected = ZonedDateTime.of( 2009, 8, 12, 14, 54, 27, 895000000, ZoneId.of( "CET" ) );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenInstantFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.INSTANT );
        List<Instant> value = stateSerialization.deserialize( module, collectionType,
                                                              "[\"2016-06-11T08:47:12.620Z\"]" );
        Instant expected = Instant.parse( "2016-06-11T08:47:12.620Z" );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenDurationFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.DURATION );
        List<Duration> value = stateSerialization.deserialize( module, collectionType, "[\"PT3.5S\"]" );
        Duration expected = Duration.ofMillis( 3500 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }

    @Test
    public void givenPeriodFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = CollectionType.listOf( ValueType.PERIOD );
        List<Period> value = stateSerialization.deserialize( module, collectionType, "[\"P3Y5M13D\"]" );
        Period expected = Period.of( 3, 5, 13 );
        assertThat( value.get( 0 ), equalTo( expected ) );
    }
}
