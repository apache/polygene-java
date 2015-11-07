/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.bootstrap;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.zest.api.type.HasTypes;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AssemblySpecificationTest
{
    @Test
    public void givenSingleMatchingTypeWhenFilteringExpectTrue()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( String.class );
        HasTypes hasTypes = new MockedHasTyoes( String.class );
        assertThat( underTest.test( hasTypes ), equalTo(true) );
    }

    @Test
    public void givenMultipleMatchingTypeWhenFilteringExpectTrue()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( Long.class, BigDecimal.class, String.class );
        HasTypes hasTypes = new MockedHasTyoes( String.class );
        assertThat( underTest.test( hasTypes ), equalTo(true) );
    }

    @Test
    public void givenSingleNonMatchingTypeWhenFilteringExpectFalse()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( Long.class );
        HasTypes hasTypes = new MockedHasTyoes( Integer.class );
        assertThat( underTest.test( hasTypes ), equalTo(false) );
    }

    @Test
    public void givenMultipleMatchingTypeWhenFilteringAgainstMultipleExpectTrue()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( Long.class, Integer.class, BigDecimal.class );
        HasTypes hasTypes = new MockedHasTyoes( String.class, Integer.class );
        assertThat( underTest.test( hasTypes ), equalTo(true) );
    }

    @Test
    public void givenMultipleNonMatchingTypeWhenFilteringAgainstSingleExpectFalse()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( Long.class, BigDecimal.class, String.class );
        HasTypes hasTypes = new MockedHasTyoes( Integer.class );
        assertThat( underTest.test( hasTypes ), equalTo(false) );
    }

    @Test
    public void givenMultipleNonMatchingTypeWhenFilteringAgainstMultipleExpectFalse()
    {
        Predicate<HasTypes> underTest = AssemblySpecifications.ofAnyType( Long.class, BigDecimal.class );
        HasTypes hasTypes = new MockedHasTyoes( String.class, Integer.class );
        assertThat( underTest.test( hasTypes ), equalTo(false) );
    }

    private static class MockedHasTyoes
        implements HasTypes
    {
        private final Class[] types;

        public MockedHasTyoes( Class... types)
        {
            this.types = types;
        }

        @Override
        public Stream<Class<?>> types()
        {
            return Arrays.stream(types);
        }
    }
}
