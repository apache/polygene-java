package org.qi4j.spi.composite;

import org.qi4j.spi.dependency.InvalidInjectionException;

/**
 * TODO
 */
public class ResolutionException
    extends RuntimeException
{
    public ResolutionException( String s )
    {
        super( s );
    }

    public ResolutionException( String s, InvalidInjectionException ex )
    {
        super( s, ex );
    }
}
