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
import org.qi4j.runtime.ModuleCompositeBuilderFactory;
import org.qi4j.runtime.ModuleObjectBuilderFactory;
import org.qi4j.runtime.Qi4jRuntime;

/**
 * TODO
 */
public class ModuleInstance
{
    private ModuleContext moduleContext;
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;

    public ModuleInstance( ModuleContext moduleContext, Qi4jRuntime runtime )
    {
        this.moduleContext = moduleContext;
        compositeBuilderFactory = new ModuleCompositeBuilderFactory( moduleContext );
        objectBuilderFactory = new ModuleObjectBuilderFactory( moduleContext, runtime );
    }

    public ModuleContext getModuleContext()
    {
        return moduleContext;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }
}
