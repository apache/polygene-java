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
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceLocator;
import org.qi4j.service.ServiceReference;

/**
 * TODO
 */
public final class LayerInstance
    implements Activatable, ServiceLocator
{
    private LayerContext layerContext;
    private List<ModuleInstance> moduleInstances;
    private Map<Class<? extends Composite>, ModuleInstance> publicCompositeModules;
    private Map<Class, ModuleInstance> publicObjectModules;
    private Map<Class, List<ModuleInstance>> publicServiceModules;
    private ServiceLocator serviceLocator;
    private Map<Class, ModuleInstance> publicMixinModules;


    public LayerInstance( LayerContext layerContext, List<ModuleInstance> moduleInstances,
                          Map<Class<? extends Composite>, ModuleInstance> publicCompositeModules,
                          Map<Class, ModuleInstance> publicObjectModules,
                          Map<Class, ModuleInstance> publicMixinModules,
                          Map<Class, List<ModuleInstance>> publicServiceModules,
                          ServiceLocator serviceLocator )
    {
        this.serviceLocator = serviceLocator;
        this.publicServiceModules = publicServiceModules;
        this.publicObjectModules = publicObjectModules;
        this.publicMixinModules = publicMixinModules;
        this.publicCompositeModules = publicCompositeModules;
        this.layerContext = layerContext;
        this.moduleInstances = moduleInstances;
    }

    public LayerContext getLayerContext()
    {
        return layerContext;
    }

    public ModuleInstance getModuleByName( String name )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( moduleInstance.getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getName().equals( name ) )
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

    public Map<Class<? extends Composite>, ModuleInstance> getPublicCompositeModules()
    {
        return publicCompositeModules;
    }

    public Map<Class, ModuleInstance> getPublicObjectModules()
    {
        return publicObjectModules;
    }

    public Map<Class, ModuleInstance> getPublicMixinModules()
    {
        return publicMixinModules;
    }

    public Map<Class, List<ModuleInstance>> getPublicServiceModules()
    {
        return publicServiceModules;
    }

    /**
     * Lookup a public Service implemented by a Module in this Layer
     *
     * @param serviceType
     * @return
     */
    public <T> ServiceReference<T> lookupService( Class<T> serviceType )
    {
        List<ModuleInstance> modulesForService = publicServiceModules.get( serviceType );
        if( modulesForService != null )
        {
            for( ModuleInstance moduleInstance : modulesForService )
            {
                ServiceReference<T> serviceReference = moduleInstance.lookupService( serviceType );
                if( serviceReference != null )
                {
                    return serviceReference;
                }
            }
        }
        return null;
    }

    /**
     * Lookup public Services implemented by Modules in this Layer
     *
     * @param serviceType
     * @return
     */
    public <T> Iterable<ServiceReference<T>> lookupServices( Class<T> serviceType )
    {
        List<ModuleInstance> modulesForService = publicServiceModules.get( serviceType );
        if( modulesForService != null )
        {
            List<ServiceReference<T>> serviceRefs = new ArrayList<ServiceReference<T>>();
            for( ModuleInstance moduleInstance : modulesForService )
            {
                Iterable<ServiceReference<T>> serviceReferences = moduleInstance.lookupServices( serviceType );
                for( ServiceReference<T> serviceReference : serviceReferences )
                {
                    serviceRefs.add( serviceReference );
                }
            }
            return serviceRefs;
        }
        else
        {
            return Collections.emptyList();
        }
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

