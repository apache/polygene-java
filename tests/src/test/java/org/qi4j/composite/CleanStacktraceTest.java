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

package org.qi4j.composite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test if the stacktrace is cleaned up properly.
 */
public class CleanStacktraceTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( CleanStacktraceTest.TestComposite.class );
    }

    /**
     * Tests that stack trace is cleaned up on an application exception.
     */
    @Test
    public void cleanStackTraceOnAplicationException()
    {
        TestComposite composite = compositeBuilderFactory.newComposite( TestComposite.class );

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
                                  "\tat org.qi4j.composite.CleanStacktraceTest$DoStuffMixin.doStuff(CleanStacktraceTest.java:79)" + separator +
                                  "\tat org.qi4j.composite.CleanStacktraceTest$TestComposite.doStuff(Unknown Source)" + separator +
                                  "\tat org.qi4j.composite.CleanStacktraceTest.cleanStackTraceOnAplicationException(CleanStacktraceTest.java:49)";
            String actual = actualTrace.toString();
            assertTrue( "Trace should have been:\n" + correctTrace + "\nbut was:\n" + actualTrace, actual.startsWith( correctTrace ) );
        }
    }

    @Mixins( DoStuffMixin.class )
    public interface TestComposite
        extends Composite
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
