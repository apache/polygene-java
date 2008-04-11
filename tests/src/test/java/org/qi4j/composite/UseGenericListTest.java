package org.qi4j.composite;

import java.util.ArrayList;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.scope.Uses;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class UseGenericListTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestCase.class );
    }

    @Test
    public void givenMixinUsesGenericListWhenUseListThenInjectWorks()
    {
        CompositeBuilder<TestCase> builder = compositeBuilderFactory.newCompositeBuilder( TestCase.class );

        ArrayList<String> list = new ArrayList<String>();
        list.add( "Hello" );
        list.add( "Bye" );
        builder.use( list );

        TestCase TestCase = builder.newInstance();
        TestCase.sayHello();

    }

    @Mixins( TestMixin.class )
    public interface TestCase
        extends Composite
    {
        void sayHello();
    }

    public abstract static class TestMixin implements TestCase
    {
        @Uses ArrayList<String> messages;

        public void sayHello()
        {
            System.out.println( messages );
        }
    }
}
