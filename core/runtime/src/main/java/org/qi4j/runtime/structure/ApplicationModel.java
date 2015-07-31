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
package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.Qi4j;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.injection.provider.InjectionProviderFactoryStrategy;

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
    public ApplicationInstance newInstance( Qi4j runtime, Object... importedServiceInstances )
        throws InvalidApplicationException
    {
        MetaInfo instanceMetaInfo = new MetaInfo( metaInfo );
        for( Object importedServiceInstance : importedServiceInstances )
        {
            instanceMetaInfo.set( importedServiceInstance );
        }

        ApplicationInstance applicationInstance = new ApplicationInstance( this, (Qi4jRuntime) runtime, instanceMetaInfo );

        // Create layer instances
        Map<LayerModel, LayerInstance> layerInstanceMap = new HashMap<>();
        Map<LayerModel, List<LayerInstance>> usedLayers = new HashMap<>();
        for( LayerModel layer : layers )
        {
            List<LayerInstance> usedLayerInstances = new ArrayList<>();
            usedLayers.put( layer, usedLayerInstances );
            UsedLayersInstance usedLayersInstance = layer.usedLayers().newInstance( usedLayerInstances );
            LayerInstance layerInstance = layer.newInstance( applicationInstance, usedLayersInstance );
            applicationInstance.addLayer( layerInstance );
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

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "ApplicationModel" );
        sb.append( "{name='" ).append( name ).append( '\'' );
        sb.append( ", version='" ).append( version ).append( '\'' );
        sb.append( ", mode=" ).append( mode );
        sb.append( '}' );
        return sb.toString();
    }
}
