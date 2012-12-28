/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.runtime.composite;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class CompositeModelResolverTest
{
    @Test
    public void testWhenDependentMixinsThenOrderMixins()
        throws Exception
    {
        Module module = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( TestComposite1.class );
            }
        }.module();

        Assert.assertEquals( "ok", module.newTransient( TestComposite1.class ).testB() );
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
    }

    @Mixins( TestA.TestAMixin.class )
    public static interface TestA
    {
        public String test();

        class TestAMixin
            implements TestA
        {
            public String test()
            {
                return "ok";
            }
        }
    }

    @Mixins( TestB.TestBMixin.class )
    public static interface TestB
    {
        public String testB();

        class TestBMixin
            implements TestB
        {
            private TestA testA;

            public TestBMixin( @This TestA testA )
            {
                this.testA = testA;
                testA.test();
            }

            public String testB()
            {
                return testA.test();
            }
        }
    }

    @Mixins( TestC.TestCMixin.class )
    public static interface TestC
    {
        public String testC();

        class TestCMixin
            implements TestC
        {
            private TestD testD;

            public TestCMixin( @This TestD testD )
            {
                this.testD = testD;
                testD.testD();
            }

            public String testC()
            {
                return testD.testD();
            }
        }
    }

    @Mixins( TestD.TestDMixin.class )
    public static interface TestD
    {
        public String testD();

        class TestDMixin
            implements TestD
        {
            private TestC testC;

            public TestDMixin( @This TestC testC )
            {
                this.testC = testC;
                testC.testC();
            }

            public String testD()
            {
                return testC.testC();
            }
        }
    }

    private static interface TestComposite1
        extends TransientComposite, TestA, TestB
    {
    }

    private static interface TestComposite2
        extends Composite, TestC, TestD
    {
    }
}