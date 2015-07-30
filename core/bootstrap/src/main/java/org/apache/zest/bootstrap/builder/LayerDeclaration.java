/*
 * Copyright 2014 Niclas Hedhman.
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.bootstrap.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.functional.Iterables;

/**
 * Provides declared {@link org.apache.zest.api.structure.Layer} information that the {@link ApplicationBuilder} can use.
 */
public class LayerDeclaration
{
    private final String layerName;
    private final List<String> using = new ArrayList<>();
    private final Map<String, ModuleDeclaration> modules = new HashMap<>();
    private LayerAssembly layer;

    LayerDeclaration( String layerName )
    {
        this.layerName = layerName;
    }

    /**
     * Declare using layer.
     * @param layerName Used layer name
     * @return This Layer declaration
     */
    public LayerDeclaration using( String layerName )
    {
        this.using.add( layerName );
        return this;
    }

    /**
     * Declare using layers.
     * @param layerNames Used layers names
     * @return This Layer declaration
     */
    public LayerDeclaration using( Iterable<String> layerNames )
    {
        Iterables.addAll( using, layerNames );
        return this;
    }

    /**
     * Declare Module.
     * @param moduleName Name of the Module
     * @return Module declaration for the given name, new if did not already exists
     */
    public ModuleDeclaration withModule( String moduleName )
    {
        ModuleDeclaration module = modules.get( moduleName );
        if( module != null )
        {
            return module;
        }
        module = new ModuleDeclaration( moduleName );
        modules.put( moduleName, module );
        return module;
    }

    LayerAssembly createLayer( ApplicationAssembly application )
    {
        layer = application.layer( layerName );
        layer.setName( layerName );
        for( ModuleDeclaration module : modules.values() )
        {
            ModuleAssembly assembly = module.createModule( layer );
        }
        return layer;
    }

    void initialize( HashMap<String, LayerAssembly> createdLayers )
        throws AssemblyException
    {
        for( String uses : using )
        {
            LayerAssembly usedLayer = createdLayers.get( uses );
            layer.uses( usedLayer );
        }
        for( ModuleDeclaration module : modules.values() )
        {
            module.initialize();
        }
    }
}
