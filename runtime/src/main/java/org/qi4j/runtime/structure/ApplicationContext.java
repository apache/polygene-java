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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.spi.structure.ApplicationBinding;

/**
 * TODO
 */
public final class ApplicationContext
{
    private ApplicationBinding applicationBinding;
    private Iterable<LayerContext> layerContexts;

    public ApplicationContext( ApplicationBinding applicationBinding, Iterable<LayerContext> layerContexts )
    {
        this.applicationBinding = applicationBinding;
        this.layerContexts = layerContexts;
    }

    public ApplicationBinding getApplicationBinding()
    {
        return applicationBinding;
    }

    public ApplicationInstance newApplicationInstance( String name )
    {
        if( name == null )
        {
            // Default instance name
            name = applicationBinding.getApplicationResolution().getApplicationModel().getName() + " instance";
        }

        List<LayerInstance> layerInstances = new ArrayList<LayerInstance>();
        for( LayerContext layerContext : layerContexts )
        {
            LayerInstance layerInstance = layerContext.newLayerInstance();
            layerInstances.add( layerInstance );
        }

        ApplicationInstance applicationInstance = new ApplicationInstance( this, layerInstances, name );
        return applicationInstance;
    }
}
