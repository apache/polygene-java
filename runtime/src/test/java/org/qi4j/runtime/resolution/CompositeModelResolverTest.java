package org.qi4j.runtime.resolution;
/**
 *  TODO
 */

import junit.framework.TestCase;
import org.junit.Test;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;

public class CompositeModelResolverTest extends TestCase
{
    CompositeModelResolver compositeModelResolver;

    @Test
    public void whenCyclicDependencyThenThrowException()
    {

    }

    @Test
    public void whenDependentMixinsThenOrderMixins()
    {
        CompositeBuilderFactory cbf = new CompositeBuilderFactoryImpl();
        cbf.newCompositeBuilder( TestComposite1.class );
    }

    @ImplementedBy( TestA.TestAMixin.class )
    private static interface TestA
    {
        String test();

        class TestAMixin
            implements TestA
        {
            public String test()
            {
                return "ok";
            }
        }
    }

    @ImplementedBy( TestB.TestBMixin.class )
    private static interface TestB
    {
        class TestBMixin
            implements TestB
        {
            public TestBMixin( @ThisAs TestA testA )
            {
                testA.test();
            }
        }
    }

    private static interface TestComposite1
        extends Composite, TestA, TestB
    {
    }
}