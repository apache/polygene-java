package org.qi4j.regression.qi53;

import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;

public class IssueTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CostPerUnitComposite.class );
    }

    @Test
    public void genericPropertiesAndParameters()
        throws SecurityException, NoSuchMethodException
    {
        TransientBuilder<CostPerUnitComposite> builder = module.newTransientBuilder( CostPerUnitComposite.class );
        builder.prototype().unit().set( new Unit<Integer>( 10 ) );
        CostPerUnitComposite test = builder.newInstance();
        assertEquals( 10, test.unit().get().value );
        assertEquals( 50, test.toCostPer( new Unit<Integer>( 50 ) ).unit().get().value );
    }

    public interface CostPerUnit
    {
        @Immutable
        Property<Unit<?>> unit();

        CostPerUnit toCostPer( Unit<?> unit );
    }

    public static class Unit<T>
    {
        private T value;

        public Unit( T value )
        {
            this.value = value;
        }

        T get()
        {
            return value;
        }
    }

    public static abstract class CostPerUnitMixin
        implements CostPerUnit
    {

        @This
        CostPerUnit costPerUnit;
        @Structure
        TransientBuilderFactory builderFactory;

        public CostPerUnit toCostPer( Unit<?> unit )
        {
            TransientBuilder<CostPerUnitComposite> builder =
                builderFactory.newTransientBuilder( CostPerUnitComposite.class );

            builder.prototype().unit().set( unit );
            return builder.newInstance();
        }
    }

    @Mixins( { CostPerUnitMixin.class } )
    public interface CostPerUnitComposite
        extends CostPerUnit, TransientComposite
    {
    }
}