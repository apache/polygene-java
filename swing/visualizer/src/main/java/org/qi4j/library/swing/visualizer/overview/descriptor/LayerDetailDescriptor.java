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
package org.qi4j.library.swing.visualizer.overview.descriptor;

import java.util.LinkedList;
import java.util.List;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.structure.LayerDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class LayerDetailDescriptor
{
    private final LayerDescriptor descriptor;
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
     * @return Layer descriptor. This argument must not be {@code null}.
     * @since 0.5
     */
    public final LayerDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Used layers of this layer. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<LayerDetailDescriptor> usedLayers()
    {
        return usedLayers;
    }

    /**
     * @return modules of this layer. Must not return {@code null}.
     * @since 0.5
     */
    public final Iterable<ModuleDetailDescriptor> modules()
    {
        return modules;
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
        modules.add( aDescriptor );
    }
}
