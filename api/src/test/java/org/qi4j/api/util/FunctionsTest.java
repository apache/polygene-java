package org.qi4j.api.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.specification.Specifications.in;
import static org.qi4j.api.util.Functions.*;
import static org.qi4j.api.util.Iterables.*;

/**
 * Test of utility functions
 */
public class FunctionsTest
{
    @Test
    public void testLongSum()
    {
        assertThat( last( map( longSum(), iterable( 1, 2L, 3F, 4D ) ) ), equalTo( 10L ) );
    }

    @Test
    public void testIntSum()
    {
        assertThat( last( map( intSum(), iterable( 1, 2L, 3F, 4D ) ) ), equalTo( 10 ) );
    }

    @Test
    public void testCount()
    {
        assertThat( last( map( count( in( "X" ) ), iterable( "X","Y","X","X","Y" ) ) ), equalTo( 3 ) );
    }

    @Test
    public void testIndexOf()
    {
        assertThat( last( map( indexOf( in( "D" ) ), iterable( "A","B","C","D","D" ) ) ), equalTo( 3 ) );
    }

    @Test
    public void testIndexOf2()
    {
        assertThat( indexOf( "D", iterable( "A","B","C","D","D" )), equalTo( 3 ) );
    }

    @Test
    public void testComparator()
    {
        Comparator<Integer> comparator = Functions.comparator( new Function<Integer, Comparable>()
        {
            @Override
            public Comparable map( Integer integer )
            {
                return integer.toString();
            }
        });

        ArrayList<Integer> integers = Iterables.addAll( new ArrayList<Integer>(), Iterables.iterable( 1, 5, 3, 6, 8 ) );
        Collections.sort( integers, comparator );
        assertThat( integers.toString(), equalTo( "[1, 3, 5, 6, 8]" ));
    }
}
