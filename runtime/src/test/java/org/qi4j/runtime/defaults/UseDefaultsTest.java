/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.defaults;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class UseDefaultsTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( TestComposite.class );
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
    }

    interface TestComposite
        extends TransientComposite
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
    }
}
