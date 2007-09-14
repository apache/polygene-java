package org.qi4j.runtime;

import org.qi4j.api.InvalidDependencyException;

/**
 * TODO
 */
public class CompositeResolutionException
    extends RuntimeException
{
    public CompositeResolutionException( String s )
    {
        super( s );
    }

    public CompositeResolutionException( String s, InvalidDependencyException ex )
    {
        super( s, ex );
    }
}
