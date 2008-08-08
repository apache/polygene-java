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

package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.spi.structure.LayerDescriptor;

/**
 * TODO
 */
public final class LayerModel
    implements Binder, LayerDescriptor
{
    // Model
    private final String name;
    private final UsedLayersModel usedLayersModel;
    private final List<ModuleModel> modules;

    public LayerModel( String name,
                       UsedLayersModel usedLayersModel,
                       List<ModuleModel> modules )
    {
        this.name = name;
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

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        for( ModuleModel module : modules )
        {
            module.visitModel( modelVisitor );
        }
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), this, null, null, null, null, null );
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

    @Override public String toString()
    {
        return name;
    }
}
