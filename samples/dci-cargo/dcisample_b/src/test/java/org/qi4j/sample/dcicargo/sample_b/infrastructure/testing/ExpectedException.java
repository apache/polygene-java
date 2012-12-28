/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.infrastructure.testing;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
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
 * {@see http://java.dzone.com/articles/unexpected-behavior-junits}
 */
public class ExpectedException implements TestRule
{
    private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

    public static ExpectedException none()
    {
        return new ExpectedException();
    }

    private ExpectedException() {}

    public Statement apply( Statement base, Description description )
    {
        return delegate.apply( base, description );
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