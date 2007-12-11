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
import org.qi4j.spi.service.ServiceProvider;

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

    public void addComposites( Class<? extends Composite>... compositeTypes )
    {
        addComposites( false, compositeTypes );
    }

    public void addComposites( boolean isModulePublic, Class<? extends Composite>... compositeTypes )
    {
        for( Class<? extends Composite> compositeType : compositeTypes )
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
    }

    public void addComposites( boolean isModulePublic, boolean isLayerPublic, Class<? extends Composite>... compositeTypes )
    {
        addComposites( isModulePublic, compositeTypes );

        if( isLayerPublic )
        {
            for( Class<? extends Composite> compositeType : compositeTypes )
            {
                layerAssembly.addPublicComposite( compositeType );
            }
        }
    }

    public void addObjects( Class... objectTypes )
    {
        for( Class objectType : objectTypes )
        {
            objects.add( objectType );
        }
    }

    public void addServiceProvider( ServiceProvider serviceProvider, Class... serviceTypes )
    {
        for( Class serviceType : serviceTypes )
        {
            serviceProviders.put( serviceType, serviceProvider );

            if( Composite.class.isAssignableFrom( serviceType ) )
            {
                addComposites( (Class<? extends Composite>) serviceType );
            }
        }

        addObjects( serviceProvider.getClass() );
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
