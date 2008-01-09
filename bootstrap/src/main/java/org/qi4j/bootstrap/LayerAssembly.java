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

package org.qi4j.bootstrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public final class LayerAssembly
{
    private ApplicationAssembly applicationAssembly;
    private List<ModuleAssembly> moduleAssemblies = new ArrayList<ModuleAssembly>();
    private Set<LayerAssembly> uses = new LinkedHashSet<LayerAssembly>();
    private Set<Class<? extends Composite>> publicComposites = new LinkedHashSet<Class<? extends Composite>>();

    private String name;

    public LayerAssembly( ApplicationAssembly applicationAssembly )
    {
        this.applicationAssembly = applicationAssembly;
    }

    public ModuleAssembly newModuleAssembly()
    {
        ModuleAssembly moduleAssembly = new ModuleAssembly( this );
        moduleAssemblies.add( moduleAssembly );
        return moduleAssembly;
    }

    public ApplicationAssembly getApplicationAssembly()
    {
        return applicationAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void uses( LayerAssembly layerAssembly )
    {
        uses.add( layerAssembly );
    }

    public void addPublicComposite( Class<? extends Composite> compositeType )
    {
        publicComposites.add( compositeType );
    }

    List<ModuleAssembly> getModuleAssemblies()
    {
        return moduleAssemblies;
    }

    Set<LayerAssembly> getUses()
    {
        return uses;
    }

    Set<Class<? extends Composite>> getPublicComposites()
    {
        Set<Class<? extends Composite>> publicComposites = new HashSet<Class<? extends Composite>>();
        for( ModuleAssembly moduleAssembly : moduleAssemblies )
        {
            for( CompositeDeclaration compositeDeclaration : moduleAssembly.getCompositeDeclarations() )
            {
                if( compositeDeclaration.getVisibility() == Visibility.layer )
                {
                    for( Class<? extends Composite> compositeType : compositeDeclaration.getCompositeTypes() )
                    {
                        publicComposites.add( compositeType );
                    }
                }
            }
        }
        return publicComposites;
    }

    String getName()
    {
        return name;
    }
}
