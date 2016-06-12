/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.test.value;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.Module;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Assert that ValueSerialization behaviour on ValueComposites is correct.
 */
// TODO Assert Arrays behaviour!
// TODO Assert Generics behaviour!
public abstract class AbstractValueCompositeSerializationTest
    extends AbstractZestTest
{
    @Rule
    public TestName testName = new TestName();

    @Structure
    Module moduleInstance;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( SomeValue.class, AnotherValue.class, FooValue.class, CustomFooValue.class,
                       SpecificCollection.class /*, SpecificValue.class, GenericValue.class */ );

        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module.layer().module( "persistence" ) );
        module.entities( BarEntity.class );
    }

    @Service
    protected ValueSerialization valueSerialization;

    @Test
    public void givenValueCompositeWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        try(UnitOfWork uow = unitOfWorkFactory.newUnitOfWork())
        {
            SomeValue some = buildSomeValue();

            // Serialize using injected service
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            valueSerialization.serialize( some, output );
            String stateString = output.toString( "UTF-8" );

            // Deserialize using Module API
            System.out.println(stateString);
            SomeValue some2 = moduleInstance.newValueFromSerializedState( SomeValue.class, stateString );

            assertThat( "String Integer Map", some2.stringIntMap().get().get( "foo" ), equalTo( 42 ) );
            assertThat( "String Value Map", some2.stringValueMap().get().get( "foo" ).internalVal(), equalTo( "Bar" ) );
            assertThat( "Nested Entities", some2.barAssociation().get().cathedral().get(), equalTo( "bazar in barAssociation" ) );

            assertThat( "Same value", some, equalTo( some2 ) );
            assertThat( "Same JSON value toString", stateString, equalTo( some2.toString() ) );
            assertThat( "Same JSON value", some.customFoo().get() instanceof CustomFooValue, is( true ) );
            assertThat( "Same JSON value explicit", some.customFooValue().get() instanceof CustomFooValue, is( true ) );
            assertThat( "Same value toString", some.toString(), equalTo( some2.toString() ) );
        }
    }

    /**
     * @return a SomeValue ValueComposite whose state is populated with test data.
     */
    private SomeValue buildSomeValue()
    {
        ValueBuilder<SomeValue> builder = moduleInstance.newValueBuilder( SomeValue.class );
        SomeValue proto = builder.prototype();
        proto.anotherList().get().add( moduleInstance.newValue( AnotherValue.class ) );

        ValueBuilder<SpecificCollection> specificColBuilder = moduleInstance.newValueBuilder( SpecificCollection.class );
        SpecificCollection specificColProto = specificColBuilder.prototype();
        List<String> genericList = new ArrayList<>( 2 );
        genericList.add( "Some" );
        genericList.add( "String" );
        specificColProto.genericList().set( genericList );
        proto.specificCollection().set( specificColBuilder.newInstance() );

        AnotherValue anotherValue1 = createAnotherValue( "Foo", "Bar" );
        AnotherValue anotherValue2 = createAnotherValue( "Habba", "ZoutZout" );
        AnotherValue anotherValue3 = createAnotherValue( "Niclas", "Hedhman" );

        // FIXME Some Control Chars are not supported in JSON nor in XML, what should we do about it?
        // Should Zest Core ensure the chars used in strings are supported by the whole stack?
        // proto.string().set( "Foo\"Bar\"\nTest\f\t\b" );
        proto.string().set( "Foo\"Bar\"\nTest\t" );
        proto.string2().set( "/Foo/bar" );
        proto.number().set( 43L );
        proto.localTime().set( LocalTime.now() );
        proto.dateTime().set( OffsetDateTime.of( 2020, 3, 4, 13, 24, 35, 0, ZoneOffset.ofHours( 1 ) ) );
        proto.localDate().set( LocalDate.now() );
        proto.localDateTime().set( LocalDateTime.now() );
        proto.entityReference().set( EntityReference.parseEntityReference( "12345" ) );
        proto.stringIntMap().get().put( "foo", 42 );

        // Can't put more than one entry in Map because this test rely on the fact that the underlying implementations
        // maintain a certain order but it's not the case on some JVMs. On OpenJDK 8 they are reversed for example.
        // This should not be enforced tough as both the Map API and the JSON specification state that name-value pairs
        // are unordered.
        // As a consequence this test should be enhanced to be Map order independant.
        //
        // proto.stringIntMap().get().put( "bar", 67 );

        proto.stringValueMap().get().put( "foo", anotherValue1 );
        proto.another().set( anotherValue1 );
        // proto.arrayOfValues().set( new AnotherValue[] { anotherValue1, anotherValue2, anotherValue3 } );
        proto.serializable().set( new SerializableObject() );
        proto.foo().set( moduleInstance.newValue( FooValue.class ) );
        proto.fooValue().set( moduleInstance.newValue( FooValue.class ) );
        proto.customFoo().set( moduleInstance.newValue( CustomFooValue.class ) );
        proto.customFooValue().set( moduleInstance.newValue( CustomFooValue.class ) );

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
        proto.barNamedAssociation().put( "bazar", buildBarEntity( "bazar in barNamedAssociation" ) );
        proto.barNamedAssociation().put( "cathedral", buildBarEntity( "cathedral in barNamedAssociation" ) );
        proto.barEntityNamedAssociation().put( "bazar", buildBarEntity( "bazar in barEntityNamedAssociation" ) );
        proto.barEntityNamedAssociation().put( "cathedral", buildBarEntity( "cathedral in barEntityNamedAssociation" ) );

        return builder.newInstance();
    }

    private AnotherValue createAnotherValue( String val1, String val2 )
    {
        ValueBuilder<AnotherValue> valueBuilder = moduleInstance.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( val1 );
        valueBuilder.prototypeFor( AnotherValueInternalState.class ).val2().set( val2 );
        return valueBuilder.newInstance();
    }

    private BarEntity buildBarEntity( String cathedral )
    {
        EntityBuilder<BarEntity> barBuilder = unitOfWorkFactory.currentUnitOfWork().newEntityBuilder( BarEntity.class );
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

        Property<LocalTime> localTime();

        Property<OffsetDateTime> dateTime();

        Property<LocalDate> localDate();

        Property<LocalDateTime> localDateTime();

        Property<EntityReference> entityReference();

        @UseDefaults
        Property<List<String>> stringList();

        @UseDefaults
        Property<Map<String, Integer>> stringIntMap();

        @UseDefaults
        Property<Map<String, AnotherValue>> stringValueMap();

        Property<AnotherValue> another();

        // Property<AnotherValue[]> arrayOfValues();

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

        NamedAssociation<Bar> barNamedAssociationEmpty();

        NamedAssociation<Bar> barNamedAssociation();

        NamedAssociation<BarEntity> barEntityNamedAssociation();
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


