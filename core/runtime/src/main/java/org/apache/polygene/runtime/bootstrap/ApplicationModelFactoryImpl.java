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

package org.apache.polygene.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.ApplicationModelFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.BindingException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.composite.CompositeMethodModel;
import org.apache.polygene.runtime.injection.InjectedFieldModel;
import org.apache.polygene.runtime.model.Binder;
import org.apache.polygene.runtime.model.Resolution;
import org.apache.polygene.runtime.structure.ApplicationModel;
import org.apache.polygene.runtime.structure.LayerModel;
import org.apache.polygene.runtime.structure.ModuleModel;
import org.apache.polygene.runtime.structure.UsedLayersModel;

/**
 * Factory for Applications.
 */
public final class ApplicationModelFactoryImpl
    implements ApplicationModelFactory
{
    @Override
    public ApplicationDescriptor newApplicationModel( ApplicationAssembly assembly )
    {
        AssemblyHelper helper = createAssemblyHelper( assembly );
        AssemblyMaps maps = new AssemblyMaps();
        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;

        List<LayerAssemblyImpl> layerAssemblies = new ArrayList<>( applicationAssembly.layerAssemblies() );
        List<LayerModel> layerModels = new ArrayList<>();

        buildAllLayers( helper, maps, layerAssemblies, layerModels );
        populateUsedLayerModels( maps, layerAssemblies );

        ApplicationModel applicationModel = buildApplicationModel( applicationAssembly, layerModels );
        bindApplicationModel( applicationModel );
        return applicationModel;
    }

    private AssemblyHelper createAssemblyHelper( ApplicationAssembly assembly )
    {
        if( assembly instanceof ApplicationAssemblyImpl )
        {
            ApplicationAssemblyImpl impl = (ApplicationAssemblyImpl) assembly;
            AssemblyHelper helper = impl.metaInfo().get( AssemblyHelper.class );
            if( helper != null )
            {
                return helper;
            }
        }
        return new AssemblyHelper();
    }

    private void buildAllLayers( AssemblyHelper helper, AssemblyMaps maps,
                                 List<LayerAssemblyImpl> layerAssemblies, List<LayerModel> layerModels )
    {
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            UsedLayersModel usedLayersModel = new UsedLayersModel( maps.usedLayersOf( layerAssembly ) );
            List<ModuleModel> moduleModels = new ArrayList<>();
            String name = layerAssembly.name();
            if( name == null )
            {
                throw new AssemblyException( "Layer must have name set" );
            }
            ActivatorsModel<Layer> layerActivators = new ActivatorsModel<>( layerAssembly.activators() );
            LayerModel layerModel = new LayerModel( name,
                                                    layerAssembly.metaInfo(),
                                                    usedLayersModel,
                                                    layerActivators,
                                                    moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.moduleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule( layerModel, helper ) );
            }
            maps.addModel( layerAssembly, layerModel );
            layerModels.add( layerModel );
        }
    }

    private void populateUsedLayerModels( AssemblyMaps maps, List<LayerAssemblyImpl> layerAssemblies )
    {
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            Set<LayerAssembly> usesLayers = layerAssembly.uses();
            List<LayerModel> usedLayers = maps.usedLayersOf( layerAssembly );
            for( LayerAssembly usesLayer : usesLayers )
            {
                usedLayers.add( maps.modelOf( usesLayer ) );
            }
        }
    }


    private ApplicationModel buildApplicationModel( ApplicationAssemblyImpl applicationAssembly,
                                                    List<LayerModel> layerModels )
    {
        return new ApplicationModel( applicationAssembly.name(),
                                     applicationAssembly.version(),
                                     applicationAssembly.mode(),
                                     applicationAssembly.metaInfo(),
                                     new ActivatorsModel<>( applicationAssembly.activators() ),
                                     layerModels );
    }

    private void bindApplicationModel( ApplicationModel applicationModel )
    {
        // This will resolve all dependencies
        try
        {
            applicationModel.accept( new BindingVisitor( applicationModel ) );
        }
        catch( BindingException e )
        {
            throw new AssemblyException( "Unable to bind: " + applicationModel, e );
        }
    }

    private static class AssemblyMaps
    {
        private final Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<>();
        private final Map<LayerModel, LayerAssembly> mapModelAssembly = new HashMap<>();
        private final Map<LayerAssembly, List<LayerModel>> mapUsedLayers = new HashMap<>();

        void addModel( LayerAssembly assembly, LayerModel model )
        {
            mapAssemblyModel.put( assembly, model );
            mapModelAssembly.put( model, assembly );
            usedLayersOf( assembly );
        }

        LayerAssembly assemblyOf( LayerModel model )
        {
            return mapModelAssembly.get( model );
        }

        LayerModel modelOf( LayerAssembly assembly )
        {
            return mapAssemblyModel.get( assembly );
        }

        List<LayerModel> usedLayersOf( LayerAssembly assembly )
        {
            if( !mapUsedLayers.containsKey( assembly ) )
            {
                mapUsedLayers.put( assembly, new ArrayList<>() );
            }
            return mapUsedLayers.get( assembly );
        }
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

        private BindingVisitor( ApplicationModel applicationModel )
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
                resolution = new Resolution( applicationModel, layer, module,
                                             objectDescriptor, compositeMethodModel, null );
            }
            else if( visited instanceof ModelDescriptor )
            {
                objectDescriptor = (ModelDescriptor) visited;
                resolution = new Resolution( applicationModel, layer, module,
                                             objectDescriptor, null, null );
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