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

package org.apache.polygene.runtime.defaults;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * JAVADOC
 */
public class UseDefaultsTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.transients( TestComposite.class );
        module.forMixin( TestComposite.class ).declareDefaults().assemblyString().set( "habba" );
    }

    @Test
    public void givenPropertyWithUseDefaultsWhenInstantiatedThenPropertiesAreDefaulted()
    {
        TransientBuilder<TestComposite> builder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
        TestComposite testComposite = builder.newInstance();

        assertThat( "nullInt is null", testComposite.nullInt().get(), nullValue() );
        assertThat( "zeroInt is zero", testComposite.defaultInt().get(), equalTo( 0 ) );
        assertThat( "nullString is null", testComposite.nullString().get(), nullValue() );
        assertThat( "defaultString is empty string", testComposite.defaultString().get(), equalTo( "" ) );
        assertThat( "assemblyString is set string", testComposite.assemblyString().get(), equalTo( "habba" ) );

        assertThat( "nullPrimitiveArray is null", testComposite.nullPrimitiveArray().get(), nullValue() );
        assertTrue( "emptyPrimitiveArray is empty",
                    Arrays.equals( testComposite.emptyPrimitiveArray().get(), new int[ 0 ] ) );
        assertThat( "nullArray is null", testComposite.nullArray().get(), nullValue() );
        assertTrue( "emptyArray is empty array",
                    Arrays.equals( testComposite.emptyArray().get(), new Integer[ 0 ] ) );
    }

    @Test
    public void givenPropertyWithValuedUseDefaultsWhenInstantiatedExpectCorrectDefaultValues()
    {
        TransientBuilder<TestComposite> builder = transientBuilderFactory.newTransientBuilder( TestComposite.class );
        TestComposite testComposite = builder.newInstance();

        assertThat( testComposite.initializedStringDefault().get(), equalTo( "abc" ) );
        assertThat( testComposite.initializedIntegerDefaultValue().get(), equalTo( 123 ) );
        assertThat( testComposite.initializedFloatDefaultValue().get(), equalTo( 123.45f ) );
        List<String> expectedList = Collections.singletonList( "abcde" );
        assertThat( testComposite.initializedStringListDefultString().get(), equalTo( expectedList ) );
        Map<String, Integer> expectedMap = Collections.singletonMap( "abcd", 345 );
        assertThat( testComposite.initializedMapDefaultValue().get(), equalTo( expectedMap ) );

        assertTrue( "initializedPrimitiveArray is set",
                    Arrays.equals( testComposite.initializedPrimitiveArray().get(), new int[] { 23, 42 } ) );
        assertTrue( "initializedArray is set",
                    Arrays.equals( testComposite.initializedArray().get(), new Integer[] { 23, 42 } ) );
    }

    interface TestComposite
    {
        @Optional
        Property<Integer> nullInt();

        @Optional
        @UseDefaults
        Property<Integer> defaultInt();

        @Optional
        Property<String> nullString();

        @Optional
        @UseDefaults
        Property<String> defaultString();

        Property<String> assemblyString();

        @UseDefaults( "abc" )
        Property<String> initializedStringDefault();

        @UseDefaults( "123" )
        Property<Integer> initializedIntegerDefaultValue();

        @UseDefaults( "123.45" )
        Property<Float> initializedFloatDefaultValue();

        @UseDefaults( "[\"abcde\"]" )
        Property<List<String>> initializedStringListDefultString();

        @UseDefaults( "{\"abcd\": 345}" )
        Property<Map<String, Integer>> initializedMapDefaultValue();

        @Optional
        Property<int[]> nullPrimitiveArray();

        @UseDefaults
        Property<int[]> emptyPrimitiveArray();

        @UseDefaults( "[23, 42]" )
        Property<int[]> initializedPrimitiveArray();

        @Optional
        Property<Integer[]> nullArray();

        @UseDefaults
        Property<Integer[]> emptyArray();

        @UseDefaults( "[23, 42]" )
        Property<Integer[]> initializedArray();
    }
}
