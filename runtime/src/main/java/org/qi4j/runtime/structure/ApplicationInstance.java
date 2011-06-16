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

import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.runtime.service.Activator;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance of a Qi4j application. Contains a list of layers which are managed by this application
 */
public class ApplicationInstance
    implements Application, ApplicationSPI
{
    private final ApplicationModel model;
    private final Qi4jSPI runtime;
    private final List<LayerInstance> layerInstances = new ArrayList<LayerInstance>(  );
    private final Activator layerActivator;
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    public ApplicationInstance( ApplicationModel model, Qi4jSPI runtime )
    {
        this.model = model;
        this.runtime = runtime;
        layerActivator = new Activator();
    }

    void addLayer(LayerInstance layer)
    {
        layerInstances.add(layer);
        layer.registerActivationEventListener( eventListenerSupport );
    }

    public ApplicationModel model()
    {
        return model;
    }

    public Qi4jSPI runtime()
    {
        return runtime;
    }

    public String name()
    {
        return model.name();
    }

    public String version()
    {
        return model.version();
    }

    public Mode mode()
    {
        return model.mode();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
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

        return null;
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

        return null;
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

    public void activate()
        throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent(this, ActivationEvent.EventType.ACTIVATING) );
        layerActivator.activate( layerInstances );
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
        throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent(this, ActivationEvent.EventType.PASSIVATING) );
        layerActivator.passivate();
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    public <ThrowableType extends Throwable> void visitDescriptor( DescriptorVisitor<ThrowableType> visitor )
        throws ThrowableType
    {
        model.visitDescriptor( visitor );
    }

    public <ThrowableType extends Throwable> void visitInstance( InstanceVisitor<ThrowableType> visitor )
        throws ThrowableType
    {
        visitor.visit( this );

        for( LayerInstance layerInstance : layerInstances )
        {
            visitor.visit( layerInstance );
            for( Module module : layerInstance.modules() )
            {
                visitor.visit( module );
            }
        }
    }

    @Override
    public String toString()
    {
        return name();
    }
}
