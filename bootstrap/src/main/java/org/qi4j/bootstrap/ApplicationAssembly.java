/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.bootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class ApplicationAssembly
{
    private List<LayerAssembly> layerAssemblies = new ArrayList<LayerAssembly>();
    private String name;

    public LayerAssembly newLayerBuilder()
    {
        LayerAssembly layerAssembly = new LayerAssembly( this );
        layerAssemblies.add( layerAssembly );
        return layerAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    List<LayerAssembly> getLayerAssemblies()
    {
        return layerAssemblies;
    }

    String getName()
    {
        return name;
    }
}
