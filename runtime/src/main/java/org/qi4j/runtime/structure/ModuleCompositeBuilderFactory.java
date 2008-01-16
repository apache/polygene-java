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
import org.qi4j.runtime.structure.TypeMapper;
import java.util.HashMap;
import java.io.Serializable;

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class ModuleCompositeBuilderFactory
    implements CompositeBuilderFactory, TypeMapper
{
    private ModuleInstance moduleInstance;
    private HashMap<Class, Class<? extends Composite>> pojoMap;

    public ModuleCompositeBuilderFactory( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
        pojoMap = new HashMap<Class, Class<? extends Composite>>();
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
        Class<? extends Composite> compositeType = pojoMap.get( pojoType );
        return pojoType.cast( newCompositeBuilder( compositeType ).newInstance() );
    }

    public void registerComposite( Class<? extends Composite> compositeType )
    {
        for( Class type : compositeType.getInterfaces())
        {
            if( type.equals( Serializable.class ) )
            {
            }
            else if( pojoMap.containsKey( type ) )
            {
                pojoMap.remove( type );
            }
            else
            {
                pojoMap.put( type, compositeType );
            }
        }
    }

    public void unregisterComposite( Class<? extends Composite> compositeType )
    {
        for( Class type : compositeType.getInterfaces())
        {
            pojoMap.remove( type );
        }
    }
    
}