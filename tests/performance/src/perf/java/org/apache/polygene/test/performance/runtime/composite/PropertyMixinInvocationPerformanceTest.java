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
 */

package org.apache.polygene.test.performance.runtime.composite;

import java.text.NumberFormat;
import java.util.Locale;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

/**
 * PropertyMixin invocation performance test.
 * <p>
 * Don't forget to add VM value "-server" before running this test!
 * </p>
 */
public class PropertyMixinInvocationPerformanceTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.transients( SimpleComposite.class );
        module.transients( SimpleComposite2.class );
    }

    @Test
    public void testNewInstance()
    {
        {
            TransientBuilder<SimpleComposite> builder = transientBuilderFactory.newTransientBuilder( SimpleComposite.class );
            SimpleComposite simple = builder.newInstance();

            int rounds = 1;
            for( int i = 0; i < rounds; i++ )
            {
                performanceCheck( simple );
            }
        }

        {
            TransientBuilder<SimpleComposite> builder = transientBuilderFactory.newTransientBuilder( SimpleComposite.class );
            SimpleComposite simple = builder.newInstance();

            int rounds = 1;
            for( int i = 0; i < rounds; i++ )
            {
                performanceCheck( simple );
            }
        }
    }

    private void performanceCheck( SimpleComposite simple )
    {
        long count = 10000000L;

        {
            long start = System.currentTimeMillis();
            for( long i = 0; i < count; i++ )
            {
                simple.test();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            long callsPerSecond = ( count / time ) * 1000;
            System.out.println( "Accesses per second: "
                                + NumberFormat.getIntegerInstance( Locale.US ).format( callsPerSecond ) );
        }

        {
            long start = System.currentTimeMillis();
            for( long i = 0; i < count; i++ )
            {
                simple.test().get();
            }
            long end = System.currentTimeMillis();
            long time = end - start;
            long callsPerSecond = ( count / time ) * 1000;
            System.out.println( "Gets per second: "
                                + NumberFormat.getIntegerInstance( Locale.US ).format( callsPerSecond ) );
        }
    }

    public interface SimpleComposite
        extends TransientComposite
    {
        @Optional
        Property<String> test();
    }

    @Mixins( SimpleMixin.class )
    public interface SimpleComposite2
        extends SimpleComposite
    {
    }

    public abstract static class SimpleMixin
        implements SimpleComposite2
    {
        @State
        Property<String> test;

        @Override
        public Property<String> test()
        {
            return test;
        }
    }

}
