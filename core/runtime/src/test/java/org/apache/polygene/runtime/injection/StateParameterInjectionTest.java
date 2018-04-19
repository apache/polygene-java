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

package org.apache.polygene.runtime.injection;

import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the @State annotation when used for parameters
 */
public class StateParameterInjectionTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( PropertyParameterInjectionComposite.class );
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