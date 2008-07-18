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

package org.qi4j.runtime.bootstrap;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import static org.qi4j.composite.NullArgumentException.validateNotNull;

/**
 * Assembly of a Layer. From here you can create more ModuleAssemblies for
 * the Layer that is being assembled. It is also here that you define
 * what other Layers this Layer is using by calling {@link LayerAssemblyImpl#uses(LayerAssembly)}.
 */
public final class LayerAssemblyImpl
    implements LayerAssembly
{
    private ApplicationAssembly applicationAssembly;
    private List<ModuleAssemblyImpl> moduleAssemblies;
    private Set<LayerAssembly> uses;

    private String name;

    public LayerAssemblyImpl( ApplicationAssembly applicationAssembly )
    {
        this.applicationAssembly = applicationAssembly;

        moduleAssemblies = new ArrayList<ModuleAssemblyImpl>();
        uses = new LinkedHashSet<LayerAssembly>();
    }

    public ModuleAssembly newModuleAssembly()
    {
        ModuleAssemblyImpl moduleAssembly = new ModuleAssemblyImpl( this );
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
        throws IllegalArgumentException
    {
        validateNotNull( "layerAssembly", layerAssembly );
        uses.add( layerAssembly );
    }

    List<ModuleAssemblyImpl> getModuleAssemblies()
    {
        return moduleAssemblies;
    }

    Set<LayerAssembly> getUses()
    {
        return uses;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public final String toString()
    {
        return "LayerAssembly [" + name + "]";
    }
}
