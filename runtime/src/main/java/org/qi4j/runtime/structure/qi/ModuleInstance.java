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

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.runtime.composite.qi.CompositeBuilderFactoryInstance;
import org.qi4j.runtime.composite.qi.CompositeModel;
import org.qi4j.service.ServiceFinder;

/**
 * TODO
 */
public class ModuleInstance
{
    private ModuleModel moduleModel;
    private LayerInstance layerInstance;

    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private UnitOfWorkFactory unitOfWorkFactory;
    private ServiceFinder serviceFinder;

    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance )
    {
        this.moduleModel = moduleModel;
        this.layerInstance = layerInstance;

        compositeBuilderFactory = new CompositeBuilderFactoryInstance( this );
    }

    public ModuleModel model()
    {
        return moduleModel;
    }

    public CompositeBuilderFactory compositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory objectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return unitOfWorkFactory;
    }

    public ServiceFinder serviceFinder()
    {
        return serviceFinder;
    }

    public ModuleInstance findModuleFor( Class mixinType )
    {
        // Check local first
        CompositeModel model = moduleModel.getCompositeModelFor( mixinType );
        if( model != null )
        {
            return this;
        }

        // Check layer
        return layerInstance.findModuleFor( mixinType );
    }
}
