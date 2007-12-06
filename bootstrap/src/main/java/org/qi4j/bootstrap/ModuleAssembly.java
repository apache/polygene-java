/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.bootstrap;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.spi.injection.ServiceProvider;

/**
 * TODO
 */
public class ModuleAssembly
{
    private LayerAssembly layerAssembly;
    private Set<Class<? extends Composite>> publicComposites = new LinkedHashSet<Class<? extends Composite>>();
    private Set<Class<? extends Composite>> privateComposites = new LinkedHashSet<Class<? extends Composite>>();
    private Set<Class> objects = new LinkedHashSet<Class>();
    private Map<Class, ServiceProvider> serviceProviders = new HashMap<Class, ServiceProvider>();
    private String name;

    public ModuleAssembly( LayerAssembly layerAssembly )
    {
        this.layerAssembly = layerAssembly;
    }

    public void addAssembly( Assembly assembly )
        throws AssemblyException
    {
        // Invoke Assembly callbacks
        assembly.configure( this );
    }

    public LayerAssembly getLayerAssembly()
    {
        return layerAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void addComposite( Class<? extends Composite> compositeType )
    {
        addComposite( compositeType, false );
    }

    public void addComposite( Class<? extends Composite> compositeType, boolean isModulePublic )
    {
        if( isModulePublic )
        {
            publicComposites.add( compositeType );
        }
        else
        {
            privateComposites.add( compositeType );
        }
    }

    public void addComposite( Class<? extends Composite> compositeType, boolean isModulePublic, boolean isLayerPublic )
    {
        if( isModulePublic )
        {
            publicComposites.add( compositeType );
        }
        else
        {
            privateComposites.add( compositeType );
        }

        if( isLayerPublic )
        {
            layerAssembly.addPublicComposite( compositeType );
        }
    }

    public void addObject( Class objectType )
    {
        objects.add( objectType );
    }

    public void addService( Class serviceType, ServiceProvider serviceProvider )
    {
        serviceProviders.put( serviceType, serviceProvider );
    }

    Set<Class<? extends Composite>> getPublicComposites()
    {
        return publicComposites;
    }

    Set<Class<? extends Composite>> getPrivateComposites()
    {
        return privateComposites;
    }

    public Set<Class> getObjects()
    {
        return objects;
    }

    Map<Class, ServiceProvider> getServiceProviders()
    {
        return serviceProviders;
    }

    String getName()
    {
        return name;
    }
}
