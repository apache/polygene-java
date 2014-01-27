/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
 *
 */
package org.qi4j.test.value;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Assert that ValueSerialization behaviour on ValueComposites is correct.
 */
// TODO Assert Association and ManyAssociation serialization behaviour!
// TODO Assert Arrays behaviour!
// TODO Assert Generics behaviour!
public abstract class AbstractValueCompositeSerializationTest
    extends AbstractQi4jTest
{

    @Rule
    @SuppressWarnings( "PublicField" )
    public TestName testName = new TestName();
    private Logger log;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class, AnotherValue.class, FooValue.class, CustomFooValue.class,
                       SpecificCollection.class /*, SpecificValue.class, GenericValue.class */ );
        new EntityTestAssembler().assemble( module );
        module.entities( BarEntity.class );
    }

    @Before
    public void before()
    {
        log = LoggerFactory.getLogger( testName.getMethodName() );
        module.injectTo( this );
    }
    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

    @Test
    public void givenValueCompositeWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            SomeValue some = buildSomeValue();

            // Serialize using injected service
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            valueSerialization.serialize( some, output );
            String stateString = output.toString( "UTF-8" );

            log.info( "Complex ValueComposite state:\n\n{}\n", stateString );

            // Deserialize using Module API
            SomeValue some2 = module.newValueFromSerializedState( SomeValue.class, stateString );

            assertThat( "Same value toString", some.toString(), equalTo( some2.toString() ) );
            assertThat( "Same value", some, equalTo( some2 ) );
            assertThat( "Same JSON value toString", stateString, equalTo( some2.toString() ) );
            assertThat( "Same JSON value", some.customFoo().get() instanceof CustomFooValue, is( true ) );
            assertThat( "Same JSON value explicit", some.customFooValue().get() instanceof CustomFooValue, is( true ) );

            assertThat( "String Integer Map", some2.stringIntMap().get().get( "foo" ), equalTo( 42 ) );
            assertThat( "String Value Map", some2.stringValueMap().get().get( "foo" ).internalVal(), equalTo( "Bar" ) );
            assertThat( "Nested Entities", some2.barAssociation().get().cathedral().get(), equalTo( "bazar in barAssociation" ) );

            assertThat( "Money Support", some2.money().get(), equalTo( Money.of( CurrencyUnit.USD, 42.23 ) ) );
            assertThat( "BigMoney Support", some2.bigMoney().get(), equalTo( BigMoney.of( CurrencyUnit.USD, 42.232323 ) ) );
        }
        catch( Exception ex )
        {
            log.error( ex.getMessage(), ex );
            throw ex;
        }
        finally
        {
            uow.discard();
        }
    }

    /**
     * @return a SomeValue ValueComposite whose state is populated with test data.
     */
    private SomeValue buildSomeValue()
    {
        ValueBuilder<SomeValue> builder = module.newValueBuilder( SomeValue.class );
        SomeValue proto = builder.prototype();
        proto.anotherList().get().add( module.newValue( AnotherValue.class ) );

        ValueBuilder<SpecificCollection> specificColBuilder = module.newValueBuilder( SpecificCollection.class );
        SpecificCollection specificColProto = specificColBuilder.prototype();
        List<String> genericList = new ArrayList<>( 2 );
        genericList.add( "Some" );
        genericList.add( "String" );
        specificColProto.genericList().set( genericList );
        proto.specificCollection().set( specificColBuilder.newInstance() );

        /*
         ValueBuilder<SpecificValue> specificValue = module.newValueBuilder(SpecificValue.class);
         specificValue.prototype().item().set("Foo");
         proto.specificValue().set(specificValue.newInstance());
         */
        ValueBuilder<AnotherValue> valueBuilder = module.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( "Foo" );
        valueBuilder.prototypeFor( AnotherValueInternalState.class ).val2().set( "Bar" );
        AnotherValue anotherValue = valueBuilder.newInstance();

        // FIXME Some Control Chars are not supported in JSON nor in XML, what should we do about it?
        // Should Qi4j Core ensure the chars used in strings are supported by the whole stack?
        // proto.string().set( "Foo\"Bar\"\nTest\f\t\b" );
        proto.string().set( "Foo\"Bar\"\nTest\t" );
        proto.string2().set( "/Foo/bar" );
        proto.number().set( 42L );
        proto.date().set( new Date() );
        proto.dateTime().set( new DateTime() );
        proto.localDate().set( new LocalDate() );
        proto.localDateTime().set( new LocalDateTime() );
        proto.money().set( Money.of( CurrencyUnit.USD, 42.23 ) );
        proto.bigMoney().set( BigMoney.of( CurrencyUnit.USD, 42.232323 ) );
        proto.entityReference().set( EntityReference.parseEntityReference( "12345" ) );
        proto.stringIntMap().get().put( "foo", 42 );

        // Can't put more than one entry in Map because this test rely on the fact that the underlying implementations
        // maintain a certain order but it's not the case on some JVMs. On OpenJDK 8 they are reversed for example.
        // This should not be enforced tough as both the Map API and the JSON specification state that name-value pairs
        // are unordered.
        // As a consequence this test should be enhanced to be Map order independant.
        //
        // proto.stringIntMap().get().put( "bar", 67 );

        proto.stringValueMap().get().put( "foo", anotherValue );
        proto.another().set( anotherValue );
        proto.serializable().set( new SerializableObject() );
        proto.foo().set( module.newValue( FooValue.class ) );
        proto.fooValue().set( module.newValue( FooValue.class ) );
        proto.customFoo().set( module.newValue( CustomFooValue.class ) );
        proto.customFooValue().set( module.newValue( CustomFooValue.class ) );

        // Arrays
        // TODO FIXME Disabled as ValueComposite equality fails here
        //proto.primitiveByteArray().set( new byte[]
        //    {
        //        9, -12, 42, -12, 127, 23, -128, 73
        //    } );
        //proto.byteArray().set( new Byte[]
        //    {
        //        9, null, -12, 23, -12, 127, -128, 73
        //    } );

        // NestedEntities
        proto.barAssociation().set( buildBarEntity( "bazar in barAssociation" ) );
        proto.barEntityAssociation().set( buildBarEntity( "bazar in barEntityAssociation" ) );
        proto.barManyAssociation().add( buildBarEntity( "bazar ONE in barManyAssociation" ) );
        proto.barManyAssociation().add( buildBarEntity( "bazar TWO in barManyAssociation" ) );
        proto.barEntityManyAssociation().add( buildBarEntity( "bazar ONE in barEntityManyAssociation" ) );
        proto.barEntityManyAssociation().add( buildBarEntity( "bazar TWO in barEntityManyAssociation" ) );

        return builder.newInstance();
    }

    private BarEntity buildBarEntity( String cathedral )
    {
        EntityBuilder<BarEntity> barBuilder = module.currentUnitOfWork().newEntityBuilder( BarEntity.class );
        barBuilder.instance().cathedral().set( cathedral );
        return barBuilder.newInstance();
    }

    public enum TestEnum
    {

        somevalue, anothervalue
    }

    public interface SomeValue
        extends ValueComposite
    {

        Property<String> string();

        Property<String> string2();

        @Optional
        Property<String> nullString();

        @UseDefaults
        Property<String> emptyString();

        @UseDefaults
        Property<Long> number();

        Property<Date> date();

        Property<DateTime> dateTime();

        Property<LocalDate> localDate();

        Property<LocalDateTime> localDateTime();

        Property<Money> money();

        Property<BigMoney> bigMoney();

        Property<EntityReference> entityReference();

        @UseDefaults
        Property<List<String>> stringList();

        @UseDefaults
        Property<Map<String, Integer>> stringIntMap();

        @UseDefaults
        Property<Map<String, AnotherValue>> stringValueMap();

        Property<AnotherValue> another();

        @Optional
        Property<AnotherValue> anotherNull();

        @UseDefaults
        Property<List<AnotherValue>> anotherList();

        @Optional
        Property<List<AnotherValue>> anotherListNull();

        @UseDefaults
        Property<List<AnotherValue>> anotherListEmpty();

        @UseDefaults
        Property<TestEnum> testEnum();

        // TODO FIXME Disabled as ValueComposite equality fails here
        //Property<byte[]> primitiveByteArray();
        //
        //@Optional
        //Property<byte[]> primitiveByteArrayNull();
        //
        //Property<Byte[]> byteArray();
        //
        //@Optional
        //Property<Byte[]> byteArrayNull();

        Property<Object> serializable();

        Property<Foo> foo();

        Property<FooValue> fooValue();

        Property<Foo> customFoo();

        Property<FooValue> customFooValue();

        Property<SpecificCollection> specificCollection();

        /* Too complicated to do generics here for now
         Property<SpecificValue> specificValue();
         */
        @Optional
        Association<Bar> barAssociationOptional();

        Association<Bar> barAssociation();

        Association<BarEntity> barEntityAssociation();

        ManyAssociation<Bar> barManyAssociationEmpty();

        ManyAssociation<Bar> barManyAssociation();

        ManyAssociation<BarEntity> barEntityManyAssociation();
    }

    public interface SpecificCollection
        extends GenericCollection<String>
    {
    }

    public interface GenericCollection<TYPE>
        extends ValueComposite
    {

        @UseDefaults
        Property<List<TYPE>> genericList();
    }

    public interface SpecificValue
        extends GenericValue<String>
    {
    }

    public interface GenericValue<TYPE>
        extends ValueComposite
    {

        @Optional
        Property<TYPE> item();
    }

    @Mixins( AnotherValueMixin.class )
    public interface AnotherValue
        extends ValueComposite
    {

        @UseDefaults
        Property<String> val1();

        String internalVal();
    }

    public interface AnotherValueInternalState
    {

        @UseDefaults
        Property<String> val2();
    }

    public static abstract class AnotherValueMixin
        implements AnotherValue
    {

        @This
        private AnotherValueInternalState internalState;

        @Override
        public String internalVal()
        {
            return internalState.val2().get();
        }
    }

    public interface Foo
    {

        @UseDefaults
        Property<String> bar();
    }

    public interface FooValue
        extends Foo, ValueComposite
    {
    }

    public interface CustomFooValue
        extends FooValue
    {

        @UseDefaults
        Property<String> custom();
    }

    public interface Bar
    {

        @UseDefaults
        Property<String> cathedral();
    }

    public interface BarEntity
        extends Bar, EntityComposite
    {
    }

    public static class SerializableObject
        implements Serializable
    {

        private static final long serialVersionUID = 1L;
        private final String foo = "Foo";
        private final int val = 35;

        @Override
        @SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            SerializableObject that = (SerializableObject) o;
            return val == that.val && foo.equals( that.foo );
        }

        @Override
        public int hashCode()
        {
            int result = foo.hashCode();
            result = 31 * result + val;
            return result;
        }
    }

}
