/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.runtime.mixin;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test of declaring Mixin in @This declared interface
 */
public class MixinsOnThisInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TestCase.class );
    }

    @Test
    public void givenCompositeWithThisInjectionAndNoMixinDeclarationWhenBindingCompositeThenUseInterfaceDeclaredMixin()
    {
        TestCase TestCase = transientBuilderFactory.newTransient( TestCase.class );
        assertThat( "Composite can be instantiated", TestCase.sayHello(), equalTo( "Hello" ) );
    }

    @Mixins( TestMixin.class )
    public interface TestCase
        extends TransientComposite
    {
        String sayHello();
    }

    public abstract static class TestMixin
        implements TestCase
    {
        @This
        TestCase2 testCase2;

        public String sayHello()
        {
            return testCase2.sayHello();
        }
    }

    @Mixins( TestMixin2.class )
    public interface TestCase2
    {
        String sayHello();
    }

    public abstract static class TestMixin2
        implements TestCase2
    {
        public String sayHello()
        {
            return "Hello";
        }
    }
}