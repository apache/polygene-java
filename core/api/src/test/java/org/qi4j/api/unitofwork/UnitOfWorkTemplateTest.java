package org.qi4j.api.unitofwork;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilderTemplate;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * TODO
 */
public class UnitOfWorkTemplateTest
    extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
    }

    @Test
    public void testTemplate()
        throws UnitOfWorkCompletionException
    {
        new UnitOfWorkTemplate<Void, RuntimeException>()
        {
            @Override
            protected Void withUnitOfWork( UnitOfWork uow )
                throws RuntimeException
            {
                new EntityBuilderTemplate<TestEntity>( TestEntity.class )
                {
                    @Override
                    protected void build( TestEntity prototype )
                    {
                        prototype.name().set( "Rickard" );
                    }
                }.newInstance( module );

                return null;
            }
        }.withModule( module );
    }

    interface TestEntity
        extends EntityComposite
    {
        Property<String> name();
    }
}
