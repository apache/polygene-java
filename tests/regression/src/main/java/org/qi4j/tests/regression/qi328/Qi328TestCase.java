package org.qi4j.tests.regression.qi328;

import org.junit.Test;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;

public class Qi328TestCase extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values(  OuterValue.class );
    }

    @Test
    public void givenValueCompositeWithInternalStateMixinWhenSerializingExpectInternalStateInOutput()
        throws Exception
    {
        ValueBuilder<OuterValue> builder = module.newValueBuilder( OuterValue.class );
        OuterValue.State prototype = builder.prototypeFor( OuterValue.State.class );
        builder.prototype().firstName().set( "Niclas" );
        prototype.lastName().set(  "Hedhman" );
        OuterValue value = builder.newInstance();
        System.out.println("Niclas: " + value);
        value.printName();
        assertEquals(  "{\"firstName\":\"Niclas\",\"lastName\":\"Hedhman\"}", value.toString() );
    }

    @Mixins( OuterValue.Mixin.class )
    public interface OuterValue extends ValueComposite
    {
        void printName();

        Property<String> firstName();

        public abstract class Mixin
            implements OuterValue
        {
            @This
            private State state;

            @Override
            public void printName()
            {
                System.out.println( firstName().get() + " " + state.lastName().get() );
            }
        }

        interface State
        {
            Property<String> lastName();
        }
    }


}
