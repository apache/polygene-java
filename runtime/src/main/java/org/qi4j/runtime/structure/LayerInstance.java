/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceReference;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public final class LayerInstance
    implements Activatable
{
    private LayerContext layerContext;
    private List<ModuleInstance> moduleInstances;
    private Iterable<LayerInstance> usedLayers;


    public LayerInstance( LayerContext layerContext,
                          List<ModuleInstance> moduleInstances,
                          Iterable<LayerInstance> usedLayers )
    {
        this.layerContext = layerContext;
        this.moduleInstances = moduleInstances;
        this.usedLayers = usedLayers;
    }

    public LayerContext getLayerContext()
    {
        return layerContext;
    }

    public ModuleInstance getModuleByName( String name )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( moduleInstance.moduleContext().getModuleBinding().getModuleResolution().getModuleModel().getName().equals( name ) )
            {
                return moduleInstance;
            }
        }

        return null;
    }

    public List<ModuleInstance> getModuleInstances()
    {
        return moduleInstances;
    }

    public Iterable<LayerInstance> getUsedLayers()
    {
        return usedLayers;
    }

    /**
     * Lookup a Service implemented by a Module in this Layer
     *
     * @param serviceType
     * @param visibility
     * @return
     */
    public <T> ServiceReference<T> findService( Class<T> serviceType, Visibility visibility )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            ServiceReference<T> serviceRef = moduleInstance.findService( serviceType, visibility );
            if( serviceRef != null )
            {
                return serviceRef;
            }
        }

        return null;
    }

    /**
     * Lookup Services implemented by Modules in this Layer
     *
     * @param serviceType
     * @param visibility
     * @return
     */
    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType, Visibility visibility )
    {
        List<ServiceReference<T>> services = null;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            Iterator<ServiceReference<T>> serviceRefs = moduleInstance.findServices( serviceType, visibility ).iterator();
            if( serviceRefs.hasNext() && services == null )
            {
                services = new ArrayList<ServiceReference<T>>();
            }

            while( serviceRefs.hasNext() )
            {
                services.add( serviceRefs.next() );
            }
        }

        if( services == null )
        {
            services = Collections.EMPTY_LIST;
        }

        return services;
    }

    public void activate() throws Exception
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            moduleInstance.activate();
        }
    }

    public void passivate() throws Exception
    {
        for( int i = moduleInstances.size() - 1; i >= 0; i-- )
        {
            moduleInstances.get( i ).passivate();
        }
    }

    @Override public String toString()
    {
        return layerContext.toString();
    }
}

