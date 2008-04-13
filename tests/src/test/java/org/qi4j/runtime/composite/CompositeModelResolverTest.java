package org.qi4j.runtime.composite;
/**
 *  TODO
 */

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.This;
import org.qi4j.spi.composite.InvalidCompositeException;

public class CompositeModelResolverTest
{
    @Test
    public void testWhenCyclicDependencyThenThrowException()
    {
        try
        {
            new SingletonAssembler()
            {
                public void assemble( ModuleAssembly module ) throws AssemblyException
                {
                    module.addComposites( TestComposite2.class );
                }
            };
            fail( "Should have thrown exception due to cyclic dependency" );
        }
        catch( InvalidCompositeException e )
        {
            // Ok
        }
    }

    @Test
    public void testWhenDependentMixinsThenOrderMixins()
        throws Exception
    {
        CompositeBuilderFactory cbf = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites( TestComposite1.class );
            }
        }.getCompositeBuilderFactory();


        assertEquals( "ok", cbf.newCompositeBuilder( TestComposite1.class ).newInstance().testB() );
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
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
        extends Composite, TestA, TestB
    {
    }

    private static interface TestComposite2
        extends Composite, TestC, TestD
    {
    }
}