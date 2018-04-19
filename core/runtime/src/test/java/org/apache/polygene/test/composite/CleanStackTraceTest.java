/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.test.composite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.concern.GenericConcern;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test if the stacktrace is cleaned up properly.
 * <p>
 * NOTE: This satisfiedBy MUST NOT be inside package org.apache.polygene.runtime, or it will fail.
 * </p>
 */
public class CleanStackTraceTest extends AbstractPolygeneTest
{
    @BeforeAll
    public static void beforeClass_IBMJDK()
    {
        assumeTrue( !( System.getProperty( "java.vendor" ).contains( "IBM" ) ) );
    }

    @Override
    public void assemble( ModuleAssembly module )
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
        String compactTracePropertyValue = System.getProperty( "polygene.compacttrace" );
        if( compactTracePropertyValue != null && !"proxy".equals( compactTracePropertyValue ) )
        {
            return;
        }
        TestComposite composite = transientBuilderFactory.newTransient( TestComposite.class );
        try
        {
            composite.doStuff();
        }
        catch( RuntimeException e )
        {
            String separator = System.getProperty( "line.separator" );
            String correctTrace1 = "java.lang.RuntimeException: level 2" + separator +
                                   "\tat method \"doStuff\" of TestComposite in module [Module 1] of layer [Layer 1].(:0)\n" +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest$DoStuffMixin.doStuff(CleanStackTraceTest.java:124)" + separator +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest$NillyWilly.invoke(CleanStackTraceTest.java:136)" + separator +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest.cleanStackTraceOnApplicationException(CleanStackTraceTest.java:75)";
            assertEquality( e, correctTrace1 );
            String correctTrace2 = "java.lang.RuntimeException: level 1" + separator +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest$DoStuffMixin.doStuff(CleanStackTraceTest.java:120)" + separator +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest$NillyWilly.invoke(CleanStackTraceTest.java:136)" + separator +
                                   "\tat org.apache.polygene.test.composite.CleanStackTraceTest.cleanStackTraceOnApplicationException(CleanStackTraceTest.java:75)";
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
        assertThat( actual, equalTo( correctTrace ) );
    }

    @Concerns( NillyWilly.class )
    @Mixins( DoStuffMixin.class )
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
