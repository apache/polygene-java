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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.injection.provider.InjectionProviderFactoryStrategy;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationDescriptor;

/**
 * TODO
 */
public final class ApplicationModel
    implements ApplicationDescriptor
{
    private final String name;
    private final List<LayerModel> layers;
    private final InjectionProviderFactory ipf;

    public ApplicationModel( String name, List<LayerModel> layers )
    {
        this.name = name;
        this.layers = layers;
        ipf = new InjectionProviderFactoryStrategy();
    }

    public String name()
    {
        return name;
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
        Resolution resolution = new Resolution( this, null, null, null, null, null, null );
        for( LayerModel layer : layers )
        {
            layer.bind( resolution );
        }
    }

    // Context
    public ApplicationInstance newInstance( Qi4jSPI runtime )
    {
        List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
        ApplicationInstance applicationInstance = new ApplicationInstance( this, runtime, layerInstances );

        Map<LayerModel, LayerInstance> layerInstanceMap = new HashMap<LayerModel, LayerInstance>();
        for( LayerModel layer : layers )
        {
            UsedLayersInstance usedLayersInstance = layer.usedLayers().newInstance( layerInstanceMap );
            LayerInstance layerInstance = layer.newInstance( applicationInstance, usedLayersInstance );
            layerInstances.add( layerInstance );
            layerInstanceMap.put( layer, layerInstance );
        }

        return applicationInstance;
    }

    public InjectionProviderFactory injectionProviderFactory()
    {
        return ipf;
    }
}
