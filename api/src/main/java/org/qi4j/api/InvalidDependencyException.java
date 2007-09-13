package org.qi4j.api;

/**
 * TODO
 */
public class InvalidDependencyException extends Exception
{
    public InvalidDependencyException( String s )
    {
        super( s );
    }

    public InvalidDependencyException( String s, Throwable throwable )
    {
        super( s, throwable );
    }
}
