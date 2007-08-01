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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.spi.MixinDependencyResolver;

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class CompositeBuilderFactoryImpl
    implements CompositeBuilderFactory
{
    private Map<CompositeModel, CompositeContextImpl> objectContexts;
    private List<DependencyResolver> resolvers;
    private CompositeModelFactory modelFactory;

    public CompositeBuilderFactoryImpl()
    {
        this( null, null );
    }

    public CompositeBuilderFactoryImpl( List<DependencyResolver> resolvers )
    {
        this( null, resolvers );
    }

    public CompositeBuilderFactoryImpl( DependencyResolver resolver )
    {
        this( null, Arrays.asList( resolver, new MixinDependencyResolver()));
    }

    public CompositeBuilderFactoryImpl( CompositeModelFactory compositeModelFactory, List<DependencyResolver> resolvers )
    {
        if( compositeModelFactory == null )
        {
            compositeModelFactory = new CompositeModelFactoryImpl();
        }
        this.modelFactory = compositeModelFactory;
        if( resolvers == null )
        {
            MixinDependencyResolver mixinResolver = new MixinDependencyResolver();
            resolvers = new ArrayList<DependencyResolver>();
            resolvers.add( mixinResolver );
        }
        this.resolvers = resolvers;
        objectContexts = new ConcurrentHashMap<CompositeModel, CompositeContextImpl>();
    }

    public <T extends Composite> T newInstance( Class<T> aCompositeClass )
        throws CompositeInstantiationException
    {
        // Ensure that given class extends Composite
        if( !Composite.class.isAssignableFrom( aCompositeClass ) )
        {
            throw new CompositeInstantiationException( "Class " + aCompositeClass.getName() + " does not extend " + Composite.class.getName() );
        }

        // Instantiate proxy for given composite interface
        try
        {
            CompositeModel compositeModel = modelFactory.getCompositeModel( aCompositeClass );
            CompositeContextImpl context = getCompositeContext( compositeModel );

            CompositeInvocationHandler handler = new RegularCompositeInvocationHandler( context );
            ClassLoader proxyClassloader = aCompositeClass.getClassLoader();
            Class[] interfaces = new Class[]{ aCompositeClass };
            return aCompositeClass.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            System.out.println( e );
            e.printStackTrace();
            throw new CompositeInstantiationException( e );
        }
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        CompositeModel compositeModel = modelFactory.getCompositeModel( compositeType );
        CompositeContextImpl context = getCompositeContext( compositeModel );
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( context );
        return builder;
    }

    public List<DependencyResolver> getDependencyResolvers()
    {
        return resolvers;
    }

    // Private ------------------------------------------------------
    private <T extends Composite> CompositeContextImpl getCompositeContext( CompositeModel<T> compositeModel )
    {
        CompositeContextImpl<T> context = objectContexts.get( compositeModel );
        if( context == null )
        {
            context = new CompositeContextImpl<T>( compositeModel, modelFactory, this );
            objectContexts.put( compositeModel, context );
        }
        return context;
    }
}