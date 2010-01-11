package org.qi4j.test.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Abstract test with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{
    @Service
    private EntityStore store;

    @Structure
    private Module module;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class );
        module.addEntities( TestEntity.class );
        module.addValues( TestValue.class, TestValue2.class, TjabbaValue.class );
        module.addObjects( getClass() );
    }

    @Before
    public void init()
    {
        objectBuilderFactory.newObjectBuilder( AbstractEntityStoreTest.class ).injectTo( this );
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        builder.instance().dateValue().set( new Date() );
        TestEntity instance = builder.newInstance();

        instance.name().set( "Test" );
        instance.association().set( instance );

        ValueBuilder<Tjabba> valueBuilder4 = valueBuilderFactory.newValueBuilder( Tjabba.class );
        final Tjabba prototype4 = valueBuilder4.prototype();
        prototype4.bling().set( "BlinkLjus" );

        // Set value
        ValueBuilder<TestValue2> valueBuilder2 = valueBuilderFactory.newValueBuilder( TestValue2.class );
        TestValue2 prototype2 = valueBuilder2.prototype();
        prototype2.stringValue().set( "Bar" );
        prototype2.anotherValue().set( valueBuilder4.newInstance() );
        prototype2.anotherValue().set( valueBuilder4.newInstance() );

        ValueBuilder<Tjabba> valueBuilder3 = valueBuilderFactory.newValueBuilder( Tjabba.class );
        final Tjabba prototype3 = valueBuilder3.prototype();
        prototype3.bling().set( "Brakfis" );

        ValueBuilder<TestValue> valueBuilder1 = valueBuilderFactory.newValueBuilder( TestValue.class );
        TestValue prototype = valueBuilder1.prototype();
        prototype.listProperty().get().add( "Foo" );

        prototype.valueProperty().set( valueBuilder2.newInstance() );
        prototype.tjabbaProperty().set( valueBuilder3.newInstance() );
        Map<String, String> mapValue = new HashMap<String, String>();
        mapValue.put( "foo", "bar" );
        prototype.serializableProperty().set( mapValue );
        instance.valueProperty().set( valueBuilder1.newInstance() );

        instance.manyAssociation().add( 0, instance );

        return instance;
    }

    @Test
    public void whenNewEntityThenCanFindEntityAndCorrectValues()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity instance = createEntity( unitOfWork );
            unitOfWork.complete();

            // Find entity
            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            instance = unitOfWork.get( instance );

            // Check state
            assertThat( "property 'name' has correct value", instance.name().get(), equalTo( "Test" ) );
            assertThat( "property 'unsetName' has correct value", instance.unsetName().get(), equalTo( null ) );

            assertThat( "property has correct value", instance.valueProperty()
                .get()
                .valueProperty()
                .get()
                .stringValue().get(), equalTo( "Bar" ) );
            assertThat( "property has correct value", instance.valueProperty()
                .get()
                .listProperty()
                .get().get( 0 ), equalTo( "Foo" ) );
            assertThat( "property has correct value", instance.valueProperty()
                .get()
                .valueProperty()
                .get()
                .anotherValue()
                .get()
                .bling().get(), equalTo( "BlinkLjus" ) );
            assertThat( "property has correct value", instance.valueProperty()
                .get()
                .tjabbaProperty()
                .get()
                .bling().get(), equalTo( "Brakfis" ) );
            Map<String, String> mapValue = new HashMap<String, String>();
            mapValue.put( "foo", "bar" );
            assertThat( "property has correct value", instance.valueProperty()
                .get()
                .serializableProperty().get(), equalTo( mapValue ) );

            assertThat( "association has correct value", instance.association().get(), equalTo( instance ) );
            assertThat( "manyAssociation has correct value", instance.manyAssociation()
                .iterator().next(), equalTo( instance ) );
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
        TestEntity instance = unitOfWork.get( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            unitOfWork.get( TestEntity.class, identity );
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
    public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has not changed", newVersion, equalTo( version ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            testEntity.name().set( "Rickard" );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has changed", newVersion, not( equalTo( version ) ) );

            unitOfWork.complete();
        }
    }

    @Test
    public void givenManyAssociationIsModifiedWhenUnitOfWorkCompletesThenStoreState()
        throws UnitOfWorkCompletionException
    {
        TestEntity testEntity;
        String version;

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            testEntity.manyAssociation().add( 0, testEntity );
            version = spi.getEntityState( testEntity ).version();

            unitOfWork.complete();
        }

        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.getEntityState( testEntity ).version();
            assertThat( "version has changed", newVersion, not( equalTo( version ) ) );

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
        String version;
        {
            // Start working with Entity in one UoW
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            version = spi.getEntityState( testEntity1 ).version();
            if( version.equals( "" ) )
            {
                unitOfWork1.discard();
                return; // Store doesn't track versions - no point in testing it
            }
            testEntity1.name().set( "A" );
            testEntity1.unsetName().set( "A" );
        }

        {
            // Start working with same Entity in another UoW, and complete it
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity testEntity2 = unitOfWork.get( testEntity );
            assertThat( "version is correct", spi.getEntityState( testEntity1 ).version(), equalTo( version ) );
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
                unitOfWork1.discard();
            }
        }

        {
            // Check values
            unitOfWork1 = unitOfWorkFactory.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            assertThat( "property name has not been set", testEntity1.name().get(), equalTo( "B" ) );
            assertThat( "version is incorrect", spi.getEntityState( testEntity1 ).version(), not( equalTo( version ) ) );
            unitOfWork1.discard();
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        @UseDefaults
        Property<Integer> intValue();

        @UseDefaults
        Property<Long> longValue();

        @UseDefaults
        Property<Double> doubleValue();

        @UseDefaults
        Property<Float> floatValue();

        @UseDefaults
        Property<Boolean> booleanValue();

        @Optional
        Property<Date> dateValue();

        @Optional
        Property<String> name();

        @Optional
        Property<String> unsetName();

        @Optional
        Property<TestValue> valueProperty();

        @Optional
        Association<TestEntity> association();

        @Optional
        Association<TestEntity> unsetAssociation();

        ManyAssociation<TestEntity> manyAssociation();
    }

    public interface TjabbaValue
        extends Tjabba, ValueComposite
    {

    }

    public interface Tjabba
    {
        Property<String> bling();
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

        @UseDefaults
        Property<Map<String, Tjabba>> mapProperty();

        Property<TestValue2> valueProperty();

        Property<Tjabba> tjabbaProperty();

        Property<Map<String, String>> serializableProperty();
    }

    public interface TestValue2
        extends ValueComposite
    {
        Property<String> stringValue();

        Property<Tjabba> anotherValue();
    }

    public enum TestEnum
    {
        VALUE1, VALUE2, VALUE3
    }
}