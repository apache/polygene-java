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
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.injection.provider.InjectionProviderFactoryStrategy;

/**
 * TODO
 */
public class ApplicationModel
{
    private String name;
    private List<LayerModel> layers;

    private InjectionProviderFactory ipf;

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

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( LayerModel layer : layers )
        {
            layer.visitDependencies( visitor );
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

    // Context
    public ApplicationInstance newInstance( Qi4jRuntime runtime )
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
