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
package org.apache.zest.functional;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.apache.zest.functional.ForEach.forEach;
import static org.apache.zest.functional.Functions.compose;
import static org.apache.zest.functional.Functions.count;
import static org.apache.zest.functional.Functions.indexOf;
import static org.apache.zest.functional.Functions.intSum;
import static org.apache.zest.functional.Functions.longSum;
import static org.apache.zest.functional.Iterables.iterable;
import static org.apache.zest.functional.Iterables.last;
import static org.apache.zest.functional.Iterables.map;
import static org.apache.zest.functional.Specifications.in;

/**
 * Test of utility functions
 */
public class FunctionsTest
{
    Function<Object, String> stringifier = new Function<Object, String>()
    {
        @Override
        public String apply( Object s )
        {
            return s.toString();
        }
    };

    Function<String, Integer> length = new Function<String, Integer>()
    {
        @Override
        public Integer apply( String s )
        {
            return s.length();
        }
    };

    @Test
    public void testCompose()
    {
        assertThat( Functions.<Object, String, Integer>compose()
                        .apply( length, stringifier )
                        .apply( 12345L ), equalTo( 5 ) );
        assertThat( compose( length, stringifier ).apply( 12345L ), equalTo( 5 ) );
    }

    @Test
    public void testFromMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put( "A", "1" );
        map.put( "B", "2" );
        map.put( "C", "3" );
        assertThat( Iterables.toList( Iterables.filter( Specifications.notNull(), Iterables.map( Functions.fromMap( map ), Iterables
            .iterable( "A", "B", "D" ) ) ) ).toString(), equalTo( "[1, 2]" ) );
    }

    @Test
    public void testWithDefault()
    {
        assertThat( Iterables.toList( Iterables.map( Functions.withDefault( "DEFAULT" ), Iterables.iterable( "123", null, "456" ) ) )
                        .toString(), equalTo( "[123, DEFAULT, 456]" ) );
    }

    @Test
    public void testLongSum()
    {
        assertThat( last( map( longSum(), iterable( 1, 2L, 3F, 4D ) ) ), equalTo( 10L ) );
    }

    @Test
    public void testLongSum2()
    {
        assertThat( forEach( iterable( 1, 2, 3, 4 ) ).map( longSum() ).last(), equalTo( 10L ) );
    }

    @Test
    public void testIntSum()
    {
        assertThat( last( map( intSum(), iterable( 1, 2L, 3F, 4D ) ) ), equalTo( 10 ) );
    }

    @Test
    public void testCount()
    {
        assertThat( last( map( count( in( "X" ) ), iterable( "X", "Y", "X", "X", "Y" ) ) ), equalTo( 3 ) );
    }

    @Test
    public void testIndexOf()
    {
        assertThat( last( map( indexOf( in( "D" ) ), iterable( "A", "B", "C", "D", "D" ) ) ), equalTo( 3 ) );
    }

    @Test
    public void testIndexOf2()
    {
        assertThat( indexOf( "D", iterable( "A", "B", "C", "D", "D" ) ), equalTo( 3 ) );
    }

    @Test
    public void testComparator()
    {
        Comparator<Integer> comparator = Functions.comparator( new Function<Integer, Comparable>()
        {
            @Override
            public Comparable apply( Integer integer )
            {
                return integer.toString();
            }
        } );
        Iterable<Integer> iterable = Iterables.iterable( 1, 5, 3, 6, 8 );
        List<Integer> integers = Iterables.toList( iterable );
        Collections.sort( integers, comparator );
        assertThat( integers.toString(), equalTo( "[1, 3, 5, 6, 8]" ) );
    }
}
