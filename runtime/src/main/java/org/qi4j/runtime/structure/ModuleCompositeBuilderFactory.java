/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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

import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.runtime.composite.CompositeContext;
import java.util.Map;

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class ModuleCompositeBuilderFactory
    implements CompositeBuilderFactory
{
    private ModuleInstance moduleInstance;
    private final Map<Class, Class<? extends Composite>> mapping;

    public ModuleCompositeBuilderFactory( ModuleInstance moduleInstance, Map<Class, Class<? extends Composite>> mapping )
    {
        this.moduleInstance = moduleInstance;
        this.mapping = mapping;
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        // Find which Module handles this Composite type
        ModuleInstance moduleInstance = this.moduleInstance.getModuleForPublicComposite( compositeType );

        // If no module handles this, then it could be a private Composite
        if( moduleInstance == null )
        {
            moduleInstance = this.moduleInstance;
        }

        // Get the Composite context
        CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );

        if( compositeContext == null )
        {
            throw new InvalidApplicationException( "Trying to create unregistered composite of type " + compositeType.getName() + " in module " + this.moduleInstance.getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getName() );
        }

        // Create a builder
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( moduleInstance, compositeContext );
        return builder;
    }

    public <T> T newComposite( Class<T> pojoType )
    {
        Class<? extends Composite> compositeType = mapping.get( pojoType );
        return pojoType.cast( newCompositeBuilder( compositeType ).newInstance() );
    }

}