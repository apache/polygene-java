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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.structure.LayerBuilder;
import org.qi4j.structure.Module;
import org.qi4j.structure.ModuleBuilder;

/**
 * TODO
 */
public class LayerBuilderImpl
    implements LayerBuilder
{
    List<ModuleBuilderImpl> moduleBuilders = new ArrayList<ModuleBuilderImpl>();
    Set<LayerBuilder> uses = new LinkedHashSet<LayerBuilder>();

    Qi4jRuntime runtime;

    public LayerBuilderImpl( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public ModuleBuilder newModuleBuilder()
    {
        ModuleBuilderImpl builder = new ModuleBuilderImpl( runtime.newCompositeBuilderFactory(), runtime.newObjectBuilderFactory() );
        moduleBuilders.add( builder );
        return builder;
    }

    public void uses( LayerBuilder layerBuilder )
    {
        uses.add( layerBuilder );
    }

    LayerImpl newLayer()
    {
        List<ModuleImpl> modules = new ArrayList<ModuleImpl>();
        List<Module> modules2 = new ArrayList<Module>();
        for( ModuleBuilderImpl moduleBuilder : moduleBuilders )
        {
            ModuleImpl module = moduleBuilder.newModule();
            modules.add( module );
            modules2.add( module );
        }

        LayerImpl layer = new LayerImpl( modules2 );

        for( ModuleImpl module : modules )
        {
            module.setLayer( layer );
        }

        return layer;
    }

    Set<LayerBuilder> getUses()
    {
        return uses;
    }
}
