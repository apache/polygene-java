package org.qi4j.test.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.Immutable;
import org.qi4j.composite.Optional;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.Qualifier;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Service;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Abstract test with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{
    @Service EntityStore store;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class );
        module.addEntities( TestEntity.class, TestValue.class );
        module.addObjects( getClass() );
    }

    @Override @After public void tearDown() throws Exception
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
            instance = unitOfWork.find( identity, TestEntity.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        TestEntity instance = builder.newInstance();
        String id = instance.identity().get();

        instance.name().set( "Test" );
        instance.association().set( instance );

/*
        EntityBuilder<TestValue> testValue = unitOfWork.newEntityBuilder( TestValue.class );
        TestValue state = testValue.stateOfComposite();
        state.someValue().set( "Foo" );
        state.otherValue().set( 5 );

        TestValue value = testValue.newInstance();
        instance.valueProperty().set( value );
        value.mutate();
*/

        instance.manyAssociation().add( instance );

        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );

        instance.setAssociation().add( instance );
        instance.setAssociation().add( instance );
        return instance;
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
        extends ValueComposite<TestValue>, EntityComposite
    {
        @Immutable Property<String> someValue();

        @Immutable Property<Integer> otherValue();
    }

    public interface Mutable<T>
    {
        EntityBuilder<T> mutate();

    }

    @Mixins( ValueComposite.ValueCompositeMixin.class )
    public interface ValueComposite<T>
        extends Mutable<T>
    {

        public abstract class ValueCompositeMixin<T>
            implements Mutable<T>
        {
            @This EntityComposite entity;
            @Structure CompositeBuilderFactory cbf;
            @Structure UnitOfWorkFactory uowf;

            public EntityBuilder<T> mutate()
            {
                final EntityBuilder<T> entityBuilder = (EntityBuilder<T>) uowf.currentUnitOfWork().newEntityBuilder( entity.type() );
                //CompositeBuilder<T> builder = (CompositeBuilder<T>) cbf.newCompositeBuilder( entity.type() );
                T state = entityBuilder.stateOfComposite();

                // Copy current state
                Method[] methods = state.getClass().getMethods();
                for( Method method : methods )
                {
                    if( Property.class.isAssignableFrom( method.getReturnType() ) )
                    {
                        try
                        {
                            Property<Object> oldProperty = (Property<Object>) method.invoke( entity );
                            Property<Object> newProperty = (Property<Object>) method.invoke( state );
                            newProperty.set( oldProperty.get() );
                        }
                        catch( IllegalAccessException e )
                        {
                            e.printStackTrace();
                        }
                        catch( InvocationTargetException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }

                return entityBuilder;
            }
        }
    }
}