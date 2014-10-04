/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
 *
 */
package org.qi4j.test.value;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueDeserializer;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

/**
 * Assert that a JSON ValueDeserializer support various date formats.
 */
@SuppressWarnings( "ProtectedField" )
public class AbstractJsonDateFormatTest
    extends AbstractQi4jTest
{

    private final ValueType dateType = new ValueType( ZonedDateTime.class );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Before
    public void before()
    {
        module.injectTo( this );
    }
    @Service
    protected ValueDeserializer valueDeserializer;

    @Test
    public void givenISO6801DateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<ZonedDateTime> value = valueDeserializer.deserialize( collectionType, "[\"2009-08-12T14:54:27.895+08:00\"]" );
        assertEquals( ZonedDateTime.parse( "2009-08-12T06:54:27.895Z" ).toInstant(), value.get( 0 ).toInstant() );
    }

    @Test( expected = ValueSerializationException.class)
    public void givenAtDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        Instant tstamp = Instant.now();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Instant> value = valueDeserializer.deserialize( collectionType, "[\"@" + tstamp + "@\"]" );
    }

    @Test( expected = ValueSerializationException.class)
    public void givenMicrosoftDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        Instant tstamp = Instant.now();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Instant> value = valueDeserializer.deserialize( collectionType, "[\"/Date(" + tstamp + ")/\"]" );
    }
}
