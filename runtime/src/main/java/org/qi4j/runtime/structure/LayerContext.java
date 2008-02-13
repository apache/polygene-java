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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.spi.service.ServiceRegistry;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.LayerBinding;
import org.qi4j.spi.structure.ObjectDescriptor;
import org.qi4j.spi.structure.ServiceDescriptor;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public final class LayerContext
{
    LayerBinding layerBinding;
    Iterable<ModuleContext> moduleContexts;

    public LayerContext( LayerBinding layerBinding, Iterable<ModuleContext> moduleContexts )
    {
        this.layerBinding = layerBinding;
        this.moduleContexts = moduleContexts;
    }

    public LayerBinding getLayerBinding()
    {
        return layerBinding;
    }

    LayerInstance newLayerInstance( Map<Class<? extends Composite>, ModuleInstance> availableCompositeModules, Map<Class, ModuleInstance> availableObjectModules, Map<Class, ModuleInstance> availableServiceModules )
    {
        List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();
        Map<Class<? extends Composite>, ModuleInstance> modulesForPublicComposites = new HashMap<Class<? extends Composite>, ModuleInstance>();
        modulesForPublicComposites.putAll( availableCompositeModules );

        Map<Class, ModuleInstance> modulesForPublicObjects = new HashMap<Class, ModuleInstance>();
        modulesForPublicObjects.putAll( availableObjectModules );

        Map<Class, ModuleInstance> modulesForPublicServices = new HashMap<Class, ModuleInstance>();
        modulesForPublicServices.putAll( availableServiceModules );

        ServiceRegistry serviceRegistry = new LayerServiceRegistry( modulesForPublicServices );

        for( ModuleContext moduleContext : moduleContexts )
        {
            ModuleInstance moduleInstance = moduleContext.newModuleInstance( modulesForPublicComposites, modulesForPublicObjects, serviceRegistry );
            moduleInstances.add( moduleInstance );

            Iterable<CompositeDescriptor> publicComposites = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getCompositeDescriptors();
            for( CompositeDescriptor publicComposite : publicComposites )
            {
                if( publicComposite.getVisibility() != Visibility.module )
                {
                    modulesForPublicComposites.put( publicComposite.getCompositeModel().getCompositeClass(), moduleInstance );
                }
            }
            Iterable<ObjectDescriptor> publicObjects = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getObjectDescriptors();
            for( ObjectDescriptor publicObject : publicObjects )
            {
                if( publicObject.getVisibility() != Visibility.module )
                {
                    modulesForPublicObjects.put( publicObject.getObjectModel().getModelClass(), moduleInstance );
                }
            }
            Iterable<ServiceDescriptor> publicServices = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getServiceDescriptors();
            for( ServiceDescriptor publicService : publicServices )
            {
                if( publicService.getVisibility() != Visibility.module )
                {
                    modulesForPublicServices.put( publicService.getServiceType(), moduleInstance );
                }
            }
        }

        return new LayerInstance( this, moduleInstances, modulesForPublicComposites, modulesForPublicObjects, modulesForPublicServices, serviceRegistry );
    }

}
