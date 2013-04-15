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

package org.qi4j.runtime.injection;

import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

/**
 * This test is created in response to QI-359
 */
public class ConstructorInjectionOfThisTest
{

    @Test
    public void givenConcernWithThisInConstructorWhenCreatingModelExpectException()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( Does.class ).withConcerns( DoesConcern.class );
            }
        };
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    @Test
    public void givenSideEffectWithThisInConstructorWhenCreatingModelExpectException()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler singletonAssembler = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( Does.class ).withSideEffects( DoesSideEffect.class );
            }
        };
        Module module = singletonAssembler.application().findModule( "Layer 1", "Module 1" );
        Does does = module.newValue( Does.class );
        does.doSomething();
    }

    public static class DoesConcern extends ConcernOf<Does>
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

    public static class DoesSideEffect extends SideEffectOf<Does>
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

    @Mixins( NoopMixin.class )
    public interface Does
    {
        void doSomething();
    }
}
