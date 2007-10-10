package org.qi4j.runtime.resolution;
/**
 *  TODO
 */

import junit.framework.TestCase;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;

public class CompositeModelResolverTest extends TestCase
{
    CompositeModelResolver compositeModelResolver;

    public void testWhenCyclicDependencyThenThrowException()
    {
        try
        {
            CompositeBuilderFactory cbf = new CompositeBuilderFactoryImpl();
            cbf.newCompositeBuilder( TestComposite2.class ).newInstance().testC();
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
        CompositeBuilderFactory cbf = new CompositeBuilderFactoryImpl();
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

            public TestBMixin( @ThisAs TestA testA )
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

            public TestCMixin( @ThisAs TestD testD )
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

            public TestDMixin( @ThisAs TestC testC )
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