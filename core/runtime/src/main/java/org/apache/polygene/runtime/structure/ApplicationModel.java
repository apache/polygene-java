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
package org.apache.polygene.runtime.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.InvalidApplicationException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.bootstrap.PolygeneRuntime;
import org.apache.polygene.runtime.activation.ActivatorsInstance;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.injection.InjectionProviderFactory;
import org.apache.polygene.runtime.injection.provider.InjectionProviderFactoryStrategy;

/**
 * JAVADOC
 */
public final class ApplicationModel
    implements ApplicationDescriptor
{
    private final String name;
    private final String version;
    private final Application.Mode mode;
    private final MetaInfo metaInfo;
    private final ActivatorsModel<Application> activatorsModel;
    private final List<LayerModel> layers;
    private final InjectionProviderFactory ipf;

    public ApplicationModel( String name,
                             String version,
                             Application.Mode mode,
                             MetaInfo metaInfo,
                             ActivatorsModel<Application> activatorsModel,
                             List<LayerModel> layers
    )
    {
        this.name = name;
        this.version = version;
        this.mode = mode;
        this.metaInfo = metaInfo;
        this.activatorsModel = activatorsModel;
        this.layers = layers;
        ipf = new InjectionProviderFactoryStrategy( metaInfo );
    }

    @Override
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

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public ActivatorsInstance<Application> newActivatorsInstance()
        throws ActivationException
    {
        return new ActivatorsInstance<>( activatorsModel.newInstances() );
    }

    // SPI
    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( activatorsModel.accept( visitor ) )
            {
                for( LayerModel layer : layers )
                {
                    if( !layer.accept( visitor ) )
                    {
                        break;
                    }
                }
            }
        }
        return visitor.visitLeave( this );
    }

    @Override
    public ApplicationInstance newInstance( PolygeneAPI runtime, Object... importedServiceInstances )
        throws InvalidApplicationException
    {
        MetaInfo instanceMetaInfo = new MetaInfo( metaInfo );
        for( Object importedServiceInstance : importedServiceInstances )
        {
            instanceMetaInfo.set( importedServiceInstance );
        }

        ApplicationInstance applicationInstance = new ApplicationInstance( this, (PolygeneRuntime) runtime, instanceMetaInfo );

        // Create layer instances
        Map<LayerDescriptor, LayerDescriptor> layerInstanceMap = new HashMap<>();
        Map<LayerDescriptor, List<LayerDescriptor>> usedLayers = new HashMap<>();
        for( LayerModel layer : layers )
        {
            List<LayerDescriptor> usedLayerInstances = new ArrayList<>();
            usedLayers.put( layer, usedLayerInstances );
            UsedLayersInstance usedLayersInstance = layer.usedLayers().newInstance( usedLayerInstances );
            LayerInstance layerInstance = layer.newInstance( applicationInstance );
            applicationInstance.addLayer( layerInstance );
            layerInstanceMap.put( layer, layerInstance.descriptor() );
        }

        // Resolve used layer instances
        for( LayerModel layer : layers )
        {
            List<LayerDescriptor> usedLayerInstances = usedLayers.get( layer );
            layer.usedLayers().layers().forEach(
                usedLayer ->
                {
                    LayerDescriptor layerDescriptor = layerInstanceMap.get( usedLayer );
                    if( layerDescriptor == null )
                    {
                        throw new InvalidApplicationException( "Could not find used layer:" + usedLayer
                            .name() );
                    }
                    usedLayerInstances.add( layerDescriptor );
                } );
        }

        return applicationInstance;
    }

    public InjectionProviderFactory injectionProviderFactory()
    {
        return ipf;
    }

    @Override
    public String toString()
    {
        return "ApplicationModel" +
               "{name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", mode=" + mode +
               '}';
    }
}
