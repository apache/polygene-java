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
 */
package org.qi4j.api.common;

import org.junit.Test;
import org.qi4j.api.util.NullArgumentException;

import static org.junit.Assert.assertEquals;

public class QualifiedNameTest
{
    @Test
    public void testQualifiedNameWithDollar()
    {
        assertEquals( "Name containing dollar is modified", "Test-Test",
                      new QualifiedName( TypeName.nameOf( "Test$Test" ), "satisfiedBy" ).type() );
    }

    @Test
    public void testQualifiedNameFromQNWithDollar()
    {
        assertEquals( "Name containing dollar is cleaned up", "Test-Test",
                      QualifiedName.fromFQN( "Test$Test:satisfiedBy" ).type() );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments1()
    {
        new QualifiedName( TypeName.nameOf( "Test" ), null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments2()
    {
        new QualifiedName( null, "satisfiedBy" );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments3()
    {
        new QualifiedName( null, null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments4()
    {
        QualifiedName.fromFQN( null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments5()
    {
        QualifiedName.fromAccessor( null );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments6()
    {
        QualifiedName.fromClass( null, "satisfiedBy" );
    }

    @Test( expected = NullArgumentException.class )
    public void nonNullArguments7()
    {
        QualifiedName.fromClass( null, null );
    }
}
