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

package org.apache.zest.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationModelFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.BindingException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.runtime.activation.ActivatorsModel;
import org.apache.zest.runtime.composite.CompositeMethodModel;
import org.apache.zest.runtime.injection.InjectedFieldModel;
import org.apache.zest.runtime.model.Binder;
import org.apache.zest.runtime.model.Resolution;
import org.apache.zest.runtime.structure.ApplicationModel;
import org.apache.zest.runtime.structure.LayerModel;
import org.apache.zest.runtime.structure.ModuleModel;
import org.apache.zest.runtime.structure.UsedLayersModel;

/**
 * Factory for Applications.
 */
public final class ApplicationModelFactoryImpl
    implements ApplicationModelFactory
{
    @Override
    public ApplicationDescriptor newApplicationModel( ApplicationAssembly assembly )
        throws AssemblyException
    {
        AssemblyHelper helper = createAssemblyHelper( assembly );

        ApplicationAssemblyImpl applicationAssembly = (ApplicationAssemblyImpl) assembly;
        ActivatorsModel<Application> applicationActivators = new ActivatorsModel<>( applicationAssembly.activators() );
        List<LayerModel> layerModels = new ArrayList<>();
        final ApplicationModel applicationModel = new ApplicationModel( applicationAssembly.name(),
                                                                        applicationAssembly.version(),
                                                                        applicationAssembly.mode(),
                                                                        applicationAssembly.metaInfo(),
                                                                        applicationActivators,
                                                                        layerModels );
        Map<LayerAssembly, LayerModel> mapAssemblyModel = new HashMap<>();
        Map<LayerAssembly, List<LayerModel>> mapUsedLayers = new HashMap<>();

        // Build all layers
        List<LayerAssemblyImpl> layerAssemblies = new ArrayList<>( applicationAssembly.layerAssemblies() );
        for( LayerAssemblyImpl layerAssembly : layerAssemblies )
        {
            List<LayerModel> usedLayers = new ArrayList<>();
            mapUsedLayers.put( layerAssembly, usedLayers );

            UsedLayersModel usedLayersModel = new UsedLayersModel( usedLayers );
            List<ModuleModel> moduleModels = new ArrayList<>();
            String name = layerAssembly.name();
            if( name == null )
            {
                throw new AssemblyException( "Layer must have name set" );
            }
            ActivatorsModel<Layer> layerActivators = new ActivatorsModel<>( layerAssembly.activators() );
            LayerModel layerModel = new LayerModel( name, layerAssembly.metaInfo(), usedLayersModel, layerActivators, moduleModels );

            for( ModuleAssemblyImpl moduleAssembly : layerAssembly.moduleAssemblies() )
            {
                moduleModels.add( moduleAssembly.assembleModule( layerModel, helper ) );
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
            throw new AssemblyException( "Unable to bind: " + applicationModel, e );
        }

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