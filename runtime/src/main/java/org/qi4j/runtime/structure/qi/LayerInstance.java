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

import java.util.List;
import org.qi4j.composite.AmbiguousMixinTypeException;
import org.qi4j.runtime.composite.qi.CompositeModel;

/**
 * TODO
 */
public class LayerInstance
{
    private LayerModel model;
    private List<ModuleInstance> moduleInstances;
    private UsedLayersInstance usedLayersInstance;

    public LayerInstance( LayerModel model, List<ModuleInstance> moduleInstances, UsedLayersInstance usedLayersInstance )
    {
        this.model = model;
        this.moduleInstances = moduleInstances;
        this.usedLayersInstance = usedLayersInstance;
    }

    public LayerModel model()
    {
        return model;
    }

    public List<ModuleInstance> modules()
    {
        return moduleInstances;
    }

    public UsedLayersInstance usedLayersInstance()
    {
        return usedLayersInstance;
    }

    public ModuleInstance findModuleFor( Class mixinType )
    {
        // Check this layer
        ModuleInstance foundModule = null;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            CompositeModel compositeModel = moduleInstance.model().getCompositeModelFor( mixinType );
            if( compositeModel != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousMixinTypeException( mixinType );
                }
                else
                {
                    foundModule = moduleInstance;
                }
            }
        }

        if( foundModule != null )
        {
            return foundModule;
        }

        // Check used layers
        return usedLayersInstance.findModuleFor( mixinType );
    }
}
