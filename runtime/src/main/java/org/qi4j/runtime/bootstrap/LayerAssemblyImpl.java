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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblyVisitor;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Assembly of a Layer. From here you can create more ModuleAssemblies for
 * the Layer that is being assembled. It is also here that you define
 * what other Layers this Layer is using by calling {@link LayerAssemblyImpl#uses(org.qi4j.bootstrap.LayerAssembly[])} .
 */
public final class LayerAssemblyImpl
    implements LayerAssembly, Serializable
{
    private ApplicationAssembly applicationAssembly;
    private HashMap<String, ModuleAssemblyImpl> moduleAssemblies;
    private Set<LayerAssembly> uses;

    private String name;
    private MetaInfo metaInfo = new MetaInfo();

    public LayerAssemblyImpl( ApplicationAssembly applicationAssembly, String name )
    {
        this.applicationAssembly = applicationAssembly;
        this.name = name;

        moduleAssemblies = new LinkedHashMap<String, ModuleAssemblyImpl>();
        uses = new LinkedHashSet<LayerAssembly>();
    }

    public ModuleAssembly moduleAssembly( String name )
    {
        if( name != null )
        {
            ModuleAssemblyImpl existing = moduleAssemblies.get( name );
            if( existing != null )
            {
                return existing;
            }
        }
        ModuleAssemblyImpl moduleAssembly = new ModuleAssemblyImpl( this, name );
        moduleAssemblies.put( name, moduleAssembly );
        return moduleAssembly;
    }

    public ApplicationAssembly applicationAssembly()
    {
        return applicationAssembly;
    }

    public LayerAssembly setName( String name )
    {
        this.name = name;
        return this;
    }

    public LayerAssembly setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public LayerAssembly uses( LayerAssembly... layerAssembly )
        throws IllegalArgumentException
    {
        uses.addAll( Arrays.asList( layerAssembly ) );
        return this;
    }

    public void visit( AssemblyVisitor visitor )
        throws AssemblyException
    {
        visitor.visitLayer( this );
        for( ModuleAssemblyImpl moduleAssembly : moduleAssemblies.values() )
        {
            moduleAssembly.visit( visitor );
        }
    }

    Collection<ModuleAssemblyImpl> moduleAssemblies()
    {
        return moduleAssemblies.values();
    }

    Set<LayerAssembly> uses()
    {
        return uses;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
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
