/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.bootstrap.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;

/**
 * Provides declared {@link org.apache.polygene.api.structure.Layer} information that the {@link ApplicationBuilder} can use.
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
        StreamSupport.stream( layerNames.spliterator(), false )
                     .forEach( using::add );
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
