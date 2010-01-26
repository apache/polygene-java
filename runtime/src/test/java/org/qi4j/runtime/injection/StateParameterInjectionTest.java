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

package org.qi4j.runtime.injection;

import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test the @State annotation when used for parameters
 */
public class StateParameterInjectionTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( PropertyParameterInjectionComposite.class );
    }

    /**
     * Tests that a mixin is injected where method parameters are annotated with {@link @PropertyParameter}.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void mixinIsInjectedForMethodParametersAnnotatedWithPropertyparameter()
        throws Exception
    {
        TransientBuilder<PropertyParameterInjectionComposite> pficBuilder =
            transientBuilderFactory.newTransientBuilder( PropertyParameterInjectionComposite.class );
        pficBuilder.prototype().testField().set( "X" );
        pficBuilder.prototype().namedField().set( "Y" );
        PropertyParameterInjectionComposite pfic = pficBuilder.newInstance();
        assertThat( "Test field", pfic.testField().get(), is( equalTo( "X" ) ) );
        assertThat( "Named field", pfic.namedField().get(), is( equalTo( "Y" ) ) );
    }

    @Mixins( PropertyParameterInjectionMixin.class )
    public interface PropertyParameterInjectionComposite
        extends TransientComposite
    {
        Property<String> testField();

        Property<String> namedField();
    }

    public abstract static class PropertyParameterInjectionMixin
        implements PropertyParameterInjectionComposite
    {
        Property<String> testField;
        Property<String> namedField;

        public PropertyParameterInjectionMixin( @State( "testField" ) Property<String> testField )
        {
            this.testField = testField;
        }

        void init( @State( "namedField" ) Property<String> namedField )
        {
            this.namedField = namedField;
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