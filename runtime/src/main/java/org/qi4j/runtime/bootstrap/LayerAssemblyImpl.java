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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyVisitor;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Assembly of a Layer. From here you can create more ModuleAssemblies for
 * the Layer that is being assembled. It is also here that you define
 * what other Layers this Layer is using by calling {@link LayerAssemblyImpl#uses(LayerAssembly)}.
 */
public final class LayerAssemblyImpl
    implements LayerAssembly, Serializable
{
    private ApplicationAssembly applicationAssembly;
    private List<ModuleAssemblyImpl> moduleAssemblies;
    private Set<LayerAssembly> uses;

    private String name;

    public LayerAssemblyImpl( ApplicationAssembly applicationAssembly, String name )
    {
        this.applicationAssembly = applicationAssembly;
        this.name = name;

        moduleAssemblies = new ArrayList<ModuleAssemblyImpl>();
        uses = new LinkedHashSet<LayerAssembly>();
    }

    public ModuleAssembly newModuleAssembly( String name )
    {
        ModuleAssemblyImpl moduleAssembly = new ModuleAssemblyImpl( this, name );
        moduleAssemblies.add( moduleAssembly );
        return moduleAssembly;
    }

    public ApplicationAssembly applicationAssembly()
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

    public void visit( AssemblyVisitor visitor )
    {
        visitor.visitLayer( this );
        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies )
        {
            moduleAssembly.visit( visitor );
        }
    }

    List<ModuleAssemblyImpl> getModuleAssemblies()
    {
        return moduleAssemblies;
    }

    Set<LayerAssembly> getUses()
    {
        return uses;
    }

    public String name()
    {
        return name;
    }

    @Override
    public final String toString()
    {
        return "LayerAssembly [" + name + "]";
    }
}
