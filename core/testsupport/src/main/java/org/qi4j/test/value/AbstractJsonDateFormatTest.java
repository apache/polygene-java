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

import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueDeserializer;
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

    private final ValueType dateType = new ValueType( Date.class );

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
        List<Date> value = valueDeserializer.deserialize( collectionType, "[\"2009-08-12T14:54:27.895+0800\"]" );
        assertEquals( new DateTime( "2009-08-12T06:54:27.895Z", DateTimeZone.UTC ).toDate(), value.get( 0 ) );
    }

    @Test
    public void givenAtDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        long tstamp = System.currentTimeMillis();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Date> value = valueDeserializer.deserialize( collectionType, "[\"@" + tstamp + "@\"]" );
        assertEquals( new Date( tstamp ), value.get( 0 ) );
    }

    @Test
    public void givenMicrosoftDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        long tstamp = System.currentTimeMillis();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Date> value = valueDeserializer.deserialize( collectionType, "[\"/Date(" + tstamp + ")/\"]" );
        assertEquals( new Date( tstamp ), value.get( 0 ) );
    }
}
