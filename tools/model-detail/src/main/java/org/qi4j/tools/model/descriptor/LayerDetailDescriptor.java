/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
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
    implements ActivateeDetailDescriptor
{
    private final LayerDescriptor descriptor;
    private ApplicationDetailDescriptor application;
    private final List<LayerDetailDescriptor> usedLayers = new LinkedList<>();
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();
    private final List<ModuleDetailDescriptor> modules = new LinkedList<>();

    LayerDetailDescriptor( LayerDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "LayerDescriptor", descriptor );
        this.descriptor = descriptor;
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
     */
    public final List<LayerDetailDescriptor> usedBy()
    {
        List<LayerDetailDescriptor> usedBy = new LinkedList<>();
        for( LayerDetailDescriptor layer : application.layers() )
        {
            if( layer.usedLayers.contains( this ) )
            {
                usedBy.add( layer );
            }
        }
        return usedBy;
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    /**
     * @return Modules of this {@code LayerDetailDescriptor}. Never return {@code null}.
     */
    public final Iterable<ModuleDetailDescriptor> modules()
    {
        return modules;
    }

    /**
     * @return Application that owns this {@code LayerDetailDescriptor}. Never return {@code null}.
     */
    public final ApplicationDetailDescriptor application()
    {
        return application;
    }

    final void setApplication( ApplicationDetailDescriptor descriptor )
    {
        validateNotNull( "ApplicationDetailDescriptor", descriptor );
        application = descriptor;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        descriptor.setLayer( this );
        activators.add( descriptor );
    }

    final void addUsedLayer( LayerDetailDescriptor descriptor )
    {
        validateNotNull( "LayerDetailDescriptor", descriptor );
        usedLayers.add( descriptor );
    }

    final void addModule( ModuleDetailDescriptor descriptor )
    {
        validateNotNull( "ModuleDetailDescriptor", descriptor );
        descriptor.setLayer( this );
        modules.add( descriptor );
    }

    @Override
    public final String toString()
    {
        return descriptor.name();
    }

}
