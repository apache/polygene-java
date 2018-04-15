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
package org.apache.polygene.api.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QualifiedNameTest
{
    @Test
    public void testQualifiedNameWithDollar()
    {
        assertThat( "Name containing dollar is modified",
                    new QualifiedName( TypeName.nameOf( "Test$Test" ), "satisfiedBy" ).type(),
                    equalTo( "Test-Test" )
        );
    }

    @Test
    public void testQualifiedNameFromQNWithDollar()
    {
        assertThat( "Name containing dollar is cleaned up",
                    QualifiedName.fromFQN( "Test$Test:satisfiedBy" ).type(),
                    equalTo( "Test-Test" ) );
    }

    @Test
    public void nonNullArguments1()
    {
        assertThrows( NullPointerException.class, () -> new QualifiedName( TypeName.nameOf( "Test" ), null ) );
    }

    @Test
    public void nonNullArguments2()
    {
        assertThrows( NullPointerException.class, () -> new QualifiedName( null, "satisfiedBy" ) );
    }

    @Test
    public void nonNullArguments3()
    {
        assertThrows( NullPointerException.class, () -> new QualifiedName( null, null ) );
    }

    @Test
    public void nonNullArguments4()
    {
        assertThrows( NullPointerException.class, () -> QualifiedName.fromFQN( null ) );
    }

    @Test
    public void nonNullArguments5()
    {
        assertThrows( NullPointerException.class, () -> QualifiedName.fromAccessor( null ) );
    }

    @Test
    public void nonNullArguments6()
    {
        assertThrows( NullPointerException.class, () -> QualifiedName.fromClass( null, "satisfiedBy" ) );
    }

    @Test
    public void nonNullArguments7()
    {
        assertThrows( NullPointerException.class, () -> QualifiedName.fromClass( null, null ) );
    }
}
