package org.qi4j.tests.regression.qi327;

import org.junit.Test;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.core.testsupport.AbstractQi4jTest;

import static org.junit.Assert.fail;

public class Qi327Test extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( Person.class );
    }

    @Test
    public void givenValueCompositeThatImplementsInitializableWhenCreatingValueExpectInitializeMethodNotCalledInPrototype()
        throws Exception
    {
        ValueBuilder<Person> builder = valueBuilderFactory.newValueBuilder( Person.class );
        builder.prototype().name().set( "Niclas" );
        builder.newInstance();
    }

    @Mixins( PersonMixin.class )
    public interface Person extends Initializable, ValueComposite
    {
        Property<String> name();
    }

    public static abstract class PersonMixin
        implements Person
    {
        @Override
        public void initialize()
            throws InitializationException
        {
            if( name().get() == null )
                fail( "initialize() called before Property is set." );
        }
    }
}
