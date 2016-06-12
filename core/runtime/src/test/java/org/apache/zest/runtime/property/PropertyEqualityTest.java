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
package org.apache.zest.runtime.property;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Test;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Assert that Property equals/hashcode methods combine PropertyDescriptor and State.
 */
public class PropertyEqualityTest
    extends AbstractZestTest
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

    private enum AnEnum
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

        Property<Instant> instantProperty();

        Property<ZonedDateTime> dateTimeProperty();

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
        Some some = buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        Some other = buildSomeValue( valueBuilderFactory );
        ValueDescriptor otherDescriptor = zest.api().valueDescriptorFor( other );
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
        Some some = buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        PrimitivesValue primitive = buildPrimitivesValue( valueBuilderFactory );
        ValueDescriptor primitiveDescriptor = zest.api().valueDescriptorFor( primitive );
        PropertyDescriptor primitiveCharPropDesc = primitiveDescriptor.state()
            .findPropertyModelByName( "characterProperty" );

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
        Some some = buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );
        PropertyDescriptor someCharPropDesc = someDescriptor.state().findPropertyModelByName( "characterProperty" );

        Other other = buildOtherValue( valueBuilderFactory );
        ValueDescriptor otherDescriptor = zest.api().valueDescriptorFor( other );
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
        PrimitivesValue primitives = buildPrimitivesValue( valueBuilderFactory );
        Some some = buildSomeValue( valueBuilderFactory );
        Some some2 = buildSomeValue( valueBuilderFactory );
        Other other = buildOtherValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        Some some2 = buildSomeValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        Some some2 = buildSomeValueWithDifferentState( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        PrimitivesValue primitive = buildPrimitivesValue( valueBuilderFactory );
        assertThat( "Property equal",
                    some.characterProperty(),
                    equalTo( primitive.characterProperty() ) );
    }

    @Test
    public void givenValuesOfCommonTypesWithDifferentStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( valueBuilderFactory );
        PrimitivesValue primitive = buildPrimitivesValueWithDifferentState( valueBuilderFactory );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( primitive.characterProperty() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( valueBuilderFactory );
        Other other = buildOtherValue( valueBuilderFactory );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( other.characterProperty() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesWithDifferentStateWhenTestingPropertyEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( valueBuilderFactory );
        Other other = buildOtherValue( valueBuilderFactory );
        assertThat( "Property not equal",
                    some.characterProperty(),
                    not( equalTo( other.characterProperty() ) ) );
    }

    //
    // -----------------------------------:: Values factory methods ::--------------------------------------------------
    //
    public static PrimitivesValue buildPrimitivesValue( ValueBuilderFactory vbf )
    {
        PrimitivesValue primitive;
        {
            ValueBuilder<PrimitivesValue> builder = vbf.newValueBuilder( PrimitivesValue.class );
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

    private static PrimitivesValue buildPrimitivesValueWithDifferentState( ValueBuilderFactory vbf )
    {
        PrimitivesValue primitive;
        {
            ValueBuilder<PrimitivesValue> builder = vbf.newValueBuilder( PrimitivesValue.class );
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

    public static Some buildSomeValue( ValueBuilderFactory vbf )
    {
        Some some;
        {
            ZonedDateTime refDate = ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 0, UTC );

            ValueBuilder<Some> builder = vbf.newValueBuilder( Some.class );
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
            builder.prototype().instantProperty().set( refDate.toInstant() );
            builder.prototype().dateTimeProperty().set( refDate );
            builder.prototype().localDateProperty().set( LocalDate.of( 2020, 3, 4 ) );
            builder.prototype().localDateTimeProperty().set( LocalDateTime.of( 2020, 3, 4, 13, 23, 0 ) );
            some = builder.newInstance();
        }
        return some;
    }

    public static Some buildSomeValueWithDifferentState( ValueBuilderFactory vbf )
    {
        Some some;
        {
            ZonedDateTime refDate = ZonedDateTime.of( 2030, 2, 8, 9, 9, 9, 0, UTC );
            ValueBuilder<Some> builder = vbf.newValueBuilder( Some.class );
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
            builder.prototype().instantProperty().set( refDate.toInstant() );
            builder.prototype().dateTimeProperty().set( refDate );
            builder.prototype().localDateProperty().set( LocalDate.of( 2030, 2, 8 ) );
            builder.prototype().localDateTimeProperty().set( LocalDateTime.of( 2030, 2, 8, 9, 9, 9 ) );
            some = builder.newInstance();
        }
        return some;
    }

    public static AnotherSome buildAnotherSomeValue( ValueBuilderFactory vbf )
    {
        AnotherSome anotherSome;
        {
            ZonedDateTime refDate = ZonedDateTime.of( 2020, 3, 4, 13, 24, 35, 0, UTC );
            ValueBuilder<AnotherSome> builder = vbf.newValueBuilder( AnotherSome.class );
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
            builder.prototype().instantProperty().set( refDate.toInstant() );
            builder.prototype().dateTimeProperty().set( refDate );
            builder.prototype().localDateProperty().set( refDate.toLocalDate() );
            builder.prototype().localDateTimeProperty().set( LocalDateTime.of( 2020, 3, 4, 13, 23, 0 ) );
            anotherSome = builder.newInstance();
        }
        return anotherSome;
    }

    public static AnotherSome buildAnotherSomeValueWithDifferentState( ValueBuilderFactory vbf )
    {
        AnotherSome anotherSome;
        {
            ValueBuilder<AnotherSome> builder = vbf.newValueBuilder( AnotherSome.class );
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
            ZonedDateTime refDate = ZonedDateTime.of( 2030, 2, 8, 9, 9, 9, 0, UTC );
            builder.prototype().instantProperty().set( refDate.toInstant() );
            builder.prototype().dateTimeProperty().set( refDate );
            builder.prototype().localDateProperty().set( LocalDate.of( 2030, 2, 8 ) );
            builder.prototype().localDateTimeProperty().set( LocalDateTime.of( 2030, 2, 8, 9, 9, 9 ) );
            anotherSome = builder.newInstance();
        }
        return anotherSome;
    }

    public static Other buildOtherValue( ValueBuilderFactory vbf )
    {
        Other other;
        {
            ValueBuilder<Other> builder = vbf.newValueBuilder( Other.class );
            builder.prototype().characterProperty().set( 'q' );
            other = builder.newInstance();
        }
        return other;
    }
}
