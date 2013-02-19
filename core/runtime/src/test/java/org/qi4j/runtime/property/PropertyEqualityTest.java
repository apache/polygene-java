/*
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
 *
 */
package org.qi4j.runtime.property;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertThat;

/**
 * Assert that Property equals/hashcode methods combine PropertyDescriptor and State.
 */
public class PropertyEqualityTest
    extends AbstractQi4jTest
{

    //
    // --------------------------------------:: Types under test ::-----------------------------------------------------
    //
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( PrimitivesValue.class, Some.class, AnotherSome.class, Other.class );
    }

    public enum AnEnum
    {

        BAZAR, CATHEDRAL
    }

    public interface PrimitivesValue
    {

        Property<Character> characterProperty();

        Property<String> stringProperty();

        Property<Boolean> booleanProperty();

        Property<Integer> integerProperty();

        Property<Long> longProperty();

        Property<Float> floatProperty();

        Property<Double> doubleProperty();

        Property<Short> shortProperty();

        Property<Byte> byteProperty();

        Property<AnEnum> enumProperty();
    }

    public interface Some
        extends PrimitivesValue
    {

        @Optional
        Property<Some> selfProperty();

        Property<BigInteger> bigIntegerProperty();

        Property<BigDecimal> bigDecimalProperty();

        Property<Date> dateProperty();

        Property<DateTime> dateTimeProperty();

        Property<LocalDate> localDateProperty();

        Property<LocalDateTime> localDateTimeProperty();
    }

    public interface AnotherSome
        extends Some
    {
    }

    public interface Other
    {

        Property<Character> characterProperty();
    }

    //
    // ------------------------------:: PropertyDescriptor equality tests ::--------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeWhenTestingPropertyDescriptorEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        Some other = buildSomeValue( module );
        ValueDescriptor otherDescriptor = qi4j.api().valueDescriptorFor( other );
        PropertyDescriptor otherCharPropDesc = otherDescriptor.state().findPropertyModelByName( "characterProperty" );

        assertThat( "PropertyDescriptors equal",
                    someCharPropDesc,
                    equalTo( otherCharPropDesc ) );
        assertThat( "PropertyDescriptors hashcode equal",
                    someCharPropDesc.hashCode(),
                    equalTo( otherCharPropDesc.hashCode() ) );
    }

    @Test
    public void givenValuesOfCommonTypesWhenTestingPropertyDescriptorEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        PrimitivesValue primitive = buildPrimitivesValue( module );
        ValueDescriptor primitiveDescriptor = qi4j.api().valueDescriptorFor( primitive );
        PropertyDescriptor primitiveCharPropDesc = primitiveDescriptor.state().findPropertyModelByName( "characterProperty" );

        assertThat( "PropertyDescriptors equal",
                    someCharPropDesc,
                    equalTo( primitiveCharPropDesc ) );
        assertThat( "PropertyDescriptors hashcode equal",
                    someCharPropDesc.hashCode(),
                    equalTo( primitiveCharPropDesc.hashCode() ) );
    }

    @Test
    public void givenValuesOfDifferentTypesWhenTestingPropertyDescriptorEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = qi4j.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        Other other = buildOtherValue( module );
        ValueDescriptor otherDescriptor = qi4j.api().valueDescriptorFor( other );
        PropertyDescriptor otherCharPropDesc = otherDescriptor.state().findPropertyModelByName( "characterProperty" );

        assertThat( "PropertyDescriptors not equal",
                    someCharPropDesc,
                    not( equalTo( otherCharPropDesc ) ) );
        assertThat( "PropertyDescriptors hashcode not equal",
                    someCharPropDesc.hashCode(),
                    not( equalTo( otherCharPropDesc.hashCode() ) ) );
    }

    //
    // --------------------------------:: Property State equality tests ::----------------------------------------------
    //
    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingPropertyStateEqualityExpectEquals()
    {
        PrimitivesValue primitives = buildPrimitivesValue( module );
        Some some = buildSomeValue( module );
        Some some2 = buildSomeValue( module );
        Other other = buildOtherValue( module );
        assertThat( "Property state equal",
                    'q',
                    allOf( equalTo( primitives.characterProperty().get() ),
                           equalTo( some.characterProperty().get() ),
                           equalTo( some2.characterProperty().get() ),
                           equalTo( other.characterProperty().get() ) ) );
        assertThat( "Property state hashcode equal",
                    new Character( 'q' ).hashCode(),
                    allOf( equalTo( primitives.characterProperty().get().hashCode() ),
                           equalTo( some.characterProperty().get().hashCode() ),
                           equalTo( some2.characterProperty().get().hashCode() ),
                           equalTo( other.characterProperty().get().hashCode() ) ) );
    }

    //
    // -----------------------------------:: Property equality tests ::-------------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeAndSameStateWhenTestingPropertyEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        Some some2 = buildSomeValue( module );
        assertThat( "Property equals",
                    some.characterProperty(),
                    equalTo( some2.characterProperty() ) );
        assertThat( "Property hashcode equals",
                    some.characterProperty().hashCode(),
                    equalTo( some2.characterProperty().hashCode() ) );
    }

    @Test
    public void givenValuesOfTheSameTypeWithDifferentStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Some some2 = buildSomeValueWithDifferentState( module );
        assertThat( "Property not equals",
                    some.characterProperty(),
                    not( equalTo( some2.characterProperty() ) ) );
        assertThat( "Property hashcode not equals",
                    some.characterProperty().hashCode(),
                    not( equalTo( some2.characterProperty().hashCode() ) ) );
    }

    @Test
    public void givenValuesOfCommonTypesAndSameStateWhenTestingPropertyEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        PrimitivesValue primitive = buildPrimitivesValue( module );
        assertThat( "Property equal",
                    some.characterProperty(),
                    equalTo( primitive.characterProperty() ) );
    }

    @Test
    public void givenValuesOfCommonTypesWithDifferentStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        PrimitivesValue primitive = buildPrimitivesValueWithDifferentState( module );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( primitive.characterProperty() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Other other = buildOtherValue( module );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( other.characterProperty() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesWithDifferentStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Other other = buildOtherValue( module );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( other.characterProperty() ) ) );
    }

    //
    // -----------------------------------:: Values factory methods ::--------------------------------------------------
    //
    public static PrimitivesValue buildPrimitivesValue( Module module )
    {
        PrimitivesValue primitive;
        {
            ValueBuilder<PrimitivesValue> builder = module.newValueBuilder( PrimitivesValue.class );
            builder.prototype().characterProperty().set( 'q' );
            builder.prototype().stringProperty().set( "foo" );
            builder.prototype().booleanProperty().set( true );
            builder.prototype().integerProperty().set( 42 );
            builder.prototype().longProperty().set( 42L );
            builder.prototype().floatProperty().set( 42.23F );
            builder.prototype().doubleProperty().set( 42.23D );
            builder.prototype().shortProperty().set( (short) 42 );
            builder.prototype().byteProperty().set( (byte) 42 );
            builder.prototype().enumProperty().set( AnEnum.BAZAR );
            primitive = builder.newInstance();
        }
        return primitive;
    }

    public static PrimitivesValue buildPrimitivesValueWithDifferentState( Module module )
    {
        PrimitivesValue primitive;
        {
            ValueBuilder<PrimitivesValue> builder = module.newValueBuilder( PrimitivesValue.class );
            builder.prototype().characterProperty().set( 'i' );
            builder.prototype().stringProperty().set( "bar" );
            builder.prototype().booleanProperty().set( false );
            builder.prototype().integerProperty().set( 23 );
            builder.prototype().longProperty().set( 23L );
            builder.prototype().floatProperty().set( 23.42F );
            builder.prototype().doubleProperty().set( 23.42D );
            builder.prototype().shortProperty().set( (short) 23 );
            builder.prototype().byteProperty().set( (byte) 23 );
            builder.prototype().enumProperty().set( AnEnum.CATHEDRAL );
            primitive = builder.newInstance();
        }
        return primitive;
    }

    public static Some buildSomeValue( Module module )
    {
        Some some;
        {
            ValueBuilder<Some> builder = module.newValueBuilder( Some.class );
            builder.prototype().characterProperty().set( 'q' );
            builder.prototype().stringProperty().set( "foo" );
            builder.prototype().booleanProperty().set( true );
            builder.prototype().integerProperty().set( 42 );
            builder.prototype().longProperty().set( 42L );
            builder.prototype().floatProperty().set( 42.23F );
            builder.prototype().doubleProperty().set( 42.23D );
            builder.prototype().shortProperty().set( (short) 42 );
            builder.prototype().byteProperty().set( (byte) 42 );
            builder.prototype().enumProperty().set( AnEnum.BAZAR );
            builder.prototype().bigIntegerProperty().set( new BigInteger( "42" ) );
            builder.prototype().bigDecimalProperty().set( new BigDecimal( "42.23" ) );
            builder.prototype().dateProperty().set( new DateTime( "2020-03-04T13:24:35", UTC ).toDate() );
            builder.prototype().dateTimeProperty().set( new DateTime( "2020-03-04T13:24:35", UTC ) );
            builder.prototype().localDateProperty().set( new LocalDate( "2020-03-04" ) );
            builder.prototype().localDateTimeProperty().set( new LocalDateTime( "2020-03-04T13:23:00", UTC ) );
            some = builder.newInstance();
        }
        return some;
    }

    public static Some buildSomeValueWithDifferentState( Module module )
    {
        Some some;
        {
            ValueBuilder<Some> builder = module.newValueBuilder( Some.class );
            builder.prototype().characterProperty().set( 'i' );
            builder.prototype().stringProperty().set( "bar" );
            builder.prototype().booleanProperty().set( false );
            builder.prototype().integerProperty().set( 23 );
            builder.prototype().longProperty().set( 23L );
            builder.prototype().floatProperty().set( 23.42F );
            builder.prototype().doubleProperty().set( 23.42D );
            builder.prototype().shortProperty().set( (short) 23 );
            builder.prototype().byteProperty().set( (byte) 23 );
            builder.prototype().enumProperty().set( AnEnum.CATHEDRAL );
            builder.prototype().bigIntegerProperty().set( new BigInteger( "23" ) );
            builder.prototype().bigDecimalProperty().set( new BigDecimal( "23.42" ) );
            builder.prototype().dateProperty().set( new DateTime( "2030-02-08T09:09:09", UTC ).toDate() );
            builder.prototype().dateTimeProperty().set( new DateTime( "2030-02-08T09:09:09", UTC ) );
            builder.prototype().localDateProperty().set( new LocalDate( "2030-02-08" ) );
            builder.prototype().localDateTimeProperty().set( new LocalDateTime( "2030-02-08T09:09:09", UTC ) );
            some = builder.newInstance();
        }
        return some;
    }

    public static AnotherSome buildAnotherSomeValue( Module module )
    {
        AnotherSome anotherSome;
        {
            ValueBuilder<AnotherSome> builder = module.newValueBuilder( AnotherSome.class );
            builder.prototype().characterProperty().set( 'q' );
            builder.prototype().stringProperty().set( "foo" );
            builder.prototype().booleanProperty().set( true );
            builder.prototype().integerProperty().set( 42 );
            builder.prototype().longProperty().set( 42L );
            builder.prototype().floatProperty().set( 42.23F );
            builder.prototype().doubleProperty().set( 42.23D );
            builder.prototype().shortProperty().set( (short) 42 );
            builder.prototype().byteProperty().set( (byte) 42 );
            builder.prototype().enumProperty().set( AnEnum.BAZAR );
            builder.prototype().bigIntegerProperty().set( new BigInteger( "42" ) );
            builder.prototype().bigDecimalProperty().set( new BigDecimal( "42.23" ) );
            builder.prototype().dateProperty().set( new DateTime( "2020-03-04T13:24:35", UTC ).toDate() );
            builder.prototype().dateTimeProperty().set( new DateTime( "2020-03-04T13:24:35", UTC ) );
            builder.prototype().localDateProperty().set( new LocalDate( "2020-03-04" ) );
            builder.prototype().localDateTimeProperty().set( new LocalDateTime( "2020-03-04T13:23:00", UTC ) );
            anotherSome = builder.newInstance();
        }
        return anotherSome;
    }

    public static AnotherSome buildAnotherSomeValueWithDifferentState( Module module )
    {
        AnotherSome anotherSome;
        {
            ValueBuilder<AnotherSome> builder = module.newValueBuilder( AnotherSome.class );
            builder.prototype().characterProperty().set( 'i' );
            builder.prototype().stringProperty().set( "bar" );
            builder.prototype().booleanProperty().set( false );
            builder.prototype().integerProperty().set( 23 );
            builder.prototype().longProperty().set( 23L );
            builder.prototype().floatProperty().set( 23.42F );
            builder.prototype().doubleProperty().set( 23.42D );
            builder.prototype().shortProperty().set( (short) 23 );
            builder.prototype().byteProperty().set( (byte) 23 );
            builder.prototype().enumProperty().set( AnEnum.CATHEDRAL );
            builder.prototype().bigIntegerProperty().set( new BigInteger( "23" ) );
            builder.prototype().bigDecimalProperty().set( new BigDecimal( "23.42" ) );
            builder.prototype().dateProperty().set( new DateTime( "2030-02-08T09:09:09", UTC ).toDate() );
            builder.prototype().dateTimeProperty().set( new DateTime( "2030-02-08T09:09:09", UTC ) );
            builder.prototype().localDateProperty().set( new LocalDate( "2030-02-08" ) );
            builder.prototype().localDateTimeProperty().set( new LocalDateTime( "2030-02-08T09:09:09", UTC ) );
            anotherSome = builder.newInstance();
        }
        return anotherSome;
    }

    public static Other buildOtherValue( Module module )
    {
        Other other;
        {
            ValueBuilder<Other> builder = module.newValueBuilder( Other.class );
            builder.prototype().characterProperty().set( 'q' );
            other = builder.newInstance();
        }
        return other;
    }
}
