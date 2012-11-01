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
import org.qi4j.api.structure.LayerDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class LayerDetailDescriptor
{
    private final LayerDescriptor descriptor;
    private ApplicationDetailDescriptor application;
    private final List<LayerDetailDescriptor> usedLayers;
    private final List<ModuleDetailDescriptor> modules;

    LayerDetailDescriptor( LayerDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        usedLayers = new LinkedList<LayerDetailDescriptor>();
        modules = new LinkedList<ModuleDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final LayerDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Used layers of this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<LayerDetailDescriptor> usedLayers()
    {
        return usedLayers;
    }

    /**
     * @return Layers that used this layer.
     *
     * @since 0.5
     */
    public final List<LayerDetailDescriptor> usedBy()
    {
        List<LayerDetailDescriptor> usedBy = new LinkedList<LayerDetailDescriptor>();

        Iterable<LayerDetailDescriptor> layers = application.layers();
        for( LayerDetailDescriptor layer : layers )
        {
            List<LayerDetailDescriptor> layerUsedLayers = layer.usedLayers;
            if( layerUsedLayers.contains( this ) )
            {
                usedBy.add( layer );
            }
        }

        return usedBy;
    }

    /**
     * @return Modules of this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<ModuleDetailDescriptor> modules()
    {
        return modules;
    }

    /**
     * @return Application that owns this {@code LayerDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ApplicationDetailDescriptor application()
    {
        return application;
    }

    final void setApplication( ApplicationDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        application = aDescriptor;
    }

    final void addUsedLayer( LayerDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        usedLayers.add( aDescriptor );
    }

    final void addModule( ModuleDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setLayer( this );
        modules.add( aDescriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }
}