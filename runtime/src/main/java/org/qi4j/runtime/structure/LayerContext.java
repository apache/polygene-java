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
import java.util.List;
import org.qi4j.spi.structure.LayerBinding;
import org.qi4j.spi.util.ListMap;

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

    LayerInstance newLayerInstance( Iterable<LayerInstance> usedLayers )
    {
        List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();

        LayerInstance layerInstance = new LayerInstance( this,
                                                         moduleInstances,
                                                         usedLayers );

        for( ModuleContext moduleContext : moduleContexts )
        {
            ModuleInstance moduleInstance = moduleContext.newModuleInstance( layerInstance );
            moduleInstances.add( moduleInstance );

/*
            Iterable<CompositeDescriptor> publicComposites = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getCompositeDescriptors();
            for( CompositeDescriptor publicComposite : publicComposites )
            {
                if( publicComposite.getVisibility() != Visibility.module )
                {
                    CompositeModel compositeModel = publicComposite.getCompositeModel();
                    Class<? extends Composite> compositeType = compositeModel.getCompositeType();
                    modulesForPublicComposites.put( compositeType, moduleInstance );

                    mapping = MixinMapper.mapMixinsToModule( compositeType, moduleInstance );
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
                if( publicService.visibility() != Visibility.module )
                {
                    Class serviceType = publicService.serviceType();
                    registerServiceModule( serviceType, moduleInstance, modulesForPublicServices );
                    modulesForPublicServices.add( serviceType, moduleInstance );

                }
            }
*/
        }

        return layerInstance;
    }

    private void registerServiceModule( Class serviceType, ModuleInstance moduleInstance, ListMap<Class, ModuleInstance> serviceMappings )
    {
        serviceMappings.add( serviceType, moduleInstance );

        Class[] extended = serviceType.getInterfaces();
        for( Class extendedType : extended )
        {
            registerServiceModule( extendedType, moduleInstance, serviceMappings );
        }
    }

    @Override public String toString()
    {
        return layerBinding.toString();
    }
}
