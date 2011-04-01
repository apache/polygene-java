package org.qi4j.api.util;

import org.junit.Test;

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
}
