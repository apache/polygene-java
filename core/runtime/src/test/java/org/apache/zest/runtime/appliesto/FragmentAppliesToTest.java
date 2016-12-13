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
package org.apache.zest.runtime.appliesto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.sideeffect.GenericSideEffect;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FragmentAppliesToTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Composite1.class );
    }

    @Test
    public void testMixin()
        throws Exception
    {
        TransientBuilder<Composite1> builder = transientBuilderFactory.newTransientBuilder( Composite1.class );

        Composite1 instance = builder.newInstance();
        assertNotNull( "DependencyOld not injected.", instance.getBuilderFactory() );
        assertNotNull( "This not injected.", instance.getMeAsMixin2() );
        assertEquals( 1, instance.getValue() );
        instance.getBuilderFactory();
        instance.getMeAsMixin2();
        instance.getBuilderFactory();
        instance.getMeAsMixin2();
        instance.getMeAsMixin2();
        instance.getBuilderFactory();
        assertEquals( 4, instance.getValue() );
    }

    @Mixins( { Mixin1.Mixin1Impl.class, Mixin2.Mixin2Impl.class, CounterImpl.class } )
    public interface Composite1
        extends TransientComposite, Mixin1, Mixin2, Counter
    {
    }

    public interface Mixin1
    {
        TransientBuilderFactory getBuilderFactory();

        Mixin2 getMeAsMixin2();

        @SideEffects( CountCallsSideEffect.class )
        public static class Mixin1Impl
            implements Mixin1
        {

            @Structure
            private TransientBuilderFactory builderFactory;

            @This
            private Mixin2 meAsMixin2;

            @CountCalls
            public TransientBuilderFactory getBuilderFactory()
            {
                return builderFactory;
            }

            public Mixin2 getMeAsMixin2()
            {
                return meAsMixin2;
            }
        }
    }

    public static class CounterImpl
        implements Counter
    {
        private int value;

        public void increment()
        {
            value++;
        }

        public void clear()
        {
            value = 0;
        }

        public int getValue()
        {
            return value;
        }
    }

    public interface Mixin2
    {
        public static class Mixin2Impl
            implements Mixin2
        {
        }
    }

    public interface Counter
    {
        void increment();

        void clear();

        int getValue();
    }

    @AppliesTo( CountCalls.class )
    public static class CountCallsSideEffect
        extends GenericSideEffect
    {

        @This
        private Counter counter;

        protected void invoke( Method method, Object[] args )
        {
            counter.increment();
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    @Documented
    @Inherited
    public @interface CountCalls
    {
    }
}
