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
package org.apache.zest.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.PassivationException;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.PolygeneRuntime;
import org.apache.zest.runtime.activation.ActivationDelegate;

/**
 * Instance of a Polygene application. Contains a list of layers which are managed by this application
 */
public class ApplicationInstance
    implements Application
{

    // Constructor parameters
    private final ApplicationModel applicationModel;
    private final PolygeneRuntime runtime;
    private final MetaInfo instanceMetaInfo;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final List<LayerInstance> layerInstances;

    public ApplicationInstance( ApplicationModel model, PolygeneRuntime runtime, MetaInfo instanceMetaInfo )
    {
        // Constructor parameters
        this.applicationModel = model;
        this.runtime = runtime;
        this.instanceMetaInfo = instanceMetaInfo;

        // Eager instance objects
        activation = new ActivationDelegate( this );
        layerInstances = new ArrayList<>();
    }

    @Override
    public String toString()
    {
        return name();
    }

    // Implementation of Application
    @Override
    public String name()
    {
        return applicationModel.name();
    }

    @Override
    public String version()
    {
        return applicationModel.version();
    }

    @Override
    public Mode mode()
    {
        return applicationModel.mode();
    }

    @Override
    public Layer findLayer( String layerName )
    {
        for( LayerInstance layerInstance : layerInstances )
        {
            if( layerInstance.model().name().equals( layerName ) )
            {
                return layerInstance;
            }
        }

        throw new IllegalArgumentException( "No such layer:" + layerName );
    }

    @Override
    public Module findModule( String layerName, String moduleName )
    {
        for( LayerInstance layerInstance : layerInstances )
        {
            if( layerInstance.model().name().equals( layerName ) )
            {
                return layerInstance.findModule( moduleName );
            }
        }

        throw new IllegalArgumentException( "No such layer:" + layerName );
    }

    @Override
    public Stream<? extends Layer> layers()
    {
        return layerInstances.stream();
    }

    @Override
    public ApplicationDescriptor descriptor()
    {
        return applicationModel;
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return instanceMetaInfo.get( infoType );
    }

    // Implementation of Activation
    @Override
    public void activate()
        throws ActivationException
    {
        activation.activate( applicationModel.newActivatorsInstance(), layerInstances );
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

    // Other methods
    void addLayer( LayerInstance layer )
    {
        layer.registerActivationEventListener( activation );
        layerInstances.add( layer );
    }

    public PolygeneRuntime runtime()
    {
        return runtime;
    }

}
