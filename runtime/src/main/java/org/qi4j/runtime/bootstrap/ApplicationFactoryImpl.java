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
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.structure.Application;

/**
 * Factory for ApplicationContext.
 */
public final class ApplicationFactoryImpl
    implements ApplicationFactory
{
    private Qi4jSPI runtime;
    private ApplicationAssemblyFactory applicationAssemblyFactory;

    public ApplicationFactoryImpl()
    {
        this.runtime = new Qi4jRuntime();
        this.applicationAssemblyFactory = new ApplicationAssemblyFactoryImpl();
    }

    public Application newApplication( Assembler assembler )
        throws AssemblyException
    {
        return newApplication( new Assembler[][][]{ { { assembler } } } );
    }

    public Application newApplication( Assembler[][][] assemblers )
        throws AssemblyException
    {
        ApplicationAssembly applicationAssembly = applicationAssemblyFactory.newApplicationAssembly();
        applicationAssembly.setName( "Application" );

        // Build all layers bottom-up
        LayerAssembly below = null;
        for( int layer = assemblers.length - 1; layer >= 0; layer-- )
        {
            // Create Layer
            LayerAssembly lb = applicationAssembly.newLayerAssembly();
            lb.setName( "Layer " + ( layer + 1 ) );
            for( int module = 0; module < assemblers[ layer ].length; module++ )
            {
                // Create Module
                ModuleAssembly mb = lb.newModuleAssembly();
                mb.setName( "Module " + ( module + 1 ) );
                for( int assembly = 0; assembly < assemblers[ layer ][ module ].length; assembly++ )
                {
                    // Register Assembler
                    mb.addAssembler( assemblers[ layer ][ module ][ assembly ] );
                }
            }
            if( below != null )
            {
                lb.uses( below ); // Link layers
            }
            below = lb;
        }
        return newApplication( applicationAssembly );
    }

    public Application newApplication( ApplicationAssembly assembly )
        throws AssemblyException
    {
        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        List<LayerModel> layerModels = new ArrayList<LayerModel>();
        ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.getName(), layerModels );
        List<LayerAssemblyImpl> layerAssemblies = applicationAssembly.getLayerAssemblies();
        Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<LayerAssembly, LayerModel>();
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            Set<LayerAssembly> usesLayers = layerAssembly.getUses();
            List<LayerModel> usedLayers = new ArrayList<LayerModel>();
            for( LayerAssembly usesLayer : usesLayers )
            {
                usedLayers.add( mapAssemblyModel.get( usesLayer ) );
            }
            UsedLayersModel usedLayersModel = new UsedLayersModel( usedLayers );
            List<ModuleModel> moduleModels = new ArrayList<ModuleModel>();
            LayerModel layerModel = new LayerModel( layerAssembly.getName(), usedLayersModel, moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.getModuleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule() );
            }
            mapAssemblyModel.put( layerAssembly, layerModel );
            layerModels.add( layerModel );
        }

        try
        {
            applicationModel.bind();
        }
        catch( BindingException e )
        {
            throw new AssemblyException( e );
        }

        return applicationModel.newInstance( runtime );
    }
}
