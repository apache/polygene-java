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
package org.apache.zest.functional;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of Iterables utility methods
 */
public class IterablesTest
{
    private List<String> numbers = Arrays.asList( "1", "2", "3" );
    private Iterable<Long> numberLongs = Arrays.asList( 1L, 2L, 3L );

    @Test
    public void testAddAll()
    {
        List<String> strings = Iterables.toList( numbers );
        assertThat( strings.toString(), equalTo( "[1, 2, 3]" ) );
        assertThat( Iterables.toList( numberLongs ).toString(), equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testCount()
    {
        assertThat( Iterables.count( numbers ), equalTo( 3L ) );
    }
}
