package org.qi4j.dependency;

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
