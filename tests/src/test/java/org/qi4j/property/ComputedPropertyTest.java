/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.property;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.This;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ComputedPropertyTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( StorableComposite.class );
    }

    @Test
    public void testComputedProperty()
    {
        Storable storable = compositeBuilderFactory.newComposite( Storable.class );

        Property<Double> volume = storable.volume();

        assertThat( "volume is zero", volume.get(), equalTo( 0.0 ) );

        storable.depth().set( 3.0 );
        storable.width().set( 3.0 );
        storable.height().set( 3.0 );

        assertThat( "volume is not zero", volume.get(), equalTo( 27.0 ) );
    }

    @Mixins( StorableVolumeMixin.class )
    public interface StorableComposite
        extends Storable, Composite
    {
    }

    public interface Storable
    {
        Property<Double> width();

        Property<Double> height();

        Property<Double> depth();

        ImmutableProperty<Double> volume();
    }

    public static abstract class StorableVolumeMixin
        implements Storable
    {
        @This Storable s;
        @PropertyField ImmutableProperty<Double> volume;

        public ImmutableProperty<Double> volume()
        {
            return new ComputedPropertyInstance<Double>( volume )
            {
                @Override public Double get()
                {
                    return s.width().get() * s.height().get() * s.depth().get();
                }
            };
        }
    }
}