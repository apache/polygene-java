package org.qi4j.tests.regression.qi94;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

public class IssueTest
    extends AbstractQi4jTest
{
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addEntities( Item.class, ItemType.class );
        new EntityTestAssembler().assemble( aModule );
    }

    @Test
    public void entityBuilderAssociationTypeIsNotNull()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Item> builder = uow.newEntityBuilder( Item.class );
            assertEquals( ItemType.class, builder.instance().typeOfItem().type() );
        }
        finally
        {
            uow.discard();
        }
    }

    interface Item
        extends EntityComposite
    {
        Association<ItemType> typeOfItem();
    }

    interface ItemType
        extends EntityComposite
    {
        Property<String> name();
    }
}
