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
import org.qi4j.composite.CompositeNotRegisteredException;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.runtime.composite.CompositeContext;

/**
 * Default implementation of CompositeBuilderFactory
 */
public class ModuleCompositeBuilderFactory
    implements CompositeBuilderFactory
{
    private ModuleInstance moduleInstance;

    public ModuleCompositeBuilderFactory( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
    }

    public <T> CompositeBuilder<T> newCompositeBuilder( Class<T> mixinType )
    {
        validateNotNull( "mixinType", mixinType );
        Class<? extends Composite> compositeType = moduleInstance.findCompositeType( mixinType );
        // Find which Module handles this Composite type
        ModuleInstance compositeModuleInstance = moduleInstance.findModuleForCompositeType( compositeType );

        // Get the Composite context
        ModuleContext context = compositeModuleInstance.moduleContext();
        CompositeContext compositeContext = context.getCompositeContext( compositeType );

        if( compositeContext == null )
        {
            throw new CompositeNotRegisteredException( compositeType, compositeModuleInstance.module() );
        }

        return createBuilder( compositeModuleInstance, compositeContext );
    }

    public <T> T newComposite( Class<T> mixinType )
    {
        return newCompositeBuilder( mixinType ).newInstance();
    }

    protected <T> CompositeBuilder<T> createBuilder( ModuleInstance moduleInstance, CompositeContext compositeContext )
    {
        // Create a builder
        return new CompositeBuilderImpl<T>( moduleInstance, compositeContext );
    }
}