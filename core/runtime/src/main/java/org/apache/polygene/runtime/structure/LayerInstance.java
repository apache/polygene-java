/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.activation.ActivationEventListener;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.runtime.activation.ActivationDelegate;

/**
 * Instance of a Polygene application layer. Contains a list of modules which are managed by this layer.
 */
public class LayerInstance
    implements Layer
{

    // Constructor parameters
    private final LayerModel layerModel;
    private final ApplicationInstance applicationInstance;

    // Eager instance objects
    private final ActivationDelegate activation;
    private final List<ModuleInstance> moduleInstances;

    public LayerInstance( LayerModel model,
                          ApplicationInstance applicationInstance
    )
    {
        // Constructor parameters
        this.layerModel = model;
        this.applicationInstance = applicationInstance;

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

    @Override
    public Application application()
    {
        return applicationInstance;
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

    @Override
    public LayerDescriptor descriptor()
    {
        return layerModel;
    }

    void addModule( ModuleInstance module )
    {
        module.registerActivationEventListener( activation );
        moduleInstances.add( module );
    }

    public LayerModel model()
    {
        return layerModel;
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
