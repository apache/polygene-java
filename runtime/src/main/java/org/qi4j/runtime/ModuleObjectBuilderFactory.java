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
package org.qi4j.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.ObjectBuilder;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.runtime.composite.ObjectBinder;
import org.qi4j.runtime.composite.ObjectResolver;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.dependency.ResolutionContext;

/**
 * Default implementation of ObjectBuilderFactory
 */
public final class ModuleObjectBuilderFactory
    implements ObjectBuilderFactory
{
    private Map<Class, ObjectBinding> objectBindings;
    private ObjectModelFactory objectModelFactory;
    private InstanceFactory instanceFactory;
    private ObjectResolver objectResolver;
    private ObjectBinder objectBinder;
    private ModuleContext moduleContext;

    public ModuleObjectBuilderFactory( ModuleContext moduleContext, Qi4jRuntime runtime )
    {
        this.moduleContext = moduleContext;
        this.objectModelFactory = runtime.getObjectModelFactory();
        this.instanceFactory = runtime.getInstanceFactory();
        this.objectResolver = runtime.getObjectResolver();
        this.objectBinder = runtime.getObjectBinder();

        objectBindings = new ConcurrentHashMap<Class, ObjectBinding>();
    }

    public <T> ObjectBuilder<T> newObjectBuilder( Class<T> type )
    {
        try
        {
            ObjectBinding objectBinding = getObjectBinding( type );
            ObjectBuilder<T> builder = new ObjectBuilderImpl<T>( objectBinding, moduleContext, instanceFactory );
            return builder;
        }
        catch( InvalidInjectionException e )
        {
            throw new CompositeInstantiationException( "Could not resolve dependencies", e );
        }
    }

    // Private ------------------------------------------------------
    private ObjectBinding getObjectBinding( Class type )
        throws InvalidInjectionException
    {
        ObjectBinding objectBinding = objectBindings.get( type );
        if( objectBinding == null )
        {
            ObjectModel objectModel = objectModelFactory.newObjectModel( type );
            ResolutionContext resolutionContext = new ResolutionContext( objectModel, null, moduleContext.getModuleBinding().getModuleResolution().getModuleModel(), null, null );
            ObjectResolution objectResolution = objectResolver.resolveObjectModel( resolutionContext );
            objectBinding = objectBinder.bindObject( objectResolution );
            objectBindings.put( type, objectBinding );
        }
        return objectBinding;
    }
}