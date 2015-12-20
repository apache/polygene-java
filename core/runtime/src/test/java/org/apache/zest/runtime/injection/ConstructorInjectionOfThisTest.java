/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.runtime.injection;

import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.NoopMixin;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.sideeffect.SideEffectOf;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test is created in response to QI-359
 */
public class ConstructorInjectionOfThisTest
{

    @Test
    public void givenMixinWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( Does.class ).withMixins( DoesMixin.class );
            }
        };
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    @Test @Ignore
    public void givenConcernWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( Does.class ).withMixins( NoopMixin.class ).withConcerns( DoesConcern.class );
            }
        };
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    @Test @Ignore
    public void givenSideEffectWithThisInConstructorWhenCreatingModelExpectNoException()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( Does.class ).withMixins( NoopMixin.class ).withSideEffects( DoesSideEffect.class );
            }
        };
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    public static class DoesMixin
        implements Does
    {
        private DoesPrivateFragment doesPrivateFragment;

        public DoesMixin( @This DoesPrivateFragment doesPrivateFragment )
        {
            if( doesPrivateFragment == null )
            {
                throw new NullPointerException();
            }
            this.doesPrivateFragment = doesPrivateFragment;
        }

        @Override
        public void doSomething()
        {
            assertFalse( doesPrivateFragment.someState().get() );
        }
    }

    public static class DoesConcern
        extends ConcernOf<Does>
        implements Does
    {

        public DoesConcern( @This Does work )
        {
            if( work == null )
            {
                throw new NullPointerException();
            }
            System.out.print( "Niclas " );
        }

        @Override
        public void doSomething()
        {
            next.doSomething();
        }
    }

    public static class DoesSideEffect
        extends SideEffectOf<Does>
        implements Does
    {

        public DoesSideEffect( @This Does work )
        {
            if( work == null )
            {
                throw new NullPointerException();
            }
            System.out.print( "Niclas " );
        }

        @Override
        public void doSomething()
        {
            this.result.doSomething();
        }
    }

    public interface DoesPrivateFragment
    {
        @UseDefaults
        Property<Boolean> someState();
    }

    public interface Does
    {
        void doSomething();
    }
}
