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
package org.qi4j.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test of Iterables utility methods
 */
public class IterablesTest
{

    private List<String> numbers = Arrays.asList( "1", "2", "3" );
    private Iterable<Long> numberLongs = Arrays.asList( 1L, 2L, 3L );
    private Iterable<Integer> numberIntegers = Arrays.asList( 1, 2, 3 );

    @Test
    public void testConstant()
    {
        String str = "";

        for( String string : Iterables.limit( 3, Iterables.constant( "123" ) ) )
        {
            str += string;
        }

        assertThat( str, CoreMatchers.equalTo( "123123123" ) );
    }

    @Test
    public void testUnique()
    {
        String str = "";

        for( String string : Iterables.unique( Iterables.<String>flatten( numbers, numbers, numbers ) ) )
        {
            str += string;
        }
        assertThat( str, CoreMatchers.equalTo( "123" ) );
    }

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

    @Test
    public void testFilter()
    {
        assertThat( Iterables.first( Iterables.filter( Specifications.in( "2" ), numbers ) ), equalTo( "2" ) );
    }

    @Test
    public void testFirst()
    {
        assertThat( Iterables.first( numbers ), equalTo( "1" ) );
        assertThat( Iterables.first( emptyList() ), nullValue() );
    }

    @Test
    public void testLast()
    {
        assertThat( Iterables.last( numbers ), equalTo( "3" ) );
        assertThat( Iterables.last( emptyList() ), nullValue() );
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
    public void testAppend()
    {
        assertThat( Iterables.toList( Iterables.append( "C", Iterables.iterable( "A", "B" ) ) ).toString(),
                    equalTo( "[A, B, C]" ) );
    }

    @Test
    public void testReverse()
    {
        assertThat( Iterables.reverse( numbers ).toString(), equalTo( "[3, 2, 1]" ) );
        assertThat( Iterables.reverse( emptyList() ), equalTo( (Object) emptyList() ) );
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
        assertThat( Iterables.matchesAll( Specifications.in( "1", "2", "3" ), numbers ), equalTo( true ) );
        assertThat( Iterables.matchesAll( Specifications.in( "2", "3", "4" ), numbers ), equalTo( false ) );
    }

    @Test
    public void testFlatten()
    {
        assertThat( Iterables.toList( Iterables.flatten( numbers, numbers ) ).toString(),
                    equalTo( "[1, 2, 3, 1, 2, 3]" ) );

        Iterable<? extends Number> flatten = Iterables.flatten( numberIntegers, numberLongs );
        assertThat( Iterables.toList( flatten ).toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testFlattenIterables()
    {
        Iterable<List<String>> iterable = Iterables.iterable( numbers, numbers );
        assertThat( Iterables.toList( Iterables.flattenIterables( iterable ) ).toString(),
                    equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testMix()
    {
        assertThat( Iterables.toList( Iterables.mix( Iterables.iterable( "A", "B", "C" ),
                                                     Iterables.iterable( "1", "2", "3", "4", "5" ),
                                                     Iterables.iterable( "X", "Y", "Z" ) ) ).toString(),
                    equalTo( "[A, 1, X, B, 2, Y, C, 3, Z, 4, 5]" ) );
    }

    @Test
    public void testMap()
    {
        assertThat( Iterables.toList( Iterables.map( new Function<String, String>()
        {

            public String map( String s )
            {
                return s + s;
            }

        }, numbers ) ).toString(), equalTo( "[11, 22, 33]" ) );

        Iterable<List<String>> numberIterable = Iterables.iterable( numbers, numbers, numbers );
        assertThat( Iterables.toList( Iterables.map( new Function<Collection, Integer>()
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

        Enumeration<String> enumeration = enumeration( numbers );
        assertThat( Iterables.toList( Iterables.iterable( enumeration ) ).toString(),
                    equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testIterableVarArg()
    {
        assertThat( Iterables.toList( Iterables.iterable( "1", "2", "3" ) ).toString(),
                    equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testCast()
    {
        Iterable<Long> values = numberLongs;
        Iterable<Number> numbers = Iterables.cast( values );
    }

    @Test
    public void testDebug()
    {
        assertThat( Iterables.first( Iterables.debug( "Filtered number:{0}",
                                                      Iterables.filter( Specifications.in( "2" ),
                                                                        Iterables.debug( "Number:{0}", numbers ) ) ) ),
                    equalTo( "2" ) );
    }

    @Test
    public void testDebugWithFunctions()
    {
        Function<String, String> fun = new Function<String, String>()
        {

            @Override
            public String map( String s )
            {
                return s + ":" + s.length();
            }

        };
        assertThat( Iterables.first( Iterables.debug( "Filtered number:{0}",
                                                      Iterables.filter( Specifications.in( "2" ),
                                                                        Iterables.debug( "Number:{0}", numbers, fun ) ) ) ),
                    equalTo( "2" ) );
    }

    @Test
    public void testCache()
    {
        final int[] count = new int[ 1 ];

        Iterable<String> b = Iterables.cache( Iterables.filter( Specifications.and( new Specification<String>()
        {

            @Override
            public boolean satisfiedBy( String item )
            {
                count[ 0] = count[ 0] + 1;
                return true;
            }

        }, Specifications.in( "B" ) ), Iterables.iterable( "A", "B", "C" ) ) );

        assertThat( count[ 0], equalTo( 0 ) );

        Iterables.toList( b );

        assertThat( count[ 0], equalTo( 3 ) );

        Iterables.toList( b );

        assertThat( count[ 0], equalTo( 3 ) );
    }

    @Test
    public void testSort()
    {
        assertThat( Iterables.sort( Iterables.reverse( numberLongs ) ).toString(), equalTo( "[1, 2, 3]" ) );

        Comparator<Long> inverseLongComparator = new Comparator<Long>()
        {

            @Override
            public int compare( Long left, Long right )
            {
                return left.compareTo( right ) * -1;
            }

        };
        assertThat( Iterables.sort( inverseLongComparator, numberLongs ).toString(), equalTo( "[3, 2, 1]" ) );
    }

}
