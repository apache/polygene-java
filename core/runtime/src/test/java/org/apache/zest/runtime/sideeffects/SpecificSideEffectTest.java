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

import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.SideEffectOf;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Test for adding side-effects to methods specified by domain interfaces
 */
public class SpecificSideEffectTest
    extends AbstractZestTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SomeComposite.class );
    }

    @Test
    public void specificSideEffect()
    {
        SomeComposite some = transientBuilderFactory.newTransient( SomeComposite.class );
        Property<Integer> count = some.count();
        assertThat( "count is zero", count.get(), equalTo( 0 ) );
        some.doStuff();
        assertThat( "count is not zero", count.get(), equalTo( 1 ) );
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

    public static class CounterSideEffect
        extends SideEffectOf<Some>
        implements Some
    {
        @This
        Counter counter;

        public String doStuff()
        {
            counter.count().set( counter.count().get() + 1 );
            return null;
        }
    }
}