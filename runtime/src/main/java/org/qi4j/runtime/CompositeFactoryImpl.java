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
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.spi.DefaultDependencyResolver;

/**
 * Default implementation of CompositeFactory
 */
public final class CompositeFactoryImpl
    implements CompositeFactory
{
    private FragmentFactory fragmentFactory;
    private Map<Class, CompositeModel> composites;
    private Map<CompositeObject, CompositeContextImpl> objectContexts;
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
        objectContexts = new ConcurrentHashMap<CompositeObject, CompositeContextImpl>();
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
            CompositeObject compositeObject = new CompositeObject( compositeModel, aCompositeClass );
            CompositeContextImpl context = getCompositeContext( compositeObject );

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

    public <T> T cast( Class<T> aCompositeClass, Object anObject )
    {
        if( aCompositeClass.isInstance( anObject ) )
        {
            return aCompositeClass.cast( anObject );
        }

        if( anObject instanceof Proxy )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( anObject );
            if( handler instanceof ProxyReferenceInvocationHandler )
            {
                // Get real handler
                handler = Proxy.getInvocationHandler( ( (ProxyReferenceInvocationHandler) handler ).getProxy() );
            }
            ClassLoader proxyClassLoader = aCompositeClass.getClassLoader();

            try
            {
                Class[] interfaces = new Class[]{ aCompositeClass };
                return aCompositeClass.cast( Proxy.newProxyInstance( proxyClassLoader, interfaces, handler ) );
            }
            catch( Exception e )
            {
                throw new CompositeInstantiationException( e );
            }
        }
        else
        {
            throw new CompositeInstantiationException( "Not a composite object:" + anObject );
        }
    }

    public <T extends Composite> T wrapInstance( Class<T> aCompositeClass, Object anObject )
    {
        if( anObject instanceof Proxy && anObject instanceof Composite )
        {
            InvocationHandler wrappedHandler = Proxy.getInvocationHandler( anObject );
            if( wrappedHandler instanceof WrappedCompositeInvocationHandler )
            {
                Object wrappedObject = ( (WrappedCompositeInvocationHandler) wrappedHandler ).getWrappedInstance();
                if( aCompositeClass.isInstance( wrappedObject ) )
                {
                    anObject = wrappedObject;
                }
            }
        }

        CompositeObject wrappedCompositeObject = null;
        if( anObject instanceof Proxy )
        {
            wrappedCompositeObject = CompositeInvocationHandler.getInvocationHandler( anObject ).getContext().getCompositeObject();
        }

        CompositeModel compositeModel = getCompositeModel( aCompositeClass );
        Class wrappedInterface = anObject.getClass().getInterfaces()[ 0 ];
        CompositeObject compositeObject = new CompositeObject( compositeModel, aCompositeClass, wrappedCompositeObject, wrappedInterface );
        CompositeContextImpl context = getCompositeContext( compositeObject );
        CompositeInvocationHandler handler = new WrappedCompositeInvocationHandler( anObject, context );
        ClassLoader proxyClassLoader = aCompositeClass.getClassLoader();

        try
        {
            Class[] interfaces = new Class[]{ aCompositeClass };
            return aCompositeClass.cast( Proxy.newProxyInstance( proxyClassLoader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
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
            if( handler instanceof WrappedCompositeInvocationHandler )
            {
                WrappedCompositeInvocationHandler wrapperHandler = (WrappedCompositeInvocationHandler) handler;
                return isInstance( anObjectType, wrapperHandler.getWrappedInstance() );
            }
            else if( handler instanceof CompositeInvocationHandler )
            {
                CompositeInvocationHandler oih = (CompositeInvocationHandler) handler;
                return oih.getContext().getCompositeObject().isAssignableFrom( anObjectType );
            }
        }
        return false;
    }

    public <T> T getThat( T proxy )
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

    public CompositeObject getCompositeObject( Composite aComposite )
    {
        return CompositeInvocationHandler.getInvocationHandler( aComposite ).getContext().getCompositeObject();
    }

    public List<DependencyResolver> getDependencyResolvers()
    {
        return resolvers;
    }

    // Private ------------------------------------------------------
    private CompositeContextImpl getCompositeContext( CompositeObject aComposite )
    {
        CompositeContextImpl context = objectContexts.get( aComposite );
        if( context == null )
        {
            context = new CompositeContextImpl( aComposite, this, fragmentFactory );
            objectContexts.put( aComposite, context );
        }
        return context;
    }
}