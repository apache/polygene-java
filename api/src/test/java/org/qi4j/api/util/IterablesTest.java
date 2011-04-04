/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.util;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.specification.Specifications;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.util.Iterables.last;
import static org.qi4j.api.util.Iterables.map;

/**
 * Test of Iterables utility methods
 */
public class IterablesTest
{
    private List<String> numbers = Arrays.asList( "1", "2", "3" );
    private Iterable<Long> numberLongs = Arrays.asList( 1L, 2L, 3L );
    private Iterable<Integer> numberIntegers = Arrays.asList(1, 2, 3);

    @Test
    public void testAddAll()
    {
        ArrayList<String> strings = Iterables.addAll( new ArrayList<String>(), numbers );
        assertThat( strings.toString(), equalTo( "[1, 2, 3]" ) );
        assertThat( Iterables.addAll( new ArrayList<Long>(), numberLongs ).toString(), equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testCount()
    {
        assertThat( Iterables.count( numbers ), equalTo( 3L ) );
    }

    @Test
    public void testFilter()
    {
        assertThat( Iterables.first( Iterables.filter( Specifications.in( "2" ), numbers ) ), equalTo( "2" ) );
    }

    @Test
    public void testFirst()
    {
        assertThat( Iterables.first( numbers ), equalTo( "1" ) );
        assertThat( Iterables.first( Collections.<Object>emptyList() ), CoreMatchers.<Object>nullValue() );
    }

    @Test
    public void testLast()
    {
        assertThat( last( numbers ), equalTo( "3" ) );
        assertThat( last( Collections.<Object>emptyList() ), CoreMatchers.<Object>nullValue() );
    }

    @Test
    public void testFolding()
    {
        assertThat( Iterables.fold( new Function<Integer, Integer>()
        {
            int sum = 0;

            @Override
            public Integer map( Integer number )
            {
                return sum += number;
            }
        }, numberIntegers ), equalTo( 6 ) );
    }

    @Test
    public void testReverse()
    {
        assertThat( Iterables.reverse( numbers ).toString(), equalTo( "[3, 2, 1]" ) );
        assertThat( Iterables.reverse( Collections.<Object>emptyList() ), equalTo( (Object) Collections.<Object>emptyList() ) );
    }

    @Test
    public void testMatchesAny()
    {
        assertThat( Iterables.matchesAny( Specifications.in( "2" ), numbers ), equalTo( true ) );
        assertThat( Iterables.matchesAny( Specifications.in( "4" ), numbers ), equalTo( false ) );
    }

    @Test
    public void testMatchesAll()
    {
        assertThat( Iterables.matchesAll( Specifications.in( "1","2","3" ), numbers ), equalTo( true ) );
        assertThat( Iterables.matchesAll( Specifications.in( "2","3","4" ), numbers ), equalTo( false ) );
    }

    @Test
    public void testFlatten()
    {
        assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.flatten( numbers, numbers ) )
                .toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );

        Iterable<? extends Number> flatten = Iterables.flatten( numberIntegers, numberLongs );
        assertThat( Iterables.addAll( new ArrayList<Number>(), flatten )
                .toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testFlattenIterables()
    {
      Iterable<List<String>> iterable = Iterables.iterable(numbers, numbers);
        assertThat( Iterables.addAll( new ArrayList<String>(),
                Iterables.flattenIterables( iterable ) )
                .toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testMap()
    {
        assertThat( Iterables.addAll( new ArrayList<String>(), map( new Function<String, String>()
        {
            public String map( String s )
            {
                return s + s;
            }
        }, numbers ) ).toString(), equalTo( "[11, 22, 33]" ) );


      Iterable<List<String>> numberIterable = Iterables.iterable(numbers, numbers, numbers);
        assertThat( Iterables.addAll( new ArrayList<Integer>(), map( new Function<Collection, Integer>()
        {
            @Override
            public Integer map( Collection collection )
            {
                return collection.size();
            }
        }, numberIterable ) ).toString(), equalTo( "[3, 3, 3]" ) );
    }

    @Test
    public void testIterableEnumeration()
    {

        Enumeration<String> enumeration = Collections.enumeration( numbers );
        assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.iterable( enumeration ) )
                .toString(), equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testIterableVarArg()
    {
        assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.iterable( "1", "2", "3" ) )
                .toString(), equalTo( "[1, 2, 3]" ) );
    }
}
