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

package org.apache.zest.runtime.injection;

import org.junit.Test;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the @State annotation
 */
public class StateInjectionTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( StateInjectionTest.PropertyFieldInjectionComposite.class );
    }

    /**
     * Tests that a mixin is injected into member variables annotated with {@link @PropertyField}.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void mixinIsInjectedForMemberVariablesAnnotatedWithPropertyField()
        throws Exception
    {
        TransientBuilder<PropertyFieldInjectionComposite> pficBuilder =
            transientBuilderFactory.newTransientBuilder( StateInjectionTest.PropertyFieldInjectionComposite.class );
        pficBuilder.prototype().testField().set( "X" );
        PropertyFieldInjectionComposite pfic = pficBuilder.newInstance();
        assertThat( "Test field", pfic.testField().get(), is( equalTo( "X" ) ) );
        assertThat( "Named fieldX", pfic.namedField().get(), is( equalTo( "X" ) ) );
        assertThat( "State", (String) pfic.getState()
            .propertyFor( PropertyFieldInjectionComposite.class.getMethod( "testField" ) )
            .get(), is( equalTo( "X" ) ) );
    }

    @Mixins( PropertyFieldInjectionMixin.class )
    public interface PropertyFieldInjectionComposite
        extends TransientComposite
    {
        @Optional
        Property<String> testField();

        @Optional
        Property<String> namedField();

        StateHolder getState();
    }

    public abstract static class PropertyFieldInjectionMixin
        implements PropertyFieldInjectionComposite
    {
        @State
        Property<String> testField;

        @State( "testField" )
        Property<String> namedField;

        @State
        StateHolder state;

        public StateHolder getState()
        {
            return state;
        }

        public Property<String> testField()
        {
            return testField;
        }

        public Property<String> namedField()
        {
            return namedField;
        }
    }
}
