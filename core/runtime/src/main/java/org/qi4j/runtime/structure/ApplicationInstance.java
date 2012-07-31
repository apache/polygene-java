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
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.activation.ActivationDelegate;

/**
 * Instance of a Qi4j application. Contains a list of layers which are managed by this application
 */
public class ApplicationInstance
    implements Application
{
    private final org.qi4j.runtime.structure.ApplicationModel applicationModel;
    private final Qi4jRuntime runtime;
    private final MetaInfo instanceMetaInfo;
    private final List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private final ActivationEventListenerSupport activationEventSupport = new ActivationEventListenerSupport();

    public ApplicationInstance( ApplicationModel model, Qi4jRuntime runtime, MetaInfo instanceMetaInfo )
    {
        this.applicationModel = model;
        this.runtime = runtime;
        this.instanceMetaInfo = instanceMetaInfo;
    }

    void addLayer( LayerInstance layer )
    {
        layer.registerActivationEventListener( activationEventSupport );
        layerInstances.add( layer );
    }

    public ApplicationDescriptor descriptor()
    {
        return applicationModel;
    }

    public Qi4jRuntime runtime()
    {
        return runtime;
    }

    public String name()
    {
        return applicationModel.name();
    }

    public String version()
    {
        return applicationModel.version();
    }

    public Mode mode()
    {
        return applicationModel.mode();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return instanceMetaInfo.get( infoType );
    }

    public List<LayerInstance> layers()
    {
        return layerInstances;
    }

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

    public void activate()
        throws Exception
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        activation.activate( applicationModel.newActivatorsInstance(), layerInstances );
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
        throws Exception
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        activation.passivate();
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    @Override
    public String toString()
    {
        return name();
    }

    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }
}
