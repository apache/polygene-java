/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.library.general.test.model.DescriptorConcern;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class DescriptorTest extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( DummyComposite.class, DummyComposite2.class );
    }

    public void testDescriptorAsMixin()
        throws Exception
    {
        CompositeBuilder<DummyComposite> builder = compositeBuilderFactory.newCompositeBuilder( DummyComposite.class );
        DummyComposite composite = builder.newInstance();

        Property<String> displayValueProperty = composite.displayValue();
        String value = "Sianny";
        displayValueProperty.set( value );

        String displayValue = displayValueProperty.get();
        assertEquals( value, displayValue );
    }

    e

    public void testDescriptorWithModifier() throws Exception
    {
        DummyComposite2 composite = compositeBuilderFactory.newCompositeBuilder( DummyComposite2.class ).newInstance();
        composite.displayValue().set( "Sianny" );
        String displayValue = composite.displayValue().get();
        assertEquals( displayValue, "My name is Sianny" );
    }

    @Mixins( PropertyMixin.class )
    private interface DummyComposite extends Descriptor, HasName, Composite
    {
    }

    @Concerns( DescriptorConcern.class )
    @Mixins( PropertyMixin.class )
    private interface DummyComposite2 extends Descriptor, HasName, Composite
    {
    }
}
