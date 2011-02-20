package org.qi4j.tests.jira.qi75;

import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static junit.framework.Assert.*;

public class IssueTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TestComposite.class ).setMetaInfo( "Foo" );
        module.addEntities( TestEntity.class ).setMetaInfo( "Bar" );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void compositeMethodsWhenBuilding()
        throws SecurityException, NoSuchMethodException
    {
        TransientBuilder<TestComposite> testBuilder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
        assertEquals( TestComposite.class, testBuilder.prototype().type() );
        assertEquals( "Foo", testBuilder.prototype().metaInfo( String.class ) );

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<TestEntity> entityBuilder = unitOfWork.newEntityBuilder( TestEntity.class );
        assertEquals( TestEntity.class, entityBuilder.instance().type() );
        assertEquals( "Bar", entityBuilder.instance().metaInfo( String.class ) );
        unitOfWork.discard();
    }

    interface TestComposite
        extends TransientComposite
    {
    }

    interface TestEntity
        extends EntityComposite
    {
    }
}