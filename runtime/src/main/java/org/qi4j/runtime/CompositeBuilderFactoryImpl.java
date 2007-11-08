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
import org.qi4j.Composite;
import org.qi4j.CompositeBuilder;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.model.CompositeModel;
import org.qi4j.runtime.resolution.CompositeModelResolver;
import org.qi4j.runtime.resolution.CompositeResolution;

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class CompositeBuilderFactoryImpl
    implements CompositeBuilderFactory
{
    private Map<Class<? extends Composite>, CompositeContextImpl> objectContexts;
    private CompositeModelFactory compositeModelFactory;
    private InstanceFactory instanceFactory;
    private CompositeModelResolver compositeModelResolver;

    public CompositeBuilderFactoryImpl( InstanceFactory instanceFactory, CompositeModelFactory compositeModelFactory, CompositeModelResolver compositeModelResolver )
    {
        this.instanceFactory = instanceFactory;
        this.compositeModelFactory = compositeModelFactory;
        this.compositeModelResolver = compositeModelResolver;

        objectContexts = new ConcurrentHashMap<Class<? extends Composite>, CompositeContextImpl>();
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        CompositeContextImpl<T> context = getCompositeContext( compositeType );
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( context, instanceFactory );
        return builder;
    }

    private <T extends Composite> CompositeContextImpl<T> getCompositeContext( Class<T> compositeType )
    {
        CompositeContextImpl<T> context = objectContexts.get( compositeType );
        if( context == null )
        {
            CompositeModel<T> model = compositeModelFactory.newCompositeModel( compositeType );
            CompositeResolution<T> resolution = compositeModelResolver.resolveCompositeModel( model );
            context = new CompositeContextImpl<T>( resolution, this, instanceFactory );
            objectContexts.put( compositeType, context );
        }
        return context;
    }
}