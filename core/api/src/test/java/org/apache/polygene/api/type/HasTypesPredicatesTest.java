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

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class HasTypesPredicatesTest
{
    @Test
    public void hasEqualTypePredicate()
    {
        assertThat( new HasEqualType<>( Integer.class ).test( ValueType.of( Integer.class ) ), is( true ) );
        assertThat( new HasEqualType<>( Integer.class ).test( ValueType.of( String.class, Integer.class ) ), is( true ) );
        assertThat( new HasEqualType<>( Number.class ).test( ValueType.of( String.class, Integer.class ) ), is( false ) );
        assertThat( new HasEqualType<>( String.class ).test( ValueType.of( LocalDate.class, Integer.class ) ), is( false ) );

        assertThat( new HasEqualType<>( ValueType.of( Integer.class ) ).test( ValueType.of( Integer.class ) ), is( true ) );
        assertThat( new HasEqualType<>( ValueType.of( Integer.class ) ).test( ValueType.of( String.class, Integer.class ) ), is( true ) );
        assertThat( new HasEqualType<>( ValueType.of( Number.class ) ).test( ValueType.of( String.class, Integer.class ) ), is( false ) );
        assertThat( new HasEqualType<>( ValueType.of( String.class ) ).test( ValueType.of( LocalDate.class, Integer.class ) ), is( false ) );
    }

    @Test
    public void hasAssignableTypePredicate()
    {
        assertThat( new HasAssignableFromType<>( Number.class ).test( ValueType.of( String.class, Integer.class ) ), is( true ) );
        assertThat( new HasAssignableFromType<>( Integer.class ).test( ValueType.of( Integer.class ) ), is( false ) );
        assertThat( new HasAssignableFromType<>( String.class ).test( ValueType.of( LocalDate.class, Integer.class ) ), is( false ) );
    }

    @Test
    public void hasEqualOrAssignablePredicate()
    {
        assertThat( new HasEqualOrAssignableFromType<>( Number.class ).test( ValueType.of( String.class, Integer.class ) ), is( true ) );
        assertThat( new HasEqualOrAssignableFromType<>( Integer.class ).test( ValueType.of( Integer.class ) ), is( true ) );
        assertThat( new HasEqualOrAssignableFromType<>( String.class ).test( ValueType.of( LocalDate.class, Integer.class ) ), is( false ) );
    }
}
