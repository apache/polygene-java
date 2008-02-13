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

import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.service.Activatable;
import org.qi4j.spi.service.ServiceRegistry;

/**
 * TODO
 */
public final class LayerInstance
    implements Activatable
{
    private LayerContext layerContext;
    private List<ModuleInstance> moduleInstances;
    private Map<Class<? extends Composite>, ModuleInstance> publicCompositeModules;
    private Map<Class, ModuleInstance> publicObjectModules;
    private Map<Class, ModuleInstance> publicServiceModules;
    private ServiceRegistry serviceRegistry;


    public LayerInstance( LayerContext layerContext, List<ModuleInstance> moduleInstances,
                          Map<Class<? extends Composite>, ModuleInstance> publicCompositeModules,
                          Map<Class, ModuleInstance> publicObjectModules,
                          Map<Class, ModuleInstance> publicServiceModules, ServiceRegistry serviceRegistry )
    {
        this.serviceRegistry = serviceRegistry;
        this.publicServiceModules = publicServiceModules;
        this.publicObjectModules = publicObjectModules;
        this.publicCompositeModules = publicCompositeModules;
        this.layerContext = layerContext;
        this.moduleInstances = moduleInstances;
    }

    public LayerContext getLayerContext()
    {
        return layerContext;
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

    public Map<Class, ModuleInstance> getPublicServiceModules()
    {
        return publicServiceModules;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
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
}

