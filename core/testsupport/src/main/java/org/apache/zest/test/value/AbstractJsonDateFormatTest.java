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

import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.value.ValueDeserializer;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.*;

/**
 * Assert that a JSON ValueDeserializer support various date formats.
 */
@SuppressWarnings( "ProtectedField" )
public class AbstractJsonDateFormatTest
    extends AbstractZestTest
{

    private final ValueType dateType = new ValueType( Date.class );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Service
    protected ValueDeserializer valueDeserializer;

    @Test
    public void givenISO6801DateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Date> value = valueDeserializer.deserialize( module, collectionType, "[\"2009-08-12T14:54:27.895+0800\"]" );
        assertEquals( new DateTime( "2009-08-12T06:54:27.895Z", DateTimeZone.UTC ).toDate(), value.get( 0 ) );
    }

    @Test
    public void givenAtDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        long tstamp = System.currentTimeMillis();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Date> value = valueDeserializer.deserialize( module, collectionType, "[\"@" + tstamp + "@\"]" );
        assertEquals( new Date( tstamp ), value.get( 0 ) );
    }

    @Test
    public void givenMicrosoftDateFormatWhenConvertingFromSerializedStateExpectValidDate()
        throws Exception
    {
        long tstamp = System.currentTimeMillis();
        CollectionType collectionType = new CollectionType( List.class, dateType );
        List<Date> value = valueDeserializer.deserialize( module, collectionType, "[\"/Date(" + tstamp + ")/\"]" );
        assertEquals( new Date( tstamp ), value.get( 0 ) );
    }
}
