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
package org.apache.polygene.api.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class HasTypesCollectorsTest
{
    @Test
    public void selectMatchingTypes()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        List<ValueType> number = valueTypes.stream().collect( HasTypesCollectors.matchingTypes( Number.class ) );
        assertThat( number.size(), is( 2 ) );
        assertThat( number.get( 0 ), equalTo( ValueType.of( Number.class ) ) );
        assertThat( number.get( 1 ), equalTo( ValueType.of( Integer.class ) ) );

        List<ValueType> integer = valueTypes.stream().collect( HasTypesCollectors.matchingTypes( Integer.class ) );
        assertThat( integer.size(), is( 1 ) );
        assertThat( integer.get( 0 ), equalTo( ValueType.of( Integer.class ) ) );
    }

    @Test
    public void selectMatchingType()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Double.class ),
            ValueType.of( Integer.class )
        );

        Optional<ValueType> number = valueTypes.stream()
                                               .collect( HasTypesCollectors.matchingType( Number.class ) );
        assertThat( number.isPresent(), is( true ) );
        assertThat( number.get(), equalTo( ValueType.of( Double.class ) ) );

        Optional<ValueType> integer = valueTypes.stream()
                                                .collect( HasTypesCollectors.matchingType( Integer.class ) );
        assertThat( integer.isPresent(), is( true ) );
        assertThat( integer.get(), equalTo( ValueType.of( Integer.class ) ) );
    }

    @Test
    public void selectMatchingValueTypes()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Number.class, Integer.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        List<ValueType> number = valueTypes.stream()
                                           .collect( HasTypesCollectors.matchingTypes( ValueType.of( Number.class ) ) );
        System.out.println( number );
        assertThat( number.size(), is( 2 ) );
        assertThat( number.get( 0 ), equalTo( ValueType.of( Number.class ) ) );
        assertThat( number.get( 1 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );

        List<ValueType> integer = valueTypes.stream()
                                            .collect(
                                                HasTypesCollectors.matchingTypes( ValueType.of( Integer.class ) ) );
        assertThat( integer.size(), is( 2 ) );
        assertThat( integer.get( 0 ), equalTo( ValueType.of( Integer.class ) ) );
        assertThat( integer.get( 1 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );

        List<ValueType> both = valueTypes.stream()
                                         .collect( HasTypesCollectors.matchingTypes( ValueType.of( Number.class,
                                                                                                   Integer.class ) ) );
        assertThat( both.size(), is( 1 ) );
        assertThat( both.get( 0 ), equalTo( ValueType.of( Number.class, Integer.class ) ) );
    }

    @Test
    public void selectMatchingValueType()
    {
        List<ValueType> valueTypes = Arrays.asList(
            ValueType.of( String.class ),
            ValueType.of( Number.class, Integer.class ),
            ValueType.of( Integer.class ),
            ValueType.of( Number.class )
        );

        Optional<ValueType> number = valueTypes.stream()
                                               .collect(
                                                   HasTypesCollectors.matchingType( ValueType.of( Number.class ) ) );
        assertThat( number.isPresent(), is( true ) );
        assertThat( number.get(), equalTo( ValueType.of( Number.class ) ) );

        Optional<ValueType> integer = valueTypes.stream()
                                                .collect(
                                                    HasTypesCollectors.matchingType( ValueType.of( Integer.class ) ) );
        assertThat( integer.isPresent(), is( true ) );
        assertThat( integer.get(), equalTo( ValueType.of( Integer.class ) ) );

        Optional<ValueType> both = valueTypes.stream()
                                             .collect( HasTypesCollectors.matchingType( ValueType.of( Number.class,
                                                                                                      Integer.class ) ) );
        assertThat( both.isPresent(), is( true ) );
        assertThat( both.get(), equalTo( ValueType.of( Number.class, Integer.class ) ) );
    }

    @Test
    public void selectClosestValueTypes()
    {
        List<ValueType> list = new ArrayList<ValueType>()
        {{
            add( ValueType.of( String.class ) );
            add( ValueType.of( Identity.class ) );
        }};

        List<ValueType> result = list.stream()
                                     .collect( HasTypesCollectors.closestTypes( StringIdentity.class ) );
        assertThat( result.size(), is( 1 ) );
        assertThat( result.get( 0 ), equalTo( ValueType.of( Identity.class ) ) );
    }
}
