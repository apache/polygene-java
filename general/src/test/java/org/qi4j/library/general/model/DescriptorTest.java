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

import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.injection.scope.PropertyField;
import org.qi4j.property.ComputedProperty;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class DescriptorTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( DummyComposite.class );
    }

    @Test
    public void testDescriptorAsMixin()
        throws Exception
    {
        CompositeBuilder<DummyComposite> builder = compositeBuilderFactory.newCompositeBuilder( DummyComposite.class );
        DummyComposite composite = builder.newInstance();
        composite.internalName().set( "Sianny" );

        String displayValue = composite.displayValue().get();
        assertEquals( "Sianny", displayValue );
    }

    @Mixins( DisplayValueMixin.class )
    private interface DummyComposite extends Descriptor, HasName, Composite
    {
        Property<String> internalName();
    }

    private static class DisplayValueMixin
        implements Descriptor
    {
        @PropertyField private Property<String> internalName;

        public ComputedProperty<String> displayValue()
        {
            Method method = null;
            try
            {
                method = Descriptor.class.getMethod( "displayValue" );
            }
            catch( NoSuchMethodException e )
            {
                throw new InternalError();
            }
            return new ComputedPropertyInstance<String>( method )
            {
                public String get()
                {
                    return internalName.get();
                }
            };
        }
    }
}
