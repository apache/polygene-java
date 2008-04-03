package org.qi4j.test.entity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Abstract test with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorComposite.class );
        module.addComposites( TestComposite.class );
    }

    @Test
    public void whenNewEntityThenCanFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite instance = createEntity( unitOfWork );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        instance = unitOfWork.dereference( instance );

        // Check state
        assertThat( "property has correct value", instance.name().get(), equalTo( "Test" ) );
        assertThat( "property has correct value", instance.unsetName().get(), equalTo( null ) );
        assertThat( "association has correct value", instance.association().get(), equalTo( instance ) );
        assertThat( "manyAssociation has correct value", instance.manyAssociation().iterator().next(), equalTo( instance ) );
        assertThat( "listAssociation has correct value", instance.listAssociation().iterator().next(), equalTo( instance ) );
        assertThat( "setAssociation has correct value", instance.setAssociation().iterator().next(), equalTo( instance ) );
        assertThat( "setAssociation has correct size", instance.setAssociation().size(), equalTo( 1 ) );
        assertThat( "listAssociation has correct size", instance.listAssociation().size(), equalTo( 3 ) );

        unitOfWork.discard();
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite newInstance = createEntity( unitOfWork );
        String identity = newInstance.identity().get();
        unitOfWork.complete();

        // Remove entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite instance = unitOfWork.dereference( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( identity, TestComposite.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    protected TestComposite createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( TestComposite.class );
        TestComposite instance = builder.newInstance();
        String id = instance.identity().get();

        instance.name().set( "Test" );
        instance.association().set( instance );

        instance.manyAssociation().add( instance );

        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );

        instance.setAssociation().add( instance );
        instance.setAssociation().add( instance );
        return instance;
    }

    public interface TestComposite
        extends EntityComposite
    {
        Property<String> name();

        Property<String> unsetName();

        Association<TestComposite> association();

        Association<TestComposite> unsetAssociation();

        ManyAssociation<TestComposite> manyAssociation();

        ListAssociation<TestComposite> listAssociation();

        SetAssociation<TestComposite> setAssociation();
    }
}