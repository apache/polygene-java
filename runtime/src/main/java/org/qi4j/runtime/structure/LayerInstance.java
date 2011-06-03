/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.event.LayerActivationEvent;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.service.Activator;
import org.qi4j.spi.structure.LayerSPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance of a Qi4j application layer. Contains a list of modules which are managed by this layer.
 */
public class LayerInstance
        implements Layer, LayerSPI
{
    private final LayerModel model;
    private final ApplicationInstance applicationInstance;
    private final List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();
    private final Activator moduleActivator;
    private final UsedLayersInstance usedLayersInstance;
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    public LayerInstance( LayerModel model,
                          ApplicationInstance applicationInstance,
                          UsedLayersInstance usedLayersInstance
    )
    {
        this.model = model;
        this.applicationInstance = applicationInstance;
        this.usedLayersInstance = usedLayersInstance;
        this.moduleActivator = new Activator();
    }

    void addModule( ModuleInstance module )
    {
        moduleInstances.add( module );
        module.registerActivationEventListener( eventListenerSupport );
    }

    public LayerModel model()
    {
        return model;
    }

    public ApplicationInstance applicationInstance()
    {
        return applicationInstance;
    }

    public String name()
    {
        return model.name();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.deregisterActivationEventListener( listener );
    }

    public List<Module> modules()
    {
        List<Module> result = new ArrayList<Module>();
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            result.add( moduleInstance );
        }
        return result;
    }

    public UsedLayersInstance usedLayersInstance()
    {
        return usedLayersInstance;
    }

    public ModuleInstance findModule( String moduleName )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( moduleInstance.model().name().equals( moduleName ) )
            {
                return moduleInstance;
            }
        }

        return null;
    }

    public void activate()
            throws Exception
    {
        eventListenerSupport.fireEvent( new LayerActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        moduleActivator.activate( moduleInstances );
        eventListenerSupport.fireEvent( new LayerActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
            throws Exception
    {
        eventListenerSupport.fireEvent( new LayerActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        moduleActivator.passivate();
        eventListenerSupport.fireEvent( new LayerActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    public <ThrowableType extends Throwable> boolean visitModules( ModuleVisitor<ThrowableType> visitor,
                                                                   Visibility visibility
    )
            throws ThrowableType
    {
        // Visit modules in this layer
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( !visitor.visitModule( moduleInstance, moduleInstance.model(), visibility ) )
            {
                return false;
            }
        }

        if( visibility == Visibility.layer )
        {
            // Visit modules in this layer
            if( !visitModules( visitor, Visibility.application ) )
            {
                return false;
            }

            // Visit modules in used layers
            return usedLayersInstance.visitModules( visitor );
        }

        return true;
    }

    @Override
    public String toString()
    {
        return model.toString();
    }
}
