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

package org.qi4j.runtime.structure;

import java.util.List;
import org.qi4j.service.Activatable;

/**
 * TODO
 */
public final class ApplicationInstance
    implements Activatable
{
    private ApplicationContext applicationContext;
    private List<LayerInstance> layerInstances;
    private String name;

    public ApplicationInstance( ApplicationContext applicationContext, List<LayerInstance> layerInstances, String name )
    {
        this.applicationContext = applicationContext;
        this.layerInstances = layerInstances;
        this.name = name;
    }

    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public List<LayerInstance> getLayerInstances()
    {
        return layerInstances;
    }

    public String getName()
    {
        return name;
    }

    public LayerInstance getLayerByName( String name )
    {
        for( LayerInstance layerInstance : layerInstances )
        {
            if( layerInstance.getLayerContext().getLayerBinding().getLayerResolution().getLayerModel().getName().equals( name ) )
            {
                return layerInstance;
            }
        }

        return null;
    }

    public void activate() throws Exception
    {
        for( LayerInstance layerInstance : layerInstances )
        {
            layerInstance.activate();
        }
    }

    public void passivate() throws Exception
    {
        for( int i = layerInstances.size() - 1; i >= 0; i-- )
        {
            layerInstances.get( i ).passivate();
        }
    }
}
