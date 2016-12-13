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

package org.apache.zest.runtime.injection;

import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.SideEffectOf;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the @This annotation
 */
public class ThisInjectionTest
    extends AbstractPolygeneTest
{
    public static boolean sideEffectInjected;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    /**
     * Tests the injected object for {@link @org.apache.zest.composite.scope.This} annotation.
     *
     * @throws Exception re-thrown
     */
    @org.junit.Test
    public void givenCompositeWithThisInjectionsWhenInstantiatedThenCompositeIsInjected()
        throws Exception
    {
        TestComposite testComposite = transientBuilderFactory.newTransient( TestComposite.class );

        assertThat( "Injection worked", testComposite.isInjected() && sideEffectInjected, is( equalTo( true ) ) );
    }

    public interface Test
    {
        boolean isInjected();

        String test();
    }

    public interface TestPrivate
    {
        @UseDefaults
        Property<Boolean> testPrivate();
    }

    @SideEffects( TestSideEffect.class )
    @Concerns( TestConcern.class )
    @Mixins( TestMixin.class )
    public interface TestComposite
        extends TransientComposite, Test
    {
    }

    public static class TestMixin
        implements Test
    {
        @This
        Test test;

        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            return test != null && !testPrivate.testPrivate().get();
        }

        public String test()
        {
            return "Foo";
        }
    }

    public static abstract class TestConcern
        extends ConcernOf<Test>
        implements Test
    {
        @This
        Test test;

        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            return test != null && test.test().equals( "Foo" ) &&
                   !testPrivate.testPrivate().get() &&
                   next.isInjected();
        }
    }

    public static abstract class TestSideEffect
        extends SideEffectOf<Test>
        implements Test
    {
        @This
        Test test;
        @This
        TestPrivate testPrivate;

        public boolean isInjected()
        {
            sideEffectInjected = test != null && !testPrivate.testPrivate().get();

            return false;
        }
    }
}