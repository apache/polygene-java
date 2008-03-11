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
import org.qi4j.spi.structure.ApplicationBinding;
import org.qi4j.spi.structure.LayerResolution;

/**
 * TODO
 */
public final class ApplicationContext
{
    private ApplicationBinding applicationBinding;
    private Iterable<LayerContext> layerContexts;

    public ApplicationContext( ApplicationBinding applicationBinding, Iterable<LayerContext> layerContexts )
    {
        this.applicationBinding = applicationBinding;
        this.layerContexts = layerContexts;
    }

    public ApplicationBinding getApplicationBinding()
    {
        return applicationBinding;
    }

    public ApplicationInstance newApplicationInstance( String name )
    {
        if( name == null )
        {
            // Default instance name
            name = applicationBinding.getApplicationResolution().getApplicationModel().getName() + " instance";
        }

        List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
        Map<LayerResolution, LayerInstance> usedLayers = new HashMap<LayerResolution, LayerInstance>();
        for( LayerContext layerContext : layerContexts )
        {
            Iterable<LayerResolution> uses = layerContext.getLayerBinding().getLayerResolution().getUses();
            Map<Class<? extends Composite>, ModuleInstance> availableCompositeModules = new HashMap<Class<? extends Composite>, ModuleInstance>();
            Map<Class, ModuleInstance> availableObjectModules = new HashMap<Class, ModuleInstance>();
            Map<Class, List<ModuleInstance>> availableServiceModules = new HashMap<Class, List<ModuleInstance>>();
            for( LayerResolution use : uses )
            {
                LayerInstance usedLayer = usedLayers.get( use );

                Map<Class<? extends Composite>, ModuleInstance> publicCompositeModules = usedLayer.getPublicCompositeModules();
                availableCompositeModules.putAll( publicCompositeModules );

                Map<Class, ModuleInstance> publicObjectModules = usedLayer.getPublicObjectModules();
                availableObjectModules.putAll( publicObjectModules );

                Map<Class, List<ModuleInstance>> publicServiceModules = usedLayer.getPublicServiceModules();
                // TODO Do proper list insertion
                availableServiceModules.putAll( publicServiceModules );
            }

            LayerInstance layerInstance = layerContext.newLayerInstance( availableCompositeModules, availableObjectModules, availableServiceModules );
            layerInstances.add( layerInstance );
            usedLayers.put( layerContext.getLayerBinding().getLayerResolution(), layerInstance );
        }

        ApplicationInstance applicationInstance = new ApplicationInstance( this, layerInstances, name );

        return applicationInstance;
    }
}
