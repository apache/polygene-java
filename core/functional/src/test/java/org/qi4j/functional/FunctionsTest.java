package org.qi4j.functional;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.functional.ForEach.forEach;
import static org.qi4j.functional.Functions.*;
import static org.qi4j.functional.Functions.count;
import static org.qi4j.functional.Iterables.*;
import static org.qi4j.functional.Specifications.in;

/**
 * Test of utility functions
 */
public class FunctionsTest
{
    Function<Object, String> stringifier = new Function<Object, String>()
    {
        @Override
        public String map( Object s )
        {
            return s.toString();
        }
    };

    Function<String, Integer> length = new Function<String, Integer>()
    {
        @Override
        public Integer map( String s )
        {
            return s.length();
        }
    };

    @Test
    public void testCompose()
    {
        assertThat( Functions.<Object, String, Integer>compose().map( length, stringifier ).map( 12345L ), equalTo( 5 ) );
        assertThat( compose( length, stringifier ).map( 12345L ), equalTo( 5 ) );
    }

    @Test
    public void testFromMap()
    {
        Map<String,String> map = new HashMap<String, String>(  );
        map.put( "A","1" );
        map.put( "B","2" );
        map.put( "C", "3" );
        assertThat( Iterables.toList( Iterables.filter( Specifications.notNull(), Iterables.map( Functions.fromMap( map ), Iterables
            .iterable( "A", "B", "D" ) ) ) ).toString(), equalTo( "[1, 2]" ));
    }

    @Test
    public void testWithDefault()
    {
        assertThat( Iterables.toList( Iterables.map( Functions.withDefault( "DEFAULT" ), Iterables.iterable( "123", null, "456" ) ) ).toString(), equalTo( "[123, DEFAULT, 456]" ) );
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
        } );

        List<Integer> integers = Iterables.toList( Iterables.iterable( 1, 5, 3, 6, 8 ) );
        Collections.sort( integers, comparator );
        assertThat( integers.toString(), equalTo( "[1, 3, 5, 6, 8]" ));
    }
}
