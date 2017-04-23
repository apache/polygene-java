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
package org.apache.polygene.test.serialization;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.ConvertedBy;
import org.apache.polygene.api.serialization.JavaSerializationConverter;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.polygene.api.usecase.UsecaseBuilder.newUsecase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Assert that Serialization behaviour on ValueComposites is correct.
 */
// TODO Assert Generics behaviour!
public abstract class AbstractValueCompositeSerializationTest
    extends AbstractPolygeneTest
{
    @Rule
    public TestName testName = new TestName();

    @Before
    public void before()
    {
        System.out.println( "# BEGIN " + testName.getMethodName() );
    }

    @After
    public void after()
    {
        System.out.println( "# END " + testName.getMethodName() );
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.objects( JavaSerializationConverter.class );
        module.values( Some.class, SomeExtended.class, SomeShuffled.class,
                       AnotherValue.class, FooValue.class, CustomFooValue.class,
                       SpecificCollection.class /*, SpecificValue.class, GenericValue.class */ );
        module.entities( Some.class, BarEntity.class );

        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module.layer().module( "persistence" ) );
    }

    @Structure
    protected Module moduleInstance;

    @Service
    protected Serialization serialization;

    @Test
    public void givenValueCompositeWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some some = buildSomeValue( moduleInstance, uow, "23" );

            // Serialize using injected service
            String stateString = serialization.serialize( some );
            System.out.println( stateString );

            // Deserialize using Module API
            Some some2 = moduleInstance.newValueFromSerializedState( Some.class, stateString );

            assertThat( "Map<String, Integer>",
                        some2.stringIntMap().get().get( "foo" ),
                        equalTo( 42 ) );
            assertThat( "Map<String, Value>",
                        some2.stringValueMap().get().get( "foo" ).internalVal(),
                        equalTo( "Bar" ) );

            assertThat( "Nested Entities",
                        some2.barAssociation().get().cathedral().get(),
                        equalTo( "bazar in barAssociation" ) );

            assertThat( "Polymorphic deserialization of value type NOT extending ValueComposite",
                        some.customFoo().get() instanceof CustomFooValue,
                        is( true ) );
            assertThat( "Polymorphic deserialization of value type extending ValueComposite",
                        some.customFooValue().get() instanceof CustomFooValue,
                        is( true ) );

            assertThat( "Value equality", some, equalTo( some2 ) );

            uow.complete();
        }
    }

    @Test
    public void givenEntityCompositeWhenSerializingAndDeserializingExpectEquals()
        throws Exception
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some some = buildSomeEntity( moduleInstance, uow, "23" );

            // Serialize using injected service
            String stateString = serialization.serialize( some );
            System.out.println( stateString );

            // Deserialize using Module API
            Some some2 = moduleInstance.newValueFromSerializedState( Some.class, stateString );

            assertThat( "Map<String, Integer>",
                        some2.stringIntMap().get().get( "foo" ),
                        equalTo( 42 ) );
            assertThat( "Map<String, Value>",
                        some2.stringValueMap().get().get( "foo" ).internalVal(),
                        equalTo( "Bar" ) );

            assertThat( "Nested Entities",
                        some2.barAssociation().get().cathedral().get(),
                        equalTo( "bazar in barAssociation" ) );

            assertThat( "Polymorphic deserialization of value type NOT extending ValueComposite",
                        some.customFoo().get() instanceof CustomFooValue,
                        is( true ) );
            assertThat( "Polymorphic deserialization of value type extending ValueComposite",
                        some.customFooValue().get() instanceof CustomFooValue,
                        is( true ) );

            assertThat( "Value equality", some, equalTo( some2 ) );

            uow.complete();
        }
    }

    @Test
    public void canDeserializeUsingSuperTypeWithLessState()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            SomeExtended someExtended = buildSomeExtendedValue( moduleInstance, uow, "42" );

            String serialized = serialization.serialize( someExtended );
            System.out.println( serialized );

            Some deserialized = serialization.deserialize( module, Some.class, serialized );
            System.out.println( deserialized );

            uow.complete();
        }
    }

    @Test
    public void canDeserializeUsingChildTypeWithSupplementaryOptionalState()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some some = buildSomeValue( moduleInstance, uow, "42" );

            String serialized = serialization.serialize( some );
            System.out.println( serialized );

            SomeExtended deserialized = serialization.deserialize( module, SomeExtended.class, serialized );
            System.out.println( deserialized );

            uow.complete();
        }
    }

    /**
     * State model order depend on declaration order, this test ensures that moving a state method up/down into a type
     * does not break deserialization.
     */
    @Test
    public void canDeserializeFromShuffledState()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            SomeExtended someExtended = buildSomeExtendedValue( moduleInstance, uow, "42" );

            String serialized = serialization.serialize( someExtended );
            System.out.println( serialized );

            SomeShuffled deserialized = serialization.deserialize( module, SomeShuffled.class, serialized );
            System.out.println( deserialized );

            serialized = serialization.serialize( deserialized );
            System.out.println( serialized );

            serialization.deserialize( module, SomeExtended.class, serialized );
            System.out.println( deserialized );

            uow.complete();
        }
    }

    @Test
    @Ignore( "JSONEntityState cannot handle polymorphic deserialization" )
    // TODO Entity == Identity + Value
    // JSONEntityState does not allow for polymorphic serialization
    public void valueAndEntityTypeEquality()
    {
        Identity identity = StringIdentity.fromString( "42" );
        Some createdValue, loadedValue;

        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "create" ) ) )
        {
            Some entity = buildSomeEntity( moduleInstance, uow, identity );
            createdValue = uow.toValue( Some.class, entity );
            System.out.println( "Created Entity\n\t" + entity + "\nCreated Value\n\t" + createdValue );
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "load" ) ) )
        {
            Some entity = uow.get( Some.class, identity );
            loadedValue = uow.toValue( Some.class, entity );
            System.out.println( "Loaded Entity\n\t" + entity + "\nLoaded Value\n\t" + loadedValue );
        }

        assertThat( "Create/Read equality",
                    createdValue, equalTo( loadedValue ) );

        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "remove" ) ) )
        {
            uow.remove( uow.get( Some.class, identity ) );
            uow.complete();
        }

        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "create from value" ) ) )
        {
            Some entity = uow.toEntity( Some.class, loadedValue );
            createdValue = uow.toValue( Some.class, entity );
            System.out.println( "Created Entity from Value\n\t" + entity + "\nCreated Value\n\t" + createdValue );
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "read again" ) ) )
        {
            Some entity = uow.get( Some.class, identity );
            loadedValue = uow.toValue( Some.class, entity );
            System.out.println( "Loaded Entity\n\t" + entity + "\nLoaded Value\n\t" + loadedValue );
        }

        assertThat( "Create from Value/Read equality",
                    createdValue, equalTo( loadedValue ) );
    }

    protected static Some buildSomeEntity( Module module, UnitOfWork uow, String identity )
    {
        EntityBuilder<Some> builder = uow.newEntityBuilder( Some.class );
        Some proto = builder.instance();
        proto.identity().set( StringIdentity.fromString( identity ) );
        setSomeValueState( module, uow, proto );
        return builder.newInstance();
    }

    /**
     * @return a Some ValueComposite whose state is populated with test data.
     */
    protected static Some buildSomeValue( Module module, UnitOfWork uow, String identity )
    {
        ValueBuilder<Some> builder = module.newValueBuilder( Some.class );
        Some proto = builder.prototype();
        proto.identity().set( StringIdentity.fromString( identity ) );
        setSomeValueState( module, uow, proto );
        return builder.newInstance();
    }

    protected static SomeExtended buildSomeExtendedValue( Module module, UnitOfWork uow, String identity )
    {
        ValueBuilder<SomeExtended> builder = module.newValueBuilder( SomeExtended.class );
        SomeExtended proto = builder.prototype();
        proto.identity().set( StringIdentity.fromString( identity ) );
        setSomeValueState( module, uow, proto );
        proto.extraProperty().set( "extra property" );
        proto.extraAssociation().set( buildBarEntity( module, uow, "extra association" ) );
        proto.extraManyAssociation().add( buildBarEntity( module, uow, "extra many association" ) );
        proto.extraNamedAssociation().put( "extra", buildBarEntity( module, uow, "extra named association" ) );
        return builder.newInstance();
    }

    /**
     * @return a Some EntityComposite whose state is populated with test data.
     */
    protected static Some buildSomeEntity( Module module, UnitOfWork uow, Identity identity )
    {
        EntityBuilder<Some> builder = uow.newEntityBuilder( Some.class, identity );
        setSomeValueState( module, uow, builder.instance() );
        return builder.newInstance();
    }

    private static void setSomeValueState( Module module, UnitOfWork uow, Some some )
    {
        some.anotherList().get().add( module.newValue( AnotherValue.class ) );

        ValueBuilder<SpecificCollection> specificColBuilder = module.newValueBuilder( SpecificCollection.class );
        SpecificCollection specificColProto = specificColBuilder.prototype();
        List<String> genericList = new ArrayList<>( 2 );
        genericList.add( "Some" );
        genericList.add( "String" );
        specificColProto.genericList().set( genericList );
        some.specificCollection().set( specificColBuilder.newInstance() );

        AnotherValue anotherValue1 = createAnotherValue( module, "Foo", "Bar" );
        AnotherValue anotherValue2 = createAnotherValue( module, "Habba", "ZoutZout" );
        AnotherValue anotherValue3 = createAnotherValue( module, "Niclas", "Hedhman" );

        some.string().set( "Foo\"Bar\"\nTest\f\t\b\r" );
        some.string2().set( "/Foo/bar" );
        some.number().set( 43L );
        some.localTime().set( LocalTime.now() );
        some.dateTime().set( OffsetDateTime.of( 2020, 3, 4, 13, 24, 35, 0, ZoneOffset.ofHours( 1 ) ) );
        some.localDate().set( LocalDate.now() );
        some.localDateTime().set( LocalDateTime.now() );
        some.entityReference().set( EntityReference.parseEntityReference( "12345" ) );
        some.stringIntMap().get().put( "foo", 42 );

        // Can't put more than one entry in Map because this test rely on the fact that the underlying implementations
        // maintain a certain order but it's not the case on some JVMs. On OpenJDK 8 they are reversed for example.
        // This should not be enforced tough as both the Map API and the JSON specification state that name-value pairs
        // are unordered.
        // As a consequence this test should be enhanced to be Map order independent.
        //
        // proto.stringIntMap().get().put( "bar", 67 );

        some.stringValueMap().get().put( "foo", anotherValue1 );
        some.another().set( anotherValue1 );
        some.arrayOfValues().set( new AnotherValue[] { anotherValue1, anotherValue2, anotherValue3 } );
        some.primitiveByteArray().set( "foo".getBytes( UTF_8 ) );
        some.byteArray().set( new Byte[] { 23, null, 42 } );
        some.serializable().set( new SerializableObject() );
        some.foo().set( module.newValue( FooValue.class ) );
        some.fooValue().set( module.newValue( FooValue.class ) );
        some.customFoo().set( module.newValue( CustomFooValue.class ) );
        some.customFooValue().set( module.newValue( CustomFooValue.class ) );

        // NestedEntities
        some.barAssociation().set( buildBarEntity( module, uow, "bazar in barAssociation" ) );
        some.barEntityAssociation().set( buildBarEntity( module, uow, "bazar in barEntityAssociation" ) );
        some.barManyAssociation().add( buildBarEntity( module, uow, "bazar ONE in barManyAssociation" ) );
        some.barManyAssociation().add( buildBarEntity( module, uow, "bazar TWO in barManyAssociation" ) );
        some.barEntityManyAssociation().add( buildBarEntity( module, uow, "bazar ONE in barEntityManyAssociation" ) );
        some.barEntityManyAssociation().add( buildBarEntity( module, uow, "bazar TWO in barEntityManyAssociation" ) );
        some.barNamedAssociation().put( "bazar", buildBarEntity( module, uow, "bazar in barNamedAssociation" ) );
        some.barNamedAssociation().put( "cathedral",
                                        buildBarEntity( module, uow, "cathedral in barNamedAssociation" ) );
        some.barEntityNamedAssociation().put( "bazar",
                                              buildBarEntity( module, uow, "bazar in barEntityNamedAssociation" ) );
        some.barEntityNamedAssociation().put( "cathedral",
                                              buildBarEntity( module, uow, "cathedral in barEntityNamedAssociation" ) );
    }

    private static AnotherValue createAnotherValue( Module module, String val1, String val2 )
    {
        ValueBuilder<AnotherValue> valueBuilder = module.newValueBuilder( AnotherValue.class );
        valueBuilder.prototype().val1().set( val1 );
        valueBuilder.prototypeFor( AnotherValueInternalState.class ).val2().set( val2 );
        return valueBuilder.newInstance();
    }

    private static BarEntity buildBarEntity( Module module, UnitOfWork uow, String cathedral )
    {
        EntityBuilder<BarEntity> barBuilder = uow.newEntityBuilder( BarEntity.class );
        barBuilder.instance().cathedral().set( cathedral );
        barBuilder.instance().another().set( createAnotherValue( module, "nested", "value" ) );
        return barBuilder.newInstance();
    }

    public enum TestEnum
    {
        somevalue,
        anothervalue
    }

    public interface Some extends HasIdentity
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

        Property<AnotherValue[]> arrayOfValues();

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

        Property<byte[]> primitiveByteArray();

        @Optional
        Property<byte[]> primitiveByteArrayNull();

        Property<Byte[]> byteArray();

        @Optional
        Property<Byte[]> byteArrayNull();

        Property<SerializableObject> serializable();

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

    interface SomeExtended extends Some
    {
        @Optional
        Property<String> extraProperty();

        @Optional
        Association<Bar> extraAssociation();

        ManyAssociation<Bar> extraManyAssociation();

        NamedAssociation<Bar> extraNamedAssociation();
    }

    interface SomeShuffled extends SomeExtended
    {
        NamedAssociation<Bar> extraNamedAssociation();

        @Override
        NamedAssociation<Bar> barNamedAssociation();

        ManyAssociation<Bar> extraManyAssociation();

        @Override
        ManyAssociation<Bar> barManyAssociation();

        Association<Bar> extraAssociation();

        @Override
        Association<Bar> barAssociation();

        Property<String> extraProperty();

        @Override
        Property<String> string();
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

        Property<AnotherValue> another();
    }

    public interface BarEntity
        extends Bar, EntityComposite
    {
    }

    @ConvertedBy( JavaSerializationConverter.class )
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


