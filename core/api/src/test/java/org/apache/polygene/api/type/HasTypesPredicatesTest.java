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
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HasTypesPredicatesTest
{
    @Test
    public void hasEqualTypePredicate()
    {
        assertTrue( new HasEqualType<>( Integer.class )
                        .test( ValueType.of( Integer.class ) ) );
        assertTrue( new HasEqualType<>( Integer.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( Number.class )
                         .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );

        assertTrue( new HasEqualType<>( ValueType.of( Integer.class ) )
                        .test( ValueType.of( Integer.class ) ) );
        assertTrue( new HasEqualType<>( ValueType.of( Integer.class ) )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( ValueType.of( Number.class ) )
                         .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasEqualType<>( ValueType.of( String.class ) )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }

    @Test
    public void hasAssignableTypePredicate()
    {
        assertTrue( new HasAssignableFromType<>( Number.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertFalse( new HasAssignableFromType<>( Integer.class )
                         .test( ValueType.of( Integer.class ) ) );
        assertFalse( new HasAssignableFromType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }

    @Test
    public void hasEqualOrAssignablePredicate()
    {
        assertTrue( new HasEqualOrAssignableFromType<>( Number.class )
                        .test( ValueType.of( String.class, Integer.class ) ) );
        assertTrue( new HasEqualOrAssignableFromType<>( Integer.class )
                        .test( ValueType.of( Integer.class ) ) );
        assertFalse( new HasEqualOrAssignableFromType<>( String.class )
                         .test( ValueType.of( LocalDate.class, Integer.class ) ) );
    }
}
