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

import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.Composite;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;
import org.qi4j.spi.object.ObjectContext;
import org.qi4j.spi.object.InvocationInstancePool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * TODO
 *
 */
public final class ObjectFactoryImpl
    implements ObjectFactory
{
    private FragmentFactory fragmentFactory;
    private InvocationInstancePool invocationInstancePool;

    public ObjectFactoryImpl()
    {
        this (new FragmentFactoryImpl());
    }

    public ObjectFactoryImpl( FragmentFactory aFragmentFactory )
    {
        fragmentFactory = aFragmentFactory;
        invocationInstancePool = new InvocationInstancePoolImpl( this, fragmentFactory );
    }

    public <T> T newInstance( Class<T> aCompositeClass )
    {
        try
        {
            Composite composite = new Composite( aCompositeClass );
            ObjectContext context= new ObjectContextImpl( composite, this, fragmentFactory, invocationInstancePool);

            ObjectInvocationHandler handler = new ObjectInvocationHandler( context);
            ClassLoader proxyClassloader = aCompositeClass.getClassLoader();
            Class[] interfaces = new Class[]{ aCompositeClass };
            return (T) Proxy.newProxyInstance( proxyClassloader, interfaces, handler );
        }
        catch( Exception e )
        {
            throw new ObjectInstantiationException( e );
        }
    }

    public <T> T cast( Class<T> aCompositeClass, Object anObject )
    {
        try
        {
            if( anObject instanceof Proxy )
            {
                InvocationHandler wrappedHandler = Proxy.getInvocationHandler( anObject );
                if( wrappedHandler instanceof DecoratorObjectInvocationHandler )
                {
                    Object wrappedObject = ( (DecoratorObjectInvocationHandler) wrappedHandler ).getDecoratedInstance();
                    if( aCompositeClass.isInstance( wrappedObject ) )
                    {
                        anObject = wrappedObject;
                    }
                }
            }

            ObjectContext context = new ObjectContextImpl( new Composite(aCompositeClass), this, fragmentFactory, invocationInstancePool );
            ObjectInvocationHandler handler = new DecoratorObjectInvocationHandler( anObject, context );
            ClassLoader proxyClassLoader = aCompositeClass.getClassLoader();
            Class[] interfaces = new Class[]{ aCompositeClass };
            return (T) Proxy.newProxyInstance( proxyClassLoader, interfaces, handler );
        }
        catch( Exception e )
        {
            throw new ObjectInstantiationException( e );
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
            if( handler instanceof DecoratorObjectInvocationHandler )
            {
                DecoratorObjectInvocationHandler decoratorHandler = (DecoratorObjectInvocationHandler) handler;
                return isInstance( anObjectType, decoratorHandler.getDecoratedInstance());
            }
        }
        return false;
    }
    
    public <T> T getThat( T proxy )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( proxy );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (T) ((ProxyReferenceInvocationHandler) handler).getProxy();
        }
        if( handler instanceof ObjectInvocationHandler )
        {
            return proxy;
        }

        return null;
    }


    public Composite getComposite( Class aCompositeClass )
    {
        return new Composite( aCompositeClass );
    }
}