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
package org.apache.zest.runtime.value;

import org.junit.Test;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.runtime.property.PropertyEqualityTest.AnotherSome;
import org.apache.zest.runtime.property.PropertyEqualityTest.Other;
import org.apache.zest.runtime.property.PropertyEqualityTest.PrimitivesValue;
import org.apache.zest.runtime.property.PropertyEqualityTest.Some;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildAnotherSomeValue;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildAnotherSomeValueWithDifferentState;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildOtherValue;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildPrimitivesValue;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildSomeValue;
import static org.apache.zest.runtime.property.PropertyEqualityTest.buildSomeValueWithDifferentState;

/**
 * Assert that Value equals/hashcode methods combine ValueDescriptor and ValueState.
 */
public class ValueEqualityTest
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

    //
    // -------------------------------:: ValueDescriptor equality tests ::----------------------------------------------
    //
    @Test
    public void givenValuesOfTheSameTypeWhenTestingValueDescriptorEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        Some other = buildSomeValue( module );
        ValueDescriptor otherDescriptor = zest.api().valueDescriptorFor( other );

        assertThat( "ValueDescriptors equal",
                    someDescriptor,
                    equalTo( otherDescriptor ) );
        assertThat( "ValueDescriptors hashcode equal",
                    someDescriptor.hashCode(),
                    equalTo( otherDescriptor.hashCode() ) );
    }

    @Test
    public void givenValuesOfCommonTypesWhenTestingValueDescriptorEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        PrimitivesValue primitive = buildPrimitivesValue( module );
        ValueDescriptor primitiveDescriptor = zest.api().valueDescriptorFor( primitive );

        assertThat( "ValueDescriptors not equal",
                    someDescriptor,
                    not( equalTo( primitiveDescriptor ) ) );
        assertThat( "ValueDescriptors hashcode not equal",
                    someDescriptor.hashCode(),
                    not( equalTo( primitiveDescriptor.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesWhenTestingValueDescriptorEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        Other other = buildOtherValue( module );
        ValueDescriptor otherDescriptor = zest.api().valueDescriptorFor( other );

        assertThat( "ValueDescriptors not equal",
                    someDescriptor,
                    not( equalTo( otherDescriptor ) ) );
        assertThat( "ValueDescriptors hashcode not equal",
                    someDescriptor.hashCode(),
                    not( equalTo( otherDescriptor.hashCode() ) ) );
    }

    //
    // ---------------------------------:: Value State equality tests ::------------------------------------------------
    //
    @Test
    public void givenValuesOfSameTypesAndSameStateWhenTestingValueStateEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        Some some2 = buildSomeValue( module );
        AssociationStateHolder some2State = zest.spi().stateOf( (ValueComposite) some2 );

        assertThat( "ValueStates equal",
                    someState,
                    equalTo( some2State ) );
        assertThat( "ValueStates hashcode equal",
                    someState.hashCode(),
                    equalTo( some2State.hashCode() ) );
    }

    @Test
    public void givenValuesOfSameTypesAndDifferentStateWhenTestingValueStateEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        Some some2 = buildSomeValueWithDifferentState( module );
        AssociationStateHolder some2State = zest.spi().stateOf( (ValueComposite) some2 );

        assertThat( "ValueStates not equal",
                    someState,
                    not( equalTo( some2State ) ) );
        assertThat( "ValueStates hashcode not equal",
                    someState.hashCode(),
                    not( equalTo( some2State.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingValueStateEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        AnotherSome anotherSome = buildAnotherSomeValue( module );
        AssociationStateHolder anotherSomeState = zest.spi().stateOf( (ValueComposite) anotherSome );

        assertThat( "ValueStates equal",
                    someState,
                    equalTo( anotherSomeState ) );
        assertThat( "ValueStates hashcode equal",
                    someState.hashCode(),
                    equalTo( anotherSomeState.hashCode() ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingValueStateEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        AnotherSome anotherSome = buildAnotherSomeValueWithDifferentState( module );
        AssociationStateHolder anotherSomeState = zest.spi().stateOf( (ValueComposite) anotherSome );

        assertThat( "ValueStates not equal",
                    someState,
                    not( equalTo( anotherSomeState ) ) );
        assertThat( "ValueStates hashcode not equal",
                    someState.hashCode(),
                    not( equalTo( anotherSomeState.hashCode() ) ) );
    }

    //
    // ------------------------------------:: Value equality tests ::---------------------------------------------------
    //
    @Test
    public void givenValuesOfSameTypesAndSameStateWhenTestingValueEqualityExpectEquals()
    {
        Some some = buildSomeValue( module );
        Some some2 = buildSomeValue( module );
        assertThat( "Values equal",
                    some,
                    equalTo( some2 ) );
        assertThat( "Values hashcode equal",
                    some.hashCode(),
                    equalTo( some2.hashCode() ) );
    }

    @Test
    public void givenValuesOfTheSameTypeWithDifferentStateWhenTestingValueEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Some some2 = buildSomeValueWithDifferentState( module );
        assertThat( "Values not equals",
                    some,
                    not( equalTo( some2 ) ) );
        assertThat( "Values hashcode not equals",
                    some.hashCode(),
                    not( equalTo( some2.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndSameStateWhenTestingValueEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Some anotherSome = buildAnotherSomeValue( module );

        assertThat( "Values not equal",
                    some,
                    not( equalTo( anotherSome ) ) );
        assertThat( "Values hashcode not equal",
                    some.hashCode(),
                    not( equalTo( anotherSome.hashCode() ) ) );
    }

    @Test
    public void givenValuesOfDifferentTypesAndDifferentStateWhenTestingValueEqualityExpectNotEquals()
    {
        Some some = buildSomeValue( module );
        Some anotherSome = buildAnotherSomeValueWithDifferentState( module );
        assertThat( "Values not equal",
                    some,
                    not( equalTo( anotherSome ) ) );
        assertThat( "Values hashcode not equal",
                    some.hashCode(),
                    not( equalTo( anotherSome.hashCode() ) ) );
    }
}
