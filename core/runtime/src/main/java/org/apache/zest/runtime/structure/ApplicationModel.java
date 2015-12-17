/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.LayerDescriptor;
import org.apache.zest.bootstrap.ZestRuntime;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.runtime.activation.ActivatorsInstance;
import org.apache.zest.runtime.activation.ActivatorsModel;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.injection.provider.InjectionProviderFactoryStrategy;

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
    public ApplicationInstance newInstance( ZestAPI runtime, Object... importedServiceInstances )
        throws InvalidApplicationException
    {
        MetaInfo instanceMetaInfo = new MetaInfo( metaInfo );
        for( Object importedServiceInstance : importedServiceInstances )
        {
            instanceMetaInfo.set( importedServiceInstance );
        }

        ApplicationInstance applicationInstance = new ApplicationInstance( this, (ZestRuntime) runtime, instanceMetaInfo );

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
