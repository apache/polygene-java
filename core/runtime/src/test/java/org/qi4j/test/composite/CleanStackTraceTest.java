/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.test.composite;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Test if the stacktrace is cleaned up properly.
 * <p/>
 * NOTE: This satisfiedBy MUST NOT be inside package org.qi4j.runtime, or it will fail.
 */
public class CleanStackTraceTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CleanStackTraceTest.TestComposite.class );
    }

    /**
     * Tests that stack trace is cleaned up on an application exception.
     */
    @Test
    public void cleanStackTraceOnApplicationException()
    {
        // Don't run the satisfiedBy if compacttrace is set to anything else but proxy
        String compactTracePropertyValue = System.getProperty( "qi4j.compacttrace" );
        if( compactTracePropertyValue != null && !"proxy".equals( compactTracePropertyValue ) )
        {
            return;
        }

        TestComposite composite = module.newTransient( TestComposite.class );

        try
        {
            composite.doStuff();
        }
        catch( RuntimeException e )
        {
            StringWriter actualTrace = new StringWriter();
            e.printStackTrace( new PrintWriter( actualTrace ) );

            String separator = System.getProperty( "line.separator" );
            String correctTrace = "java.lang.RuntimeException" + separator +
                                  "\tat org.qi4j.test.composite.CleanStackTraceTest$DoStuffMixin.doStuff(CleanStackTraceTest.java:92)" + separator +
                                  "\tat org.qi4j.test.composite.CleanStackTraceTest$TestComposite.doStuff(Unknown Source)" + separator +
                                  "\tat org.qi4j.test.composite.CleanStackTraceTest.cleanStackTraceOnApplicationException(CleanStackTraceTest.java:61)";
            String actual = actualTrace.toString();
            actual = actual.substring( 0, correctTrace.length() );
            assertEquals( correctTrace, actual );
        }
    }

    @Mixins( DoStuffMixin.class )
    public interface TestComposite
        extends TransientComposite
    {
        void doStuff();
    }

    public abstract static class DoStuffMixin
        implements TestComposite
    {

        public void doStuff()
        {
            throw new RuntimeException();
        }
    }
}
