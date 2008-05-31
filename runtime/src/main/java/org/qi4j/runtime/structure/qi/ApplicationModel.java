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

package org.qi4j.runtime.structure.qi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.runtime.composite.InjectionProviderFactoryStrategy;
import org.qi4j.runtime.composite.qi.InjectionProviderFactory;
import org.qi4j.runtime.composite.qi.Resolution;

/**
 * TODO
 */
public class ApplicationModel
{
    private List<LayerModel> layers;

    private InjectionProviderFactory ipf;

    public ApplicationModel( List<LayerModel> layers )
    {
        this.layers = layers;
        ipf = new InjectionProviderFactoryStrategy();
    }

    // Binding
    public void bind()
    {
        Resolution resolution = new Resolution( this, null, null, null, null );
        for( LayerModel layer : layers )
        {
            layer.bind( resolution );
        }
    }

    // Context
    public ApplicationInstance newInstance()
    {
        List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
        ApplicationInstance applicationInstance = new ApplicationInstance( this, layerInstances );

        Map<LayerModel, LayerInstance> layerInstanceMap = new HashMap<LayerModel, LayerInstance>();
        for( LayerModel layer : layers )
        {
            UsedLayersInstance usedLayersInstance = layer.usedLayers().newInstance( layerInstanceMap );
            LayerInstance layerInstance = layer.newInstance( applicationInstance, usedLayersInstance );
            layerInstances.add( layerInstance );
            layerInstanceMap.put( layer, layerInstance );
        }

        return applicationInstance;
    }

    public InjectionProviderFactory injectionProviderFactory()
    {
        return ipf;
    }
}
