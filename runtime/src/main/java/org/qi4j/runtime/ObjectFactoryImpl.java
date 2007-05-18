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

import org.qi4j.api.MixinFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.ObjectFactory;
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
    private MixinFactory mixinFactory;
    private InvocationInstancePool invocationInstancePool;

    public ObjectFactoryImpl( MixinFactory aMixinFactory )
    {
        mixinFactory = aMixinFactory;
        invocationInstancePool = new InvocationInstancePoolImpl( new ModifierInstanceFactoryImpl( this, mixinFactory) );
    }

    public <T> T newInstance( Class<T> anObjectType )
    {
        try
        {
            ObjectContext context= new ObjectContextImpl( anObjectType, this, mixinFactory, invocationInstancePool);

            ObjectInvocationHandler handler = new ObjectInvocationHandler( context);
            ClassLoader proxyClassloader = anObjectType.getClassLoader();
            Class[] interfaces = new Class[]{ anObjectType };
            return (T) Proxy.newProxyInstance( proxyClassloader, interfaces, handler );
        }
        catch( Exception e )
        {
            throw new ObjectInstantiationException( e );
        }
    }

    public <T> T cast( Class<T> anObjectType, Object anObject )
    {
        try
        {
            if( anObject instanceof Proxy )
            {
                InvocationHandler wrappedHandler = Proxy.getInvocationHandler( anObject );
                if( wrappedHandler instanceof WrappedObjectInvocationHandler )
                {
                    Object wrappedObject = ( (WrappedObjectInvocationHandler) wrappedHandler ).getWrappedInstance();
                    if( anObjectType.isInstance( wrappedObject ) )
                    {
                        anObject = wrappedObject;
                    }
                }
            }

            ObjectContext context = new ObjectContextImpl( anObjectType, this, mixinFactory, invocationInstancePool );
            ObjectInvocationHandler handler = new WrappedObjectInvocationHandler( anObject, context );
            ClassLoader proxyClassLoader = anObjectType.getClassLoader();
            Class[] interfaces = new Class[]{ anObjectType };
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
            if( handler instanceof WrappedObjectInvocationHandler )
            {
                WrappedObjectInvocationHandler wrappedHandler = (WrappedObjectInvocationHandler) handler;
                return isInstance( anObjectType, wrappedHandler.getWrappedInstance());
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


}