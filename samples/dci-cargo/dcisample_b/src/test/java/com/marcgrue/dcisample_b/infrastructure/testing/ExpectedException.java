package com.marcgrue.dcisample_b.infrastructure.testing;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * ExpectedException
 *
 * Wrapper of {@link org.junit.rules.ExpectedException} in order to provide custom
 * expected exception one-liners for convenience.
 *
 * If you're checking certain DomainSpecificException often, you could wrap it here too...
 *
 * {@see http://alexruiz.developerblogs.com/?p=1530}
 *
 * NOTE!
 * 1. the check for the expected exception must be immediately above the code that is expected to throw such exception
 * 2. the line of code that is expected to throw an exception should be the last line in the test method
 * {@see http://java.dzone.com/articles/unexpected-behavior-junit’s}
 */
public class ExpectedException implements MethodRule
{
    private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

    public static ExpectedException none()
    {
        return new ExpectedException();
    }

    private ExpectedException() {}

    public Statement apply( Statement base, FrameworkMethod method, Object target )
    {
        return delegate.apply( base, method, target );
    }

    // This one saves a little typing :-)
    public void expect( Class<? extends Throwable> type, String substringOfMessage )
    {
        expect( type );
        expectMessage( substringOfMessage );
    }

    public void expectAssertionError( String message )
    {
        expect( AssertionError.class );
        expectMessage( message );
    }

    public void expectNullPointerException( String message )
    {
        expect( NullPointerException.class );
        expectMessage( message );
    }

    public void expect( Throwable error )
    {
        expect( error.getClass() );
        expectMessage( error.getMessage() );
    }


    public void expect( Class<? extends Throwable> type )
    {
        delegate.expect( type );
    }

    public void expectMessage( String message )
    {
        delegate.expectMessage( message );
    }
}