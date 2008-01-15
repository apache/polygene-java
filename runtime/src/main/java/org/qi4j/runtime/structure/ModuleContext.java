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

import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.ObjectContext;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ModuleContext
{
    private ModuleBinding moduleBinding;
    private Map<Class, ObjectContext> objectContexts;
    private Map<Class<? extends Composite>, CompositeContext> compositeContexts;

    public ModuleContext( ModuleBinding moduleBinding, Map<Class<? extends Composite>, CompositeContext> compositeContexts, Map<Class, ObjectContext> instantiableObjectContexts, Map<Class<? extends Composite>, ModuleContext> moduleContexts )
    {
        this.moduleBinding = moduleBinding;
        objectContexts = instantiableObjectContexts;

        this.compositeContexts = compositeContexts;

    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }

    public ModuleInstance newModuleInstance( Map<Class<? extends Composite>, ModuleInstance> modulesForPublicComposites, Map<Class, ModuleInstance> modulesForPublicObjects )
    {

        ModuleInstance moduleInstance = new ModuleInstance( this, modulesForPublicComposites, modulesForPublicObjects );
        return moduleInstance;
    }

    public CompositeContext getCompositeContext( Class<? extends Composite> compositeType )
    {
        return compositeContexts.get( compositeType );
    }

    public ObjectContext getObjectContext( Class objectType )
    {
        return objectContexts.get( objectType );
    }

    public Map<Class<? extends Composite>, CompositeContext> getCompositeContexts()
    {
        return compositeContexts;
    }
}
