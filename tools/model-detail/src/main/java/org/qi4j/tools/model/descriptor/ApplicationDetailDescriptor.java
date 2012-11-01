/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.structure.ApplicationDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ApplicationDetailDescriptor
{
    private final ApplicationDescriptor descriptor;
    private final List<LayerDetailDescriptor> layers;

    ApplicationDetailDescriptor( ApplicationDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        layers = new LinkedList<LayerDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code ApplicationDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ApplicationDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Layers of this {@code ApplicationDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<LayerDetailDescriptor> layers()
    {
        return layers;
    }

    final void addLayer( LayerDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setApplication( this );
        layers.add( aDescriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }
}