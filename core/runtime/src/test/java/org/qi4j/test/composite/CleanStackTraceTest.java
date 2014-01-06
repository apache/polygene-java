/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * Test if the stacktrace is cleaned up properly.
 * <p/>
 * NOTE: This satisfiedBy MUST NOT be inside package org.qi4j.runtime, or it will fail.
 */
public class CleanStackTraceTest
    extends AbstractQi4jTest
{

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {   
        assumeTrue( !( System.getProperty( "java.vendor" ).contains( "IBM" ) ) );
    }   

    @Override
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
            String separator = System.getProperty( "line.separator" );
            String correctTrace1 = "java.lang.RuntimeException: level 2" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest$DoStuffMixin.doStuff(CleanStackTraceTest.java:121)" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest$NillyWilly.invoke(CleanStackTraceTest.java:134)" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest.cleanStackTraceOnApplicationException(CleanStackTraceTest.java:72)";
            assertEquality( e, correctTrace1 );
            String correctTrace2 = "java.lang.RuntimeException: level 1" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest$DoStuffMixin.doStuff(CleanStackTraceTest.java:117)" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest$NillyWilly.invoke(CleanStackTraceTest.java:134)" + separator +
                                   "\tat org.qi4j.test.composite.CleanStackTraceTest.cleanStackTraceOnApplicationException(CleanStackTraceTest.java:72)";
            assertThat( e.getCause(), notNullValue() );
            assertEquality( e.getCause(), correctTrace2 );
        }
    }

    private void assertEquality( Throwable e, String correctTrace )
    {
        StringWriter actualTrace = new StringWriter();
        e.printStackTrace( new PrintWriter( actualTrace ) );

        String actual = actualTrace.toString();
        actual = actual.substring( 0, correctTrace.length() );
        assertEquals( correctTrace, actual );
    }

    @Concerns( NillyWilly.class )
    @Mixins(DoStuffMixin.class)
    public interface TestComposite
    {
        void doStuff();
    }

    public static class DoStuffMixin
        implements TestComposite
    {

        @Override
        public void doStuff()
        {
            try
            {
                throw new RuntimeException( "level 1" );
            }
            catch( RuntimeException e )
            {
                throw new RuntimeException( "level 2", e );
            }
        }
    }

    static class NillyWilly extends GenericConcern
        implements InvocationHandler
    {

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return next.invoke( proxy, method, args );
        }
    }
}
