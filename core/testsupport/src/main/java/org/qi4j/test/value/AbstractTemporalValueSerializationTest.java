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
package org.qi4j.test.value;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.*;

/**
 * Assert that a ValueSerialization support JodaTime times correctly.
 */
@SuppressWarnings( "ProtectedField" )
public abstract class AbstractTemporalValueSerializationTest
    extends AbstractQi4jTest
{

    private final ValueType jodaDateTimeType = new ValueType( DateTime.class );
    private final ValueType jodaLocalDateTimeType = new ValueType( LocalDateTime.class );
    private final ValueType jodaLocalDateType = new ValueType( LocalDate.class );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // NOOP
    }

    @Before
    public void before()
    {
        module.injectTo( this );
    }
    @Service
    protected ValueSerialization valueSerialization;

    @Test
    public void givenDateTimeTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Set<DateTime> value = Collections.singleton( new DateTime( "2020-03-04T13:24:35", UTC ) );
        String stateString = valueSerialization.serialize( value );
        assertEquals( asSingleValueArray( "2020-03-04T13:24:35.000Z" ), stateString );
    }

    @Test
    public void givenDateTimeTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, jodaDateTimeType );
        String serializedState = asSingleValueArray( "2020-03-04T12:23:33Z" );
        List<DateTime> value = valueSerialization.deserialize( collectionType, serializedState );
        assertEquals( new DateTime( "2020-03-04T12:23:33Z", UTC ), value.get( 0 ) );
    }

    @Test
    public void givenLocalDateTimeTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Set<LocalDateTime> value = Collections.singleton( new LocalDateTime( "2020-03-04T13:23:00", UTC ) );
        String stateString = valueSerialization.serialize( value );
        assertEquals( asSingleValueArray( "2020-03-04T13:23:00.000" ), stateString );
    }

    @Test
    public void givenLocalDateTimeTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, jodaLocalDateTimeType );
        String serializedState = asSingleValueArray( "2020-03-04T12:23:09" );
        List<LocalDateTime> value = valueSerialization.deserialize( collectionType, serializedState );
        assertEquals( new LocalDateTime( "2020-03-04T12:23:09", UTC ), value.get( 0 ) );
    }

    @Test
    public void givenLocalDateTypeWhenConvertingToJsonExpectValidString()
        throws Exception
    {
        Set<LocalDate> value = Collections.singleton( new LocalDate( "2020-03-04" ) );
        String stateString = valueSerialization.serialize( value );
        assertEquals( asSingleValueArray( "2020-03-04" ), stateString );
    }

    @Test
    public void givenLocalDateTypeWhenConvertingFromJsonExpectValidLocalDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, jodaLocalDateType );
        String serializedState = asSingleValueArray( "2020-03-04" );
        List<LocalDate> value = valueSerialization.deserialize( collectionType, serializedState );
        assertEquals( new LocalDate( "2020-03-04" ), value.get( 0 ) );
    }

    protected abstract String asSingleValueArray( String value );
}
