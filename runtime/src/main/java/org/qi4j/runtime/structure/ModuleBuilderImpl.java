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

import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.structure.Assembly;
import org.qi4j.structure.ModuleBuilder;

/**
 * TODO
 */
public class ModuleBuilderImpl
    implements ModuleBuilder
{
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private Set<Assembly> assemblies = new LinkedHashSet<Assembly>();

    public ModuleBuilderImpl( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory )
    {
        this.compositeBuilderFactory = compositeBuilderFactory;
        this.objectBuilderFactory = objectBuilderFactory;
    }

    public void addAssembly( Assembly assembly )
    {
        assemblies.add( assembly );
    }

    ModuleImpl newModule()
    {
        return new ModuleImpl( assemblies, compositeBuilderFactory, objectBuilderFactory );
    }
}
