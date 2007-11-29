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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO
 */
public class LayerResolution
    implements Serializable
{
    private LayerModel layerModel;
    private Iterable<ModuleResolution> moduleResolutions;
    private Iterable<LayerModel> uses = new ArrayList<LayerModel>();

    public LayerResolution( LayerModel layerModel, Iterable<ModuleResolution> modules, Iterable<LayerModel> uses )
    {
        this.layerModel = layerModel;
        this.moduleResolutions = modules;
        this.uses = uses;
    }

    public LayerModel getLayerModel()
    {
        return layerModel;
    }

    public Iterable<ModuleResolution> getModuleResolutions()
    {
        return moduleResolutions;
    }

    public Iterable<LayerModel> getUses()
    {
        return uses;
    }
}
