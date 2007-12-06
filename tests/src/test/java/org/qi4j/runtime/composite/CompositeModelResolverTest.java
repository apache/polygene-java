package org.qi4j.runtime.composite;
/**
 *  TODO
 */

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.ThisCompositeAs;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.test.AbstractQi4jTest;

public class CompositeModelResolverTest extends AbstractQi4jTest
{
    public void testWhenCyclicDependencyThenThrowException()
    {
        try
        {
            new SingletonAssembly()
            {
                public void configure( ModuleAssembly module ) throws AssemblyException
                {
                    module.addComposite( TestComposite2.class, false );
                }
            };
            fail( "Should have thrown exception due to cyclic dependency" );
        }
        catch( InvalidCompositeException e )
        {
            // Ok
        }
    }

    public void testWhenDependentMixinsThenOrderMixins()
        throws Exception
    {
        CompositeBuilderFactory cbf = new SingletonAssembly()
        {
            public void configure( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposite( TestComposite1.class, false );
            }
        }.getCompositeBuilderFactory();


        assertEquals( "ok", cbf.newCompositeBuilder( TestComposite1.class ).newInstance().testB() );
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

            public TestBMixin( @ThisCompositeAs TestA testA )
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

            public TestCMixin( @ThisCompositeAs TestD testD )
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

            public TestDMixin( @ThisCompositeAs TestC testC )
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
        extends Composite, TestA, TestB
    {
    }

    private static interface TestComposite2
        extends Composite, TestC, TestD
    {
    }
}