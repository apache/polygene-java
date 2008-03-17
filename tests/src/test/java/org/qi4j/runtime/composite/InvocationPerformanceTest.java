/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.composite;

import java.text.NumberFormat;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.test.Qi4jTestSetup;

/**
 * Invocation performance test. Don't forget to add VM value "-server"
 * before running this test!
 */
public class InvocationPerformanceTest
    extends Qi4jTestSetup
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( SimpleComposite.class );
    }

    @Test
    public void testNewInstance()
    {
        // Create instance
        CompositeBuilder<SimpleComposite> builder = compositeBuilderFactory.newCompositeBuilder( SimpleComposite.class );
        Simple simple = builder.newInstance();

        for( int i = 0; i < 60000; i++ )
        {
            simple.test();
        }

        int rounds = 2;
        for( int i = 0; i < rounds; i++ )
        {
            performanceCheck( simple );
        }
    }

    private void performanceCheck( Simple simple )
    {
        long count = 10000000L;

        long start = System.currentTimeMillis();
        for( long i = 0; i < count; i++ )
        {
            simple.test();
        }
        long end = System.currentTimeMillis();
        long time = end - start;
        long callsPerSecond = ( count / time ) * 1000;
        System.out.println( "Calls per second: " + NumberFormat.getIntegerInstance().format( callsPerSecond ) );
    }

    @Mixins( SimpleMixin.class )
    @Concerns( SimpleConcern.class )
    public interface SimpleComposite
        extends Simple, Composite
    {
    }

    public interface Simple
    {
        public void test();
    }

    public static class SimpleMixin
        implements Simple
    {
        long count = 0;

        // Public --------------------------------------------------------
        public void test()
        {
            count++; // Do nothing
        }
    }

    public static class SimpleConcern
        implements Simple
    {
        @ConcernFor Simple next;

        public void test()
        {
            next.test();
        }
    }
}
