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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationModelFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;
import org.qi4j.spi.structure.ApplicationModelSPI;

/**
 * Factory for Applications.
 */
public final class ApplicationModelFactoryImpl
    implements ApplicationModelFactory
{
    public ApplicationModelSPI newApplicationModel( ApplicationAssembly assembly )
        throws AssemblyException
    {
        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        List<LayerModel> layerModels = new ArrayList<LayerModel>();
        ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.name(), applicationAssembly.version(), applicationAssembly.mode(), applicationAssembly.metaInfo(), layerModels );
        Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<LayerAssembly, LayerModel>();
        Map<LayerAssembly, List<LayerModel>> mapUsedLayers = new HashMap<LayerAssembly, List<LayerModel>>();

        // Build all layers
        List<LayerAssemblyImpl> layerAssemblies = new ArrayList<LayerAssemblyImpl>( applicationAssembly.layerAssemblies() );
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            List<LayerModel> usedLayers = new ArrayList<LayerModel>();
            mapUsedLayers.put( layerAssembly, usedLayers );

            UsedLayersModel usedLayersModel = new UsedLayersModel( usedLayers );
            List<ModuleModel> moduleModels = new ArrayList<ModuleModel>();
            String name = layerAssembly.name();
            if( name == null )
            {
                throw new AssemblyException( "Layer must have name set" );
            }
            LayerModel layerModel = new LayerModel( name, layerAssembly.metaInfo(), usedLayersModel, moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.moduleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule() );
            }
            mapAssemblyModel.put( layerAssembly, layerModel );
            layerModels.add( layerModel );
        }

        // Populate used layer lists
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            Set<LayerAssembly> usesLayers = layerAssembly.uses();
            List<LayerModel> usedLayers = mapUsedLayers.get( layerAssembly );
            for( LayerAssembly usesLayer : usesLayers )
            {
                LayerModel layerModel = mapAssemblyModel.get( usesLayer );
                usedLayers.add( layerModel );
            }
        }

        // Bind model
        // This will resolve all dependencies
        try
        {
            applicationModel.bind();
        }
        catch( BindingException e )
        {
            throw new AssemblyException( e );
        }

        return applicationModel;
    }
}