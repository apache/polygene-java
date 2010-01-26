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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.AssemblyVisitor;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * The representation of an entire application. From
 * this you can set information about the application
 * and create LayerAssemblies.
 */
public final class ApplicationAssemblyImpl
    implements ApplicationAssembly, Serializable
{
    private Map<String, LayerAssemblyImpl> layerAssemblies = new LinkedHashMap<String, LayerAssemblyImpl>();
    private String name = "Application";
    private String version = "1.0"; // Default version
    private Application.Mode mode;
    private MetaInfo metaInfo = new MetaInfo();

    public ApplicationAssemblyImpl()
    {
        mode = Application.Mode.valueOf( System.getProperty( "mode", "production" ) );
    }

    public LayerAssembly layerAssembly( String name )
    {
        if( name != null )
        {
            LayerAssemblyImpl existing = layerAssemblies.get( name );
            if( existing != null )
            {
                return existing;
            }
        }
        LayerAssemblyImpl layerAssembly = new LayerAssemblyImpl( this, name );
        layerAssemblies.put( name, layerAssembly );
        return layerAssembly;
    }

    public ApplicationAssembly setName( String name )
    {
        this.name = name;
        return this;
    }

    public ApplicationAssembly setVersion( String version )
    {
        this.version = version;
        return this;
    }

    public ApplicationAssembly setMode( Application.Mode mode )
    {
        this.mode = mode;
        return this;
    }

    public ApplicationAssembly setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public void visit( AssemblyVisitor visitor )
        throws AssemblyException
    {
        visitor.visitApplication( this );
        for( LayerAssemblyImpl layerAssembly : layerAssemblies.values() )
        {
            layerAssembly.visit( visitor );
        }
    }

    public Collection<LayerAssemblyImpl> layerAssemblies()
    {
        return layerAssemblies.values();
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public String name()
    {
        return name;
    }

    public String version()
    {
        return version;
    }

    public Application.Mode mode()
    {
        return mode;
    }
}
