package org.qi4j.api.value;

import org.junit.Test;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ValueBuilderTemplateTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( TestValue.class );
    }

    @Test
    public void testTemplate()
    {
        new TestBuilder( "Rickard" ).newInstance( module );
    }

    @Test
    public void testAnonymousTemplate()
    {
        new ValueBuilderTemplate<TestValue>( TestValue.class )
        {
            @Override
            protected void build( TestValue prototype )
            {
                prototype.name().set( "Rickard" );
            }
        }.newInstance( module );
    }

    interface TestValue
        extends ValueComposite
    {
        Property<String> name();
    }

    class TestBuilder
        extends ValueBuilderTemplate<TestValue>
    {
        String name;

        TestBuilder( String name )
        {
            super( TestValue.class );
            this.name = name;
        }

        @Override
        protected void build( TestValue prototype )
        {
            prototype.name().set( name );
        }
    }

    ;
}
