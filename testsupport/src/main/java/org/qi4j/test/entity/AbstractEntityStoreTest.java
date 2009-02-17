package org.qi4j.test.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.association.Qualifier;
import org.qi4j.api.entity.association.SetAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Abstract test with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{
    @Service EntityStore store;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class );
        module.addEntities( TestEntity.class);
        module.addValues( TestValue.class, TestValue2.class );
        module.addObjects( getClass() );
    }

    @Override @After public void tearDown()
        throws Exception
    {
        try
        {
            // Remove all state that was created
            objectBuilderFactory.newObjectBuilder( AbstractEntityStoreTest.class ).injectTo( this );

            List<QualifiedIdentity> stateToRemove = new ArrayList<QualifiedIdentity>();
            for( EntityState entityState : store )
            {
                stateToRemove.add( entityState.qualifiedIdentity() );
            }
            store.prepare( Collections.EMPTY_LIST, Collections.EMPTY_LIST, stateToRemove ).commit();

            super.tearDown();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        TestEntity instance = builder.newInstance();

        instance.name().set( "Test" );
        instance.association().set( instance );

        // Set value
        ValueBuilder<TestValue2> valueBuilder2 = valueBuilderFactory.newValueBuilder( TestValue2.class );
        valueBuilder2.prototype().stringValue().set("Bar");

        ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
        TestValue prototype = valueBuilder.prototype();
        prototype.listProperty().get().add( "Foo" );
        prototype.valueProperty().set( valueBuilder2.newInstance() );
        Map<String, String> mapValue = new HashMap<String, String>();
        mapValue.put( "foo", "bar" );
        prototype.serializableProperty().set( mapValue );

        instance.valueProperty().set( valueBuilder.newInstance() );
        instance.manyAssociation().add( instance );

        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );

        instance.setAssociation().add( instance );
        instance.setAssociation().add( instance );
        return instance;
    }

    @Test
    public void whenNewEntityThenCanFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = null;
        try
        {
            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity instance = createEntity( unitOfWork );
            unitOfWork.complete();

            // Find entity
            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            instance = unitOfWork.dereference( instance );

            // Check state
            assertThat( "property has correct value", instance.name().get(), equalTo( "Test" ) );
            assertThat( "property has correct value", instance.unsetName().get(), equalTo( null ) );
            assertThat( "property has correct value", instance.valueProperty().get().valueProperty().get().stringValue().get(), equalTo( "Bar" ) );
            assertThat( "association has correct value", instance.association().get(), equalTo( instance ) );
            assertThat( "manyAssociation has correct value", instance.manyAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "listAssociation has correct value", instance.listAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "setAssociation has correct value", instance.setAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "setAssociation has correct size", instance.setAssociation().size(), equalTo( 1 ) );
            assertThat( "listAssociation has correct size", instance.listAssociation().size(), equalTo( 3 ) );

            unitOfWork.discard();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
            throw e;
        }
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity newInstance = createEntity( unitOfWork );
        String identity = newInstance.identity().get();
        unitOfWork.complete();

        // Remove entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity instance = unitOfWork.dereference( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            unitOfWork.find( identity, TestEntity.class );
            fail( "Should not be able to find entity" );
        }
        catch( NoSuchEntityException e )
        {
            // Ok!
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        long version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        long version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            testEntity.name().set( "Rickard" );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version + 1 ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenManyAssociationIsModifiedWhenUnitOfWorkCompletesThenStoreState() throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        long version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }


        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            testEntity.manyAssociation().add( testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();

        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.dereference( testEntity );
            long newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version + 1 ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        UnitOfWork unitOfWork1;
        TestEntity testEntity1;
        {
            // Start working with Entity in one UoW
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.dereference( testEntity );
            testEntity1.name().set( "A" );
            testEntity1.unsetName().set( "A" );
        }

        {
            // Start working with same Entity in another UoW, and complete it
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity testEntity2 = unitOfWork.dereference( testEntity );
            testEntity2.name().set( "B" );
            unitOfWork.complete();
        }

        {
            // Try to complete first UnitOfWork
            try
            {
                unitOfWork1.complete();
                fail( "Should have thrown concurrent modification exception" );
            }
            catch( ConcurrentEntityModificationException e )
            {
                e.refreshEntities( unitOfWork1 );

                assertThat( "property name has been refreshed", testEntity1.name().get(), equalTo( "B" ) );

                // Set it again
                testEntity1.name().set( "A" );

                unitOfWork1.complete();
            }
        }

        {
            // Check values
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.dereference( testEntity );
            assertThat( "property name has been set", testEntity1.name().get(), equalTo( "A" ) );
            assertThat( "version is correct", spi.getEntityState( testEntity1 ).version(), equalTo( 2L ) );
            unitOfWork1.discard();
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Optional Property<String> name();

        @Optional Property<String> unsetName();

        @Optional Property<TestValue> valueProperty();

        @Optional Association<TestEntity> association();

        @Optional Association<TestEntity> unsetAssociation();

        ManyAssociation<TestEntity> manyAssociation();

        ListAssociation<TestEntity> listAssociation();

        SetAssociation<TestEntity> setAssociation();

        @Optional Association<Qualifier<TestEntity, TestEntity>> qualifier();
    }

    public interface TestValue
        extends ValueComposite
    {
        @UseDefaults
        Property<String> stringProperty();

        @UseDefaults
        Property<Integer> intProperty();
        
        @UseDefaults
        Property<TestEnum> enumProperty();

        @UseDefaults
        Property<List<String>> listProperty();

        Property<TestValue2> valueProperty();

        Property<Map<String,String>> serializableProperty();
    }

    public interface TestValue2
        extends ValueComposite
    {
        Property<String> stringValue();
    }
    
    public enum TestEnum
    {
        VALUE1, VALUE2, VALUE3
    }
}