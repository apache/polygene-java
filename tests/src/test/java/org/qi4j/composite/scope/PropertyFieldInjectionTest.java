/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.composite.scope;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.State;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test the @PropertyField annotation
 */
public class PropertyFieldInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( PropertyFieldInjectionTest.PropertyFieldInjectionComposite.class );
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
        CompositeBuilder<PropertyFieldInjectionComposite> pficBuilder = compositeBuilderFactory.newCompositeBuilder( PropertyFieldInjectionTest.PropertyFieldInjectionComposite.class );
        pficBuilder.stateOfComposite().testField().set( "X" );
        PropertyFieldInjectionComposite pfic = pficBuilder.newInstance();
        assertThat( "Test field", pfic.testField().get(), is( equalTo( "X" ) ) );
        assertThat( "Named fieldX", pfic.namedField().get(), is( equalTo( "X" ) ) );
        assertThat( "State", (String) pfic.getState().getProperty( PropertyFieldInjectionComposite.class.getMethod( "testField" ) ).get(), is( equalTo( "X" ) ) );
    }

    @Mixins( PropertyFieldInjectionMixin.class )
    public interface PropertyFieldInjectionComposite
        extends Composite
    {
        Property<String> testField();

        Property<String> namedField();

        State getState();
    }

    public abstract static class PropertyFieldInjectionMixin
        implements PropertyFieldInjectionComposite
    {
        @PropertyField Property<String> testField;
        @PropertyField( "testField" ) Property<String> namedField;
        @PropertyField State state;
        @PropertyField( optional = true ) Property<String> notImplemented;

        public State getState()
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
