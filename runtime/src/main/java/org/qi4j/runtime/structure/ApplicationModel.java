/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.structure.Application;
import org.qi4j.runtime.bootstrap.migration.MigrationImpl;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.injection.provider.InjectionProviderFactoryStrategy;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.DescriptorVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
public final class ApplicationModel
    implements ApplicationModelSPI, ApplicationDescriptor, Serializable
{
    private final String name;
    private Application.Mode mode;
    private MetaInfo metaInfo;
    private final List<LayerModel> layers;
    private MigrationImpl migration;
    private final InjectionProviderFactory ipf;

    public ApplicationModel( String name, Application.Mode mode, MetaInfo metaInfo, List<LayerModel> layers, MigrationImpl migration )
    {
        this.name = name;
        this.mode = mode;
        this.metaInfo = metaInfo;
        this.layers = layers;
        this.migration = migration;
        ipf = new InjectionProviderFactoryStrategy();
    }

    public String name()
    {
        return name;
    }

    public Application.Mode mode()
    {
        return mode;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public String toURI()
    {
        return "urn:qi4j:model:application:" + name;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        for( LayerModel layer : layers )
        {
            layer.visitModel( modelVisitor );
        }
    }

    // Binding
    public void bind() throws BindingException
    {
        Resolution resolution = new Resolution( this, null, null, null, null, null );
        for( LayerModel layer : layers )
        {
            layer.bind( resolution );
        }
    }

    // SPI
    public void visitDescriptor( DescriptorVisitor visitor )
    {
        visitModel( new DescriptorModelVisitor( visitor ) );
    }

    public ApplicationInstance newInstance( Qi4jSPI runtime )
        throws InvalidApplicationException
    {
        List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
        ApplicationInstance applicationInstance = new ApplicationInstance( this, runtime, layerInstances, migration );

        // Create layer instances
        Map<LayerModel, LayerInstance> layerInstanceMap = new HashMap<LayerModel, LayerInstance>();
        Map<LayerModel, List<LayerInstance>> usedLayers = new HashMap<LayerModel, List<LayerInstance>>();
        for( LayerModel layer : layers )
        {
            List<LayerInstance> usedLayerInstances = new ArrayList<LayerInstance>();
            usedLayers.put( layer, usedLayerInstances );
            UsedLayersInstance usedLayersInstance = layer.usedLayers().newInstance( usedLayerInstances );
            LayerInstance layerInstance = layer.newInstance( applicationInstance, usedLayersInstance );
            layerInstances.add( layerInstance );
            layerInstanceMap.put( layer, layerInstance );
        }

        // Resolve used layer instances
        for( LayerModel layer : layers )
        {
            List<LayerInstance> usedLayerInstances = usedLayers.get( layer );
            for( LayerModel usedLayer : layer.usedLayers().layers() )
            {
                LayerInstance layerInstance = layerInstanceMap.get( usedLayer );
                if( layerInstance == null )
                {
                    throw new InvalidApplicationException( "Could not find used layer:" + usedLayer.name() );
                }
                usedLayerInstances.add( layerInstance );
            }
        }


        return applicationInstance;
    }

    public InjectionProviderFactory injectionProviderFactory()
    {
        return ipf;
    }
}
