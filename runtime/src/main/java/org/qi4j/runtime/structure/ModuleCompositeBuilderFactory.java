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

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class ModuleCompositeBuilderFactory
    implements CompositeBuilderFactory
{
    private ModuleContext moduleContext;

    public ModuleCompositeBuilderFactory( ModuleContext moduleContext )
    {
        this.moduleContext = moduleContext;
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        // Find which Module handles this Composite type
        ModuleContext context = moduleContext.getModuleContext( compositeType );

        // Check if this Composite has been registered properly
        if( context == null )
        {
            throw new InvalidApplicationException( "Trying to create unregistered composite of type " + compositeType.getName() + " in module " + moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getName() );
        }

        // Get the Composite context
        CompositeContext compositeContext = context.getCompositeContext( compositeType );

        // Create a builder
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( context, compositeContext );
        return builder;
    }
}