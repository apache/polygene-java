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
package org.apache.polygene.runtime.value;

import org.junit.Test;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.runtime.property.PropertyEqualityTest.AnotherSome;
import org.apache.polygene.runtime.property.PropertyEqualityTest.Other;
import org.apache.polygene.runtime.property.PropertyEqualityTest.PrimitivesValue;
import org.apache.polygene.runtime.property.PropertyEqualityTest.Some;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildAnotherSomeValue;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildAnotherSomeValueWithDifferentState;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildOtherValue;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildPrimitivesValue;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildSomeValue;
import static org.apache.polygene.runtime.property.PropertyEqualityTest.buildSomeValueWithDifferentState;

/**
 * Assert that Value equals/hashcode methods combine ValueDescriptor and ValueState.
 */
public class ValueEqualityTest
    extends AbstractPolygeneTest
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
        Some some = buildSomeValue(valueBuilderFactory);
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        Some other = buildSomeValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        PrimitivesValue primitive = buildPrimitivesValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        ValueDescriptor someDescriptor = zest.api().valueDescriptorFor( some );

        Other other = buildOtherValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        Some some2 = buildSomeValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        Some some2 = buildSomeValueWithDifferentState( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        AnotherSome anotherSome = buildAnotherSomeValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        AssociationStateHolder someState = zest.spi().stateOf( (ValueComposite) some );

        AnotherSome anotherSome = buildAnotherSomeValueWithDifferentState( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        Some some2 = buildSomeValue( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        Some some2 = buildSomeValueWithDifferentState( valueBuilderFactory );
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
        Some some = buildSomeValue( valueBuilderFactory );
        Some anotherSome = buildAnotherSomeValue( valueBuilderFactory );

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
        Some some = buildSomeValue( valueBuilderFactory );
        Some anotherSome = buildAnotherSomeValueWithDifferentState( valueBuilderFactory );
        assertThat( "Values not equal",
                    some,
                    not( equalTo( anotherSome ) ) );
        assertThat( "Values hashcode not equal",
                    some.hashCode(),
                    not( equalTo( anotherSome.hashCode() ) ) );
    }
}
