package org.qi4j.bootstrap;

/**
 * Thrown by the Qi4j runtime if a dependency injection declaration is invalid.
 */
public class InvalidInjectionException
    extends Exception
{
    public InvalidInjectionException( String s )
    {
        super( s );
    }

    public InvalidInjectionException( String s, Throwable throwable )
    {
        super( s, throwable );
    }
}
