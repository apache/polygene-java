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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.spi.service.Activator;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public class ApplicationInstance
    implements Application
{
    private final ApplicationModel model;
    private final Qi4jRuntime runtime;
    private final List<LayerInstance> layerInstances;
    private final Activator layerActivator;
    private final String uri;

    public ApplicationInstance( ApplicationModel model, Qi4jRuntime runtime, List<LayerInstance> layerInstances )
    {
        this.model = model;
        this.runtime = runtime;
        this.layerInstances = layerInstances;
        layerActivator = new Activator();
        uri = createApplicationUri();
    }

    public ApplicationModel model()
    {
        return model;
    }

    public Qi4jRuntime runtime()
    {
        return runtime;
    }

    public String toURI()
    {
        return uri;
    }

    public List<LayerInstance> layers()
    {
        return layerInstances;
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

    public void activate() throws Exception
    {
        layerActivator.activate( layerInstances );
    }

    public void passivate() throws Exception
    {
        layerActivator.passivate();
    }

    private String createApplicationUri()
    {
        String hostname;
        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch( UnknownHostException e )
        {
            // Can not happen ?
            hostname = "localhost";
        }
        String jvminstance = System.getProperty( "qi4j.jvm.name" );
        if( jvminstance != null )
        {
            jvminstance = ":" + jvminstance;
        }
        else
        {
            jvminstance = "";
        }
        return "urn:qi4j:instance:" + hostname + jvminstance + ":" + model.name();
    }

}
