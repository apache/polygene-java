package org.qi4j.samples.dddsample.domain.model.cargo;

import org.junit.Test;

import static org.junit.Assert.fail;

public class TrackingIdTest
{
    @Test( expected = IllegalArgumentException.class )
    public void testConstructor()
        throws Exception
    {
        new TrackingId( null );
        fail( "Should not accept null constructor arguments" );
    }
}