package org.qi4j.api.unitofwork;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilderTemplate;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class UnitOfWorkTemplateTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.entities( TestEntity.class );
    }

    @Test
    public void testTemplate()
    {
        new UnitOfWorkTemplate()
        {
            @Override
            protected void withUnitOfWork( UnitOfWork uow ) throws Exception
            {
                new EntityBuilderTemplate<TestEntity>(TestEntity.class)
                {
                    @Override
                    protected void build( TestEntity prototype )
                    {
                        prototype.name().set( "Rickard" );
                    }
                }.newInstance( module );
            }
        }.withModule( module );
    }

    interface TestEntity
    {
        Property<String> name();
    }
}
