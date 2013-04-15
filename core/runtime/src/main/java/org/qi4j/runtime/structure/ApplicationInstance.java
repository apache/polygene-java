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
package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;

/**
 * Instance of a Qi4j application. Contains a list of layers which are managed by this application
 */
public class ApplicationInstance
    implements Application
{

    // Constructor parameters
    private final ApplicationModel applicationModel;
    private final Qi4jRuntime runtime;
    private final MetaInfo instanceMetaInfo;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final ActivationEventListenerSupport activationEventSupport;
    private final List<LayerInstance> layerInstances;

    public ApplicationInstance( ApplicationModel model, Qi4jRuntime runtime, MetaInfo instanceMetaInfo )
    {
        // Constructor parameters
        this.applicationModel = model;
        this.runtime = runtime;
        this.instanceMetaInfo = instanceMetaInfo;

        // Eager instance objects
        activation = new ActivationDelegate( this );
        activationEventSupport = new ActivationEventListenerSupport();
        layerInstances = new ArrayList<LayerInstance>();
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
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        activation.activate( applicationModel.newActivatorsInstance(), layerInstances );
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        activation.passivate();
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }

    // Other methods
    /* package */ void addLayer( LayerInstance layer )
    {
        layer.registerActivationEventListener( activationEventSupport );
        layerInstances.add( layer );
    }

    public Qi4jRuntime runtime()
    {
        return runtime;
    }

}
