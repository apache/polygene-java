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
 */
package org.apache.polygene.api.util;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CollectorsTest
{
    @Test
    public void single()
    {
        assertThat( Stream.of( 1L ).collect( Collectors.single() ), is( 1L ) );

        try
        {
            Stream.of().collect( Collectors.single() );
            fail( "Should have failed" );
        }
        catch( IllegalArgumentException ex ) {}
        try
        {
            Stream.of( 1, 1 ).collect( Collectors.single() );
            fail( "Should have failed" );
        }
        catch( IllegalArgumentException ex ) {}
        try
        {
            Stream.of( 1, 1, 1 ).collect( Collectors.single() );
            fail( "Should have failed" );
        }
        catch( IllegalArgumentException ex ) {}
    }

    @Test
    public void singleOrEmpty()
    {
        assertEquals( Optional.empty(), Stream.of().collect( Collectors.singleOrEmpty() ) );
        assertEquals( Optional.of( 1 ), Stream.of( 1 ).collect( Collectors.singleOrEmpty() ) );

        try
        {
            Stream.of( 1, 1 ).collect( Collectors.singleOrEmpty() );
            fail( "Should have failed" );
        }
        catch( IllegalArgumentException ex ) {}
        try
        {
            Stream.of( 1, 1, 1 ).collect( Collectors.singleOrEmpty() );
            fail( "Should have failed" );
        }
        catch( IllegalArgumentException ex ) {}
    }
}
