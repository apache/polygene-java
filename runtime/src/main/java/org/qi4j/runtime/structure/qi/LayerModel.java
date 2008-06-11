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
import java.util.List;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.qi.DependencyVisitor;
import org.qi4j.runtime.composite.qi.Resolution;

/**
 * TODO
 */
public final class LayerModel
    implements Binder
{
    // Model
    private String name;
    private ApplicationModel applicationComposite;
    private UsedLayersModel usedLayersModel;
    private List<ModuleModel> modules;

    public LayerModel( String name, ApplicationModel applicationComposite, UsedLayersModel usedLayersModel, List<ModuleModel> modules )
    {
        this.name = name;
        this.applicationComposite = applicationComposite;
        this.usedLayersModel = usedLayersModel;
        this.modules = modules;
    }

    public String name()
    {
        return name;
    }

    public UsedLayersModel usedLayers()
    {
        return usedLayersModel;
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( ModuleModel module : modules )
        {
            module.visitDependencies( visitor );
        }
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), this, null, null, null, null );
        for( ModuleModel module : modules )
        {
            module.bind( resolution );
        }
    }

    // Context
    public LayerInstance newInstance( ApplicationInstance applicationInstance, UsedLayersInstance usedLayerInstance )
    {
        List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();
        LayerInstance layerInstance = new LayerInstance( this, applicationInstance, moduleInstances, usedLayerInstance );
        for( ModuleModel module : modules )
        {
            ModuleInstance moduleInstance = module.newInstance( layerInstance );
            moduleInstances.add( moduleInstance );
        }

        return layerInstance;
    }
}
