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

package org.apache.zest.runtime.sideeffects;

import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.AppliesToFilter;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.GenericSideEffect;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * JAVADOC
 */
public class GenericSideEffectTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SomeComposite.class );
    }

    @Test
    public void testGenericSideEffect()
    {
        SomeComposite some = transientBuilderFactory.newTransient( SomeComposite.class );
        Property<Integer> count = some.count();
        assertThat( "count is zero", count.get(), equalTo( 0 ) );
        some.doStuff();
        assertThat( "count is not zero", count.get(), not( equalTo( 0 ) ) );
    }

    @SideEffects( CounterSideEffect.class )
    @Mixins( SomeMixin.class )
    public interface SomeComposite
        extends Some, Counter, TransientComposite
    {
    }

    public interface Some
    {
        public String doStuff();
    }

    public static abstract class SomeMixin
        implements Some
    {
        public String doStuff()
        {
            return "Blah blah";
        }
    }

    public interface Counter
    {
        @UseDefaults
        Property<Integer> count();
    }

    @AppliesTo( NotCounterFilter.class )
    public static class CounterSideEffect
        extends GenericSideEffect
    {
        @This
        Counter counter;

        public void invoke( Method method, Object[] objects )
        {
            counter.count().set( counter.count().get() + 1 );
        }
    }

    public static class NotCounterFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modifierClass )
        {
            return !method.getDeclaringClass().equals( Counter.class );
        }
    }
}