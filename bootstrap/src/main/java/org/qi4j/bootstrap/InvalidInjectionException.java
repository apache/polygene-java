package org.qi4j.bootstrap;

/**
 * JAVADOC
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
