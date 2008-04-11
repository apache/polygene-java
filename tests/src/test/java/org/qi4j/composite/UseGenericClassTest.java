package org.qi4j.composite;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.scope.Uses;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of generic class injection
 */
public class UseGenericClassTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestCase.class );
    }

    @Test
    public void givenMixinUsesGenericClassWhenUseClassThenInjectWorks()
    {
        CompositeBuilder<TestCase> builder = compositeBuilderFactory.newCompositeBuilder( TestCase.class );

        builder.use( UseGenericClassTest.class );

        TestCase testCase = builder.newInstance();
        assertThat( "class name is returned", testCase.test(), equalTo( UseGenericClassTest.class.getName() ) );

    }

    @Mixins( TestMixin.class )
    public interface TestCase
        extends Composite
    {
        String test();
    }

    public abstract static class TestMixin implements TestCase
    {
        @Uses Class<? extends TestCase> clazz;

        public String test()
        {
            return clazz.getName();
        }
    }
}