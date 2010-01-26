/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.property;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test for DefaultValues
 */
public class DefaultValuesTest
{
    @Test
    public void givenDefaultValuesWhenRequestStringThenGetEmptyString()
    {
        assertThat( "Empty string", (String) DefaultValues.getDefaultValue( String.class ), equalTo( "" ) );
    }

    @Test
    public void givenDefaultValuesWhenRequestListThenGetEmptyList()
    {
        assertThat( "Empty list", (List<Object>) DefaultValues.getDefaultValue( List.class ), equalTo( Collections.emptyList() ) );
    }

    @Test
    public void givenDefaultValuesWhenRequestSetThenGetEmptySet()
    {
        assertThat( "Empty set", (Set<Object>) DefaultValues.getDefaultValue( Set.class ), equalTo( Collections.emptySet() ) );
    }

    @Test
    public void givenDefaultValuesWhenRequestCollectionThenGetEmptyCollection()
    {
        Collection<Object> coll = (Collection<Object>) DefaultValues.getDefaultValue( Collection.class );
        Collection<Object> empty = Collections.EMPTY_LIST;
        assertThat( "Empty collection", coll, equalTo( empty ) );
    }

    @Test
    public void givenDefaultValuesWhenRequestEnumThenGetFirstValue()
    {
        Object val = DefaultValues.getDefaultValue( ValueTest.class );
        Object value1 = ValueTest.VALUE1;
        assertThat( "Enum first value", val, equalTo( value1 ) );
    }

    public enum ValueTest
    {
        VALUE1, VALUE2, VALUE3
    }
}
