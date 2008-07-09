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

package org.qi4j.runtime.bootstrap;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * The representation of an entire application. From
 * this you can set information about the application
 * and create LayerAssemblies.
 */
public final class ApplicationAssemblyImpl
    implements ApplicationAssembly
{
    private List<LayerAssemblyImpl> layerAssemblies = new ArrayList<LayerAssemblyImpl>();
    private String name = "Application";

    public LayerAssembly newLayerAssembly()
    {
        LayerAssemblyImpl layerAssembly = new LayerAssemblyImpl( this );
        layerAssemblies.add( layerAssembly );
        return layerAssembly;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<LayerAssemblyImpl> getLayerAssemblies()
    {
        return layerAssemblies;
    }

    public String getName()
    {
        return name;
    }
}
