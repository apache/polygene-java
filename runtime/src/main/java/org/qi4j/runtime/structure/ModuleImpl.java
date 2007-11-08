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

package org.qi4j.runtime.structure;

import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.structure.Assembly;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public class ModuleImpl
    implements Module
{
    Layer layer;
    Iterable<Assembly> assemblies;
    CompositeBuilderFactory compositeBuilderFactory;
    ObjectBuilderFactory objectBuilderFactory;

    public ModuleImpl( Iterable<Assembly> assemblies, CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory )
    {
        this.assemblies = assemblies;
        this.compositeBuilderFactory = compositeBuilderFactory;
        this.objectBuilderFactory = objectBuilderFactory;
    }

    public Layer getLayer()
    {
        return layer;
    }

    public Iterable<Assembly> getAssemblies()
    {
        return assemblies;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    void setLayer( Layer layer )
    {
        this.layer = layer;
    }
}
