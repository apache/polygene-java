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

package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.structure.Application;
import org.qi4j.structure.ApplicationBuilder;
import org.qi4j.structure.Layer;
import org.qi4j.structure.LayerBuilder;

/**
 * TODO
 */
public class ApplicationBuilderImpl
    implements ApplicationBuilder
{
    Qi4jRuntime runtime;

    List<LayerBuilderImpl> layerBuilders = new ArrayList<LayerBuilderImpl>();

    public ApplicationBuilderImpl( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public LayerBuilder newLayerBuilder()
    {
        LayerBuilderImpl layerBuilder = new LayerBuilderImpl( runtime );
        layerBuilders.add( layerBuilder );
        return layerBuilder;
    }

    public Application newApplication()
    {
        Map<LayerBuilder, LayerImpl> layerMap = new HashMap<LayerBuilder, LayerImpl>();
        List<Layer> layers = new ArrayList<Layer>();

        // Instantiate Layers
        for( LayerBuilderImpl layerBuilder : layerBuilders )
        {
            LayerImpl layer = layerBuilder.newLayer();
            layers.add( layer );
            layerMap.put( layerBuilder, layer );
        }

        // Set up Layer usages
        for( LayerBuilderImpl layerBuilder : layerBuilders )
        {
            Set<LayerBuilder> uses = layerBuilder.getUses();
            LayerImpl layer = layerMap.get( layerBuilder );
            for( LayerBuilder use : uses )
            {
                LayerImpl usedLayer = layerMap.get( use );
                layer.addUses( usedLayer );
                usedLayer.addUsage( layer );
            }

        }

        // Create Application
        Application application = new ApplicationImpl( layers );

        // Set Application references
        for( Iterator<LayerImpl> layerImpls = layerMap.values().iterator(); layerImpls.hasNext(); )
        {
            LayerImpl layer = layerImpls.next();
            layer.setApplication( application );
        }

        // Clear list of builders in case newApplication is called again
        layerBuilders.clear();

        return application;
    }
}
