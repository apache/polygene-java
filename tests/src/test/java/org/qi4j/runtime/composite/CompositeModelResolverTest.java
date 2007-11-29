package org.qi4j.runtime.composite;
/**
 *  TODO
 */

import org.qi4j.Composite;
import org.qi4j.annotation.Mixins;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.test.AbstractQi4jTest;

public class CompositeModelResolverTest extends AbstractQi4jTest
{
    CompositeResolver compositeResolver;

    public void testWhenCyclicDependencyThenThrowException()
    {
        try
        {
            compositeBuilderFactory.newCompositeBuilder( TestComposite2.class ).newInstance().testC();
            fail( "Should have thrown exception" );
        }
        catch( InvalidCompositeException e )
        {
            // Ok!
        }
    }

    public void testWhenDependentMixinsThenOrderMixins()
        throws Exception
    {
        assertEquals( "ok", compositeBuilderFactory.newCompositeBuilder( TestComposite1.class ).newInstance().testB() );
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