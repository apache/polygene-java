/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
package org.apache.zest.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.PassivationException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.runtime.activation.ActivationDelegate;
import org.apache.zest.spi.module.ModelModule;

/**
 * Instance of a Zest application layer. Contains a list of modules which are managed by this layer.
 */
public class LayerInstance
    implements Layer
{

    // Constructor parameters
    private final LayerModel layerModel;
    private final ApplicationInstance applicationInstance;
    private final UsedLayersInstance usedLayersInstance;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final List<ModuleInstance> moduleInstances;

    public LayerInstance( LayerModel model,
                          ApplicationInstance applicationInstance,
                          UsedLayersInstance usedLayersInstance
    )
    {
        // Constructor parameters
        this.layerModel = model;
        this.applicationInstance = applicationInstance;
        this.usedLayersInstance = usedLayersInstance;

        // Eager instance objects
        activation = new ActivationDelegate( this );
        moduleInstances = new ArrayList<>();
    }

    @Override
    public String toString()
    {
        return layerModel.toString();
    }

    // Implementation of Layer
    @Override
    public String name()
    {
        return layerModel.name();
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return layerModel.metaInfo( infoType );
    }

    // Implementation of Activation
    @Override
    public void activate()
        throws ActivationException
    {
        activation.activate( layerModel.newActivatorsInstance(), moduleInstances );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    @Override
    public Stream<? extends Module> modules()
    {
        return moduleInstances.stream();
    }

    // Other methods
    /* package */ void addModule( ModuleInstance module )
    {
        module.registerActivationEventListener( activation );
        moduleInstances.add( module );
    }

    /* package */ LayerModel model()
    {
        return layerModel;
    }

    public ApplicationInstance applicationInstance()
    {
        return applicationInstance;
    }

    /* package */ UsedLayersInstance usedLayersInstance()
    {
        return usedLayersInstance;
    }

    /* package */ Stream<ModelModule<? extends ModelDescriptor>> visibleObjects( final Visibility visibility )
    {
        return moduleInstances.stream().flatMap( module -> module.visibleObjects( visibility ) );
    }

    /* package */ Stream<ModelModule<? extends ModelDescriptor>> visibleTransients( final Visibility visibility )
    {
        return moduleInstances.stream().flatMap( module -> module.visibleTransients( visibility ) );
    }

    /* package */ Stream<ModelModule<? extends ModelDescriptor>> visibleEntities( final Visibility visibility )
    {
        return moduleInstances.stream().flatMap( module -> module.visibleEntities( visibility ) );
    }

    /* package */ Stream<ModelModule<? extends ModelDescriptor>> visibleValues( final Visibility visibility )
    {
        return moduleInstances.stream().flatMap( module -> module.visibleValues( visibility ) );
    }

    /* package */ Stream<ServiceReference<?>> visibleServices( final Visibility visibility )
    {
        return moduleInstances.stream().flatMap( module -> module.visibleServices( visibility ) );
    }

    /* package */ ModuleInstance findModule( String moduleName )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( moduleInstance.model().name().equals( moduleName ) )
            {
                return moduleInstance;
            }
        }

        throw new IllegalArgumentException( "No such module:" + moduleName );
    }
}
