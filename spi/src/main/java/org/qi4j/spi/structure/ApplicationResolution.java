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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public final class ApplicationResolution
    implements Serializable
{
    private ApplicationModel applicationModel;
    private Iterable<LayerResolution> layerResolutions;

    private Map<LayerModel, LayerResolution> layerResolutionMap = new HashMap<LayerModel, LayerResolution>();

    public ApplicationResolution( ApplicationModel applicationModel, Iterable<LayerResolution> layers )
    {
        this.applicationModel = applicationModel;
        this.layerResolutions = layers;
        for( LayerResolution layer : layers )
        {
            layerResolutionMap.put( layer.getLayerModel(), layer );
        }
    }

    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }

    public Iterable<LayerResolution> getLayerResolutions()
    {
        return layerResolutions;
    }

    public LayerResolution getLayerResolution( LayerModel layerModel )
    {
        return layerResolutionMap.get( layerModel );
    }
}
