/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 * Copyright (c) 2008, Michael Hunger. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2013, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
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
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Abstract satisfiedBy with tests for the EntityStore interface.
 */
public abstract class AbstractEntityStoreTest
    extends AbstractQi4jTest
{

    @Service
    private EntityStore store;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class );
        module.entities( TestEntity.class );
        module.values( TestValue.class, TestValue2.class, TjabbaValue.class );
        module.objects( getClass() );
    }

    @Before
    public void init()
    {
        module.injectTo( this );
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
        instance.intValue().set( 42 );
        instance.longValue().set( 42L );
        instance.doubleValue().set( 42D );
        instance.floatValue().set( 42F );
        instance.booleanValue().set( Boolean.TRUE );
        instance.bigIntegerValue().set( new BigInteger( "42" ) );
        instance.bigDecimalValue().set( new BigDecimal( "42" ) );
        instance.dateValue().set( new DateTime( "2020-03-04T13:24:35", UTC ).toDate() );
        instance.dateTimeValue().set( new DateTime( "2020-03-04T13:24:35", UTC ) );
        instance.localDateTimeValue().set( new LocalDateTime( "2020-03-04T13:23:00" ) );
        instance.localDateValue().set( new LocalDate( "2020-03-04" ) );
        instance.money().set( Money.of( CurrencyUnit.USD, 42.23 ) );
        instance.bigMoney().set( BigMoney.of( CurrencyUnit.USD,
                                              new BigDecimal( "42424242424242424242424242.232323230000000000000" ) ) );
        instance.association().set( instance );

        ValueBuilder<Tjabba> valueBuilder4 = module.newValueBuilder( Tjabba.class );
        final Tjabba prototype4 = valueBuilder4.prototype();
        prototype4.bling().set( "BlinkLjus" );

        // Set value
        ValueBuilder<TestValue2> valueBuilder2 = module.newValueBuilder( TestValue2.class );
        TestValue2 prototype2 = valueBuilder2.prototype();
        prototype2.stringValue().set( "Bar" );
        Tjabba newValue = valueBuilder4.newInstance();
        prototype2.anotherValue().set( newValue );
        prototype2.anotherValue().set( newValue );

        ValueBuilder<Tjabba> valueBuilder3 = module.newValueBuilder( Tjabba.class );
        final Tjabba prototype3 = valueBuilder3.prototype();
        prototype3.bling().set( "Brakfis" );

        ValueBuilder<TestValue> valueBuilder1 = module.newValueBuilder( TestValue.class );
        TestValue prototype = valueBuilder1.prototype();
        prototype.enumProperty().set( TestEnum.VALUE3 );
        prototype.listProperty().get().add( "Foo" );

        prototype.valueProperty().set( valueBuilder2.newInstance() );
        prototype.tjabbaProperty().set( valueBuilder3.newInstance() );
        Map<String, String> mapValue = new HashMap<>( 1 );
        mapValue.put( "foo", "bar" );
        prototype.mapStringStringProperty().set( mapValue );
        instance.valueProperty().set( valueBuilder1.newInstance() );

        instance.manyAssociation().add( 0, instance );

        instance.namedAssociation().put( "foo", instance );
        instance.namedAssociation().put( "bar", instance );

        return instance;
    }

    @Test
    public void whenNewEntityThenCanFindEntityAndCorrectValues()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            TestEntity instance = createEntity( unitOfWork );
            unitOfWork.complete();

            // Find entity
            unitOfWork = module.newUnitOfWork();
            instance = unitOfWork.get( instance );

            // Check state
            assertThat( "property 'intValue' has correct value",
                        instance.intValue().get(),
                        equalTo( 42 ) );

            assertThat( "property 'longValue' has correct value",
                        instance.longValue().get(),
                        equalTo( 42L ) );

            assertThat( "property 'doubleValue' has correct value",
                        instance.doubleValue().get(),
                        equalTo( 42D ) );

            assertThat( "property 'floatValue' has correct value",
                        instance.floatValue().get(),
                        equalTo( 42F ) );

            assertThat( "property 'booleanValue' has correct value",
                        instance.booleanValue().get(),
                        equalTo( Boolean.TRUE ) );

            assertThat( "property 'bigInteger' has correct value",
                        instance.bigIntegerValue().get(),
                        equalTo( new BigInteger( "42" ) ) );

            assertThat( "property 'bigDecimal' has correct value",
                        instance.bigDecimalValue().get(),
                        equalTo( new BigDecimal( "42" ) ) );

            assertThat( "property 'dateValue' has correct value",
                        instance.dateValue().get(),
                        equalTo( new DateTime( "2020-03-04T13:24:35", UTC ).toDate() ) );

            assertThat( "property 'dateTimeValue' has correct value",
                        instance.dateTimeValue().get(),
                        equalTo( new DateTime( "2020-03-04T13:24:35", UTC ) ) );

            assertThat( "property 'localDateTimeValue' has correct value",
                        instance.localDateTimeValue().get(),
                        equalTo( new LocalDateTime( "2020-03-04T13:23:00", UTC ) ) );

            assertThat( "property 'localDateValue' has correct value",
                        instance.localDateValue().get(),
                        equalTo( new LocalDate( "2020-03-04" ) ) );

            assertThat( "property 'bigMoney' has correct value",
                        instance.bigMoney().get(),
                        equalTo( BigMoney.of( CurrencyUnit.USD,
                                              new BigDecimal( "42424242424242424242424242.232323230000000000000" ) ) ) );

            assertThat( "property 'money' has correct value",
                        instance.money().get(),
                        equalTo( Money.of( CurrencyUnit.USD, 42.23 ) ) );

            assertThat( "property 'name' has correct value",
                        instance.name().get(),
                        equalTo( "Test" ) );

            assertThat( "property 'unsetName' has correct value",
                        instance.unsetName().get(),
                        equalTo( null ) );

            assertThat( "property 'emptyName' has correct value",
                        instance.emptyName().get(),
                        equalTo( "" ) );

            assertThat( "property 'valueProperty.stringValue' has correct value",
                        instance.valueProperty().get().valueProperty().get().stringValue().get(),
                        equalTo( "Bar" ) );

            assertThat( "property 'valueProperty.listProperty' has correct value",
                        instance.valueProperty().get().listProperty().get().get( 0 ),
                        equalTo( "Foo" ) );

            assertThat( "property 'valueProperty.enumProperty' has correct value",
                        instance.valueProperty().get().enumProperty().get(),
                        equalTo( TestEnum.VALUE3 ) );

            assertThat( "property 'valueProperty.anotherValue.bling' has correct value",
                        instance.valueProperty().get().valueProperty().get().anotherValue().get().bling().get(),
                        equalTo( "BlinkLjus" ) );

            assertThat( "property 'valueProperty.tjabbaProperty.bling' has correct value",
                        instance.valueProperty().get().tjabbaProperty().get().bling().get(),
                        equalTo( "Brakfis" ) );

            Map<String, String> mapValue = new HashMap<>();
            mapValue.put( "foo", "bar" );
            assertThat( "property 'valueProperty.mapStringStringProperty' has correct value",
                        instance.valueProperty().get().mapStringStringProperty().get(),
                        equalTo( mapValue ) );

            assertThat( "association has correct value",
                        instance.association().get(),
                        equalTo( instance ) );

            assertThat( "manyAssociation has correct value",
                        instance.manyAssociation().iterator().next(),
                        equalTo( instance ) );

            assertThat( "namedAssociation has correct 'foo' value",
                        instance.namedAssociation().get( "foo" ),
                        equalTo( instance ) );

            assertThat( "namedAssociation has correct 'bar' value",
                        instance.namedAssociation().get( "bar" ),
                        equalTo( instance ) );

            unitOfWork.discard();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        TestEntity newInstance = createEntity( unitOfWork );
        String identity = newInstance.identity().get();
        unitOfWork.complete();

        // Remove entity
        unitOfWork = module.newUnitOfWork();
        TestEntity instance = unitOfWork.get( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = module.newUnitOfWork();
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
            UnitOfWork unitOfWork = module.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            version = spi.entityStateOf( testEntity ).version();

            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
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
            UnitOfWork unitOfWork = module.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            testEntity.name().set( "Rickard" );
            version = spi.entityStateOf( testEntity ).version();

            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
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
            UnitOfWork unitOfWork = module.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            testEntity.manyAssociation().add( 0, testEntity );
            version = spi.entityStateOf( testEntity ).version();

            unitOfWork.complete();
        }
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();
            testEntity = unitOfWork.get( testEntity );
            String newVersion = spi.entityStateOf( testEntity ).version();
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
            UnitOfWork unitOfWork = module.newUnitOfWork();
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );

            testEntity = builder.newInstance();
            unitOfWork.complete();
        }

        UnitOfWork unitOfWork1;
        TestEntity testEntity1;
        String version;
        {
            // Start working with Entity in one UoW
            unitOfWork1 = module.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            version = spi.entityStateOf( testEntity1 ).version();
            if( version.isEmpty() )
            {
                unitOfWork1.discard();
                return; // Store doesn't track versions - no point in testing it
            }
            testEntity1.name().set( "A" );
            testEntity1.unsetName().set( "A" );
        }
        {
            // Start working with same Entity in another UoW, and complete it
            UnitOfWork unitOfWork = module.newUnitOfWork();
            TestEntity testEntity2 = unitOfWork.get( testEntity );
            assertThat( "version is correct", spi.entityStateOf( testEntity1 ).version(), equalTo( version ) );
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
            unitOfWork1 = module.newUnitOfWork();
            testEntity1 = unitOfWork1.get( testEntity );
            assertThat( "property name has not been set", testEntity1.name().get(), equalTo( "B" ) );
            assertThat( "version is incorrect", spi.entityStateOf( testEntity1 ).version(),
                        not( equalTo( version ) ) );
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
        Property<BigInteger> bigIntegerValue();

        @Optional
        Property<BigDecimal> bigDecimalValue();

        @Optional
        Property<Date> dateValue();

        @Optional
        Property<DateTime> dateTimeValue();

        @Optional
        Property<LocalDateTime> localDateTimeValue();

        @Optional
        Property<LocalDate> localDateValue();

        @Optional
        Property<Money> money();

        @Optional
        Property<BigMoney> bigMoney();

        @Optional
        Property<String> name();

        @Optional
        Property<String> unsetName();

        @UseDefaults
        Property<String> emptyName();

        @Optional
        Property<TestValue> valueProperty();

        @Optional
        Association<TestEntity> association();

        @Optional
        Association<TestEntity> unsetAssociation();

        ManyAssociation<TestEntity> manyAssociation();

        NamedAssociation<TestEntity> namedAssociation();
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

        Property<Map<String, String>> mapStringStringProperty();
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
