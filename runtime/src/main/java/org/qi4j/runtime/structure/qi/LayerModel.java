/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.structure.qi;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.composite.qi.Resolution;

/**
 * TODO
 */
public class LayerModel
{
    private ApplicationModel applicationComposite;

    private List<LayerModel> usedLayers;

    private List<ModuleModel> modules;

    public LayerModel( ApplicationModel applicationComposite, List<LayerModel> usedLayers, List<ModuleModel> modules )
    {
        this.applicationComposite = applicationComposite;
        this.usedLayers = usedLayers;
        this.modules = modules;
    }

    // Resolution
    public List<LayerModel> usedLayers()
    {
        return usedLayers;
    }

    // Binding
    public void bind( Resolution resolution )
    {
        resolution = new Resolution( resolution.application(), this, null, null, null );
        for( ModuleModel module : modules )
        {
            module.bind( resolution );
        }
    }

    // Context
    public LayerInstance newInstance( ApplicationInstance applicationInstance, List<LayerInstance> usedLayerInstances )
    {
        List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();
        LayerInstance layerInstance = new LayerInstance( this, moduleInstances, usedLayerInstances );
        for( ModuleModel module : modules )
        {
            ModuleInstance moduleInstance = module.newInstance( layerInstance );
            moduleInstances.add( moduleInstance );
        }

        return layerInstance;
    }
}
