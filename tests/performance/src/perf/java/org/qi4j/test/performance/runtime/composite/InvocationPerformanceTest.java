/*
 * Copyright (c) 2007-2011, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.test.performance.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Invocation performance test.
 * <p/>
 * Don't forget to add VM value "-server" before running this test!
 * <p/>
 * These tests are very sensitive to warmup of JVM, hence the duplication. Often the first round
 * is only for getting the code jitted, and the second round is what you want to look at.
 */
public class InvocationPerformanceTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SimpleComposite.class );
        module.transients( SimpleWithTypedConcernComposite.class );
        module.transients( SimpleWithGenericConcernComposite.class );
    }

    @Test
    public void testInvokeMixin()
    {
        // Create instance
        TransientBuilder<SimpleComposite> builder = module.newTransientBuilder( SimpleComposite.class );
        Simple simple = builder.newInstance();

        for( int i = 0; i < 60000; i++ )
        {
            simple.test();
        }

        int rounds = 10;
        for( int i = 0; i < rounds; i++ )
        {
            System.gc();
            performanceCheck( simple );
        }
    }

    @Test
    public void testInvokeMixinWithTypedConcern()
    {
        // Create instance
        Simple simple = module.newTransient( SimpleWithTypedConcernComposite.class );

        for( int i = 0; i < 60000; i++ )
        {
            simple.test();
        }

        int rounds = 3;
        for( int i = 0; i < rounds; i++ )
        {
            performanceCheck( simple );
        }
    }

    @Test
    public void testInvokeMixinWithGenericConcern()
    {
        // Create instance
        Simple simple = module.newTransient( SimpleWithGenericConcernComposite.class );

        for( int i = 0; i < 60000; i++ )
        {
            simple.test();
        }

        int rounds = 3;
        for( int i = 0; i < rounds; i++ )
        {
            performanceCheck( simple );
        }
    }

    @Test
    public void testInvokeMixin2()
    {
        testInvokeMixin();
    }

    @Test
    public void testInvokeMixinWithTypedConcern2()
    {
        testInvokeMixinWithTypedConcern();
    }

    @Test
    public void testInvokeMixinWithGenericConcern2()
    {
        testInvokeMixinWithGenericConcern();
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
    @Concerns( SimpleTypedConcern.class )
    public interface SimpleWithTypedConcernComposite
        extends Simple, TransientComposite
    {
    }

    @Mixins( SimpleMixin.class )
    @Concerns( SimpleGenericConcern.class )
    public interface SimpleWithGenericConcernComposite
        extends Simple, TransientComposite
    {
    }

    @Mixins( SimpleMixin.class )
    public interface SimpleComposite
        extends Simple, TransientComposite
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

        @Override
        public void test()
        {
            count++; // Do nothing
        }
    }

    public static class SimpleTypedConcern
        extends ConcernOf<Simple>
        implements Simple
    {
        @Override
        public void test()
        {
            next.test();
        }
    }

    public static class SimpleGenericConcern
        extends ConcernOf<InvocationHandler>
        implements InvocationHandler
    {
        @Override
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            return next.invoke( o, method, objects );
        }
    }

}
