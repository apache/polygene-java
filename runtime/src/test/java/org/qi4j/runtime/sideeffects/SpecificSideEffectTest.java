/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.sideeffects;

import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test for adding side-effects to methods specified by domain interfaces
 */
public class SpecificSideEffectTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( SomeComposite.class );
    }

    @Test
    public void specificSideEffect()
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