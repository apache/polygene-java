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
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationModelFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;

/**
 * Factory for Applications.
 */
public final class ApplicationModelFactoryImpl
    implements ApplicationModelFactory
{
    public ApplicationDescriptor newApplicationModel( ApplicationAssembly assembly )
        throws AssemblyException
    {
        AssemblyHelper helper = new AssemblyHelper();

        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        List<LayerModel> layerModels = new ArrayList<LayerModel>();
        final ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.name(),
                                                                        applicationAssembly.version(),
                                                                        applicationAssembly.mode(),
                                                                        applicationAssembly.metaInfo(),
                                                                        layerModels );
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
                moduleModels.add( moduleAssembly.assembleModule( helper ) );
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
//            applicationModel.bind();
            applicationModel.accept( new BindingVisitor( applicationModel ) );
        }
        catch( BindingException e )
        {
            throw new AssemblyException( e );
        }

        return applicationModel;
    }

    private static class BindingVisitor
        implements HierarchicalVisitor<Object, Object, BindingException>
    {
        private LayerModel layer;
        private ModuleModel module;
        private ModelDescriptor objectDescriptor;
        private CompositeMethodModel compositeMethodModel;

        private Resolution resolution;
        private final ApplicationModel applicationModel;

        public BindingVisitor( org.qi4j.runtime.structure.ApplicationModel applicationModel )
        {
            this.applicationModel = applicationModel;
        }

        @Override
        public boolean visitEnter( Object visited )
            throws BindingException
        {
            if( visited instanceof Binder )
            {
                Binder binder = (Binder) visited;
                binder.bind( resolution );

                return false;
            }
            else if( visited instanceof CompositeMethodModel )
            {
                compositeMethodModel = (CompositeMethodModel) visited;
                resolution = new Resolution( applicationModel, layer, module, objectDescriptor, compositeMethodModel, null );
            }
            else if( visited instanceof ModelDescriptor )
            {
                objectDescriptor = (ModelDescriptor) visited;
                resolution = new Resolution( applicationModel, layer, module, objectDescriptor, null, null );
            }
            else if( visited instanceof InjectedFieldModel )
            {
                InjectedFieldModel fieldModel = (InjectedFieldModel) visited;
                fieldModel.bind( new Resolution( applicationModel, layer, module,
                                                 objectDescriptor, compositeMethodModel, fieldModel.field() ) );
            }
            else if( visited instanceof ModuleModel )
            {
                module = (ModuleModel) visited;
            }
            else if( visited instanceof LayerModel )
            {
                layer = (LayerModel) visited;
            }

            return true;
        }

        @Override
        public boolean visitLeave( Object visited )
            throws BindingException
        {
            return true;
        }

        @Override
        public boolean visit( Object visited )
            throws BindingException
        {
            if( visited instanceof Binder )
            {
                ( (Binder) visited ).bind( resolution );
            }
            return true;
        }
    }
}