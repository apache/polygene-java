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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeObject;

/**
 * Default implementation of ObjectFactory
 */
public final class ObjectFactoryImpl
    implements ObjectFactory
{
    private FragmentFactory fragmentFactory;
    private Map<Class, CompositeModel> composites;
    private Map<CompositeObject, ObjectContext> objectContexts;

    public ObjectFactoryImpl()
    {
        this( new FragmentFactoryImpl() );
    }

    public ObjectFactoryImpl( FragmentFactory aFragmentFactory )
    {
        fragmentFactory = aFragmentFactory;
        objectContexts = new ConcurrentHashMap<CompositeObject, ObjectContext>();
        composites = new ConcurrentHashMap();
    }

    public <T extends Composite> T newInstance( Class<T> aCompositeClass )
        throws ObjectInstantiationException
    {
        // Ensure that given class extends Composite
        if (!Composite.class.isAssignableFrom(aCompositeClass))
            throw new ObjectInstantiationException( "Class "+aCompositeClass.getName()+" does not extend "+ Composite.class.getName());

        // Instantiate proxy for given composite interface
        try
        {
            CompositeModel compositeModel = getCompositeModel( aCompositeClass );
            CompositeObject compositeObject = new CompositeObject( compositeModel, aCompositeClass );
            ObjectContext context = getObjectContext( compositeObject );

            ObjectInvocationHandler handler = new ObjectInvocationHandler( context );
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

        CompositeObject wrappedCompositeObject = null;
        if( anObject instanceof Proxy )
        {
            wrappedCompositeObject = ObjectInvocationHandler.getInvocationHandler( anObject ).getContext().getCompositeObject();
        }

        CompositeModel compositeModel = getCompositeModel( aCompositeClass );
        CompositeObject compositeObject = new CompositeObject( compositeModel, aCompositeClass, wrappedCompositeObject );
        ObjectContext context = getObjectContext( compositeObject );
        ObjectInvocationHandler handler = new DecoratorObjectInvocationHandler( anObject, context );
        ClassLoader proxyClassLoader = aCompositeClass.getClassLoader();

        try
        {
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
                return isInstance( anObjectType, decoratorHandler.getDecoratedInstance() );
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
        if( handler instanceof ObjectInvocationHandler )
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

    public CompositeObject getCompositeObject( Composite aComposite)
    {
        return ObjectInvocationHandler.getInvocationHandler( aComposite ).getContext().getCompositeObject();
    }

    // Private ------------------------------------------------------
    private ObjectContext getObjectContext( CompositeObject aComposite )
    {
        ObjectContext context = objectContexts.get( aComposite );
        if( context == null )
        {
            context = new ObjectContext( aComposite, this, fragmentFactory );
            objectContexts.put( aComposite, context );
        }
        return context;
    }
}