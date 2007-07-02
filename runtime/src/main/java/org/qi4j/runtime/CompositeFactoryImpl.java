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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeCastException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.spi.DefaultDependencyResolver;

/**
 * Default implementation of CompositeFactory
 */
public final class CompositeFactoryImpl
    implements CompositeFactory
{
    private FragmentFactory fragmentFactory;
    private Map<Class, CompositeModel> composites;
    private Map<CompositeModel, CompositeContextImpl> objectContexts;
    private List<DependencyResolver> resolvers;

    public CompositeFactoryImpl()
    {
        this( new FragmentFactoryImpl(), null );
    }

    public CompositeFactoryImpl( FragmentFactory aFragmentFactory, List<DependencyResolver> resolvers )
    {
        fragmentFactory = aFragmentFactory;
        if( resolvers == null )
        {
            DefaultDependencyResolver defaultResolver = new DefaultDependencyResolver();
            resolvers = new ArrayList<DependencyResolver>();
            resolvers.add( defaultResolver );
        }
        this.resolvers = resolvers;
        objectContexts = new ConcurrentHashMap<CompositeModel, CompositeContextImpl>();
        composites = new ConcurrentHashMap<Class, CompositeModel>();
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
            CompositeModel compositeModel = getCompositeModel( aCompositeClass );
            CompositeContextImpl context = getCompositeContext( compositeModel );

            CompositeInvocationHandler handler = new CompositeInvocationHandler( context );
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

    public <T extends Composite> T cast( Class<T> aCompositeClass, Composite anObject )
    {
        if( aCompositeClass.isInstance( anObject ) )
        {
            return aCompositeClass.cast( anObject );
        }
        CompositeModel model = getCompositeModel( anObject );
        Class existingCompositeClass = model.getCompositeClass();
        if( ! existingCompositeClass.isAssignableFrom( aCompositeClass ) )
        {
            throw new CompositeCastException(existingCompositeClass.getName() + " is not a super-type of " + aCompositeClass.getName() );
        }
        
        if( anObject instanceof Proxy )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( anObject );
            if( handler instanceof ProxyReferenceInvocationHandler )
            {
                // Get real handler
                handler = Proxy.getInvocationHandler( ( (ProxyReferenceInvocationHandler) handler ).getProxy() );
            }
            if( handler instanceof CompositeInvocationHandler )
            {
                T newComposite = newInstance( aCompositeClass );
                Map<Class, Object> oldMixins = ( (CompositeInvocationHandler) handler ).getMixins();
                CompositeInvocationHandler newHandler = CompositeInvocationHandler.getInvocationHandler( newComposite );
                newHandler.setMixins( oldMixins, true );
                return newComposite;
            }
        }
        throw new CompositeInstantiationException( "Not a composite object:" + anObject );
    }

    public boolean isInstance( Class anObjectType, Object anObject )
    {
        if( anObjectType.isInstance( anObject ) )
        {
            return true;
        }
        if( anObject instanceof Proxy )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( anObject );
            if( handler instanceof CompositeInvocationHandler )
            {
                CompositeInvocationHandler oih = (CompositeInvocationHandler) handler;
                return oih.getContext().getCompositeModel().isAssignableFrom( anObjectType );
            }
        }
        return false;
    }

    public <T> T dereference( T proxy )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( proxy );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (T) ( (ProxyReferenceInvocationHandler) handler ).getProxy();
        }
        if( handler instanceof CompositeInvocationHandler )
        {
            return proxy;
        }

        return null;
    }


    public CompositeModel getCompositeModel( Class aCompositeClass )
    {
        CompositeModel compositeModel = composites.get( aCompositeClass );
        if( compositeModel == null )
        {
            compositeModel = new CompositeModel( aCompositeClass );
            composites.put( aCompositeClass, compositeModel );
        }

        return compositeModel;
    }

    public CompositeModel getCompositeModel( Composite aComposite )
    {
        return CompositeInvocationHandler.getInvocationHandler( aComposite ).getContext().getCompositeModel();
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( this, compositeType );
        return builder;
    }

    public List<DependencyResolver> getDependencyResolvers()
    {
        return resolvers;
    }

    // Private ------------------------------------------------------
    private CompositeContextImpl getCompositeContext( CompositeModel compositeModel )
    {
        CompositeContextImpl context = objectContexts.get( compositeModel );
        if( context == null )
        {
            context = new CompositeContextImpl( compositeModel, this, fragmentFactory );
            objectContexts.put( compositeModel, context );
        }
        return context;
    }
}