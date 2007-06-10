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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.model.CompositeInterface;
import org.qi4j.api.model.Mixin;
import org.qi4j.api.persistence.Identity;

/**
 * InvocationHandler for proxy objects.
 */
public class ObjectInvocationHandler
    implements InvocationHandler
{
    private ObjectContext context;
    private Map<Class, Object> mixins;

    public ObjectInvocationHandler( ObjectContext aContext )
    {
        this.context = aContext;
        mixins = new IdentityHashMap<Class, Object>();
    }

    public static ObjectInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (ObjectInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Class proxyInterface = method.getDeclaringClass();

        Object mixin = mixins.get( proxyInterface );

        if( mixin == null )
        {
            if( proxyInterface.equals( Object.class ) )
            {
                return invokeObject( proxy, method, args );
            }
            else
            {
                mixin = initializeMixin( proxyInterface, proxy, getDecoratedInstance() );
            }
        }

        // Invoke
        return context.getInvocationInstance( method ).invoke( proxy, method, args, mixin );
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    public ObjectContext getContext()
    {
        return context;
    }

    // Private -------------------------------------------------------
    protected synchronized Object initializeMixin( Class aProxyInterface, Object proxy, Object wrappedInstance )
        throws IllegalAccessException
    {
        Mixin mixin = context.getCompositeInterface().getMixin( aProxyInterface );

        // Check if implementation is latent in decorated object
        if( mixin == null && wrappedInstance != null )
        {
            // Check if wrapped instance can handle the call directly
            if( aProxyInterface.isInstance( wrappedInstance ) )
            {
                return wrappedInstance;
            }

            CompositeInterface decoratedComposite = context.getCompositeInterface().getWrappedComposite();
            if( decoratedComposite != null )
            {
                mixin = decoratedComposite.getMixin( aProxyInterface );
            }
        }

        Object instance = null;
        if( mixin == null )
        {
            // Try the interface itself
            try
            {
                instance = context.getFragmentFactory().newFragment( new Mixin( aProxyInterface ), context.getCompositeInterface() );
            }
            catch( ObjectInstantiationException e )
            {
                // Didn't work
                throw new ObjectInstantiationException( "Could not find implementation for " + aProxyInterface.getName() + " in composite " + context.getCompositeInterface().getCompositeInterface().getName() );
            }

        }

        if( instance == null )
        {
            instance = context.getFragmentFactory().newFragment( mixin, context.getCompositeInterface() );
        }

        List<Field> usesFields = mixin.getUsesFields();
        for( Field usesField : usesFields )
        {
            if( usesField.getType().isInstance( proxy ) )
            {
                usesField.set( instance, proxy );
            }
            else if( usesField.getType().isInstance( wrappedInstance ) )
            {
                usesField.set( instance, wrappedInstance );
            }
            else if( context.getObjectFactory().isInstance( usesField.getType(), wrappedInstance ) )
            {
                usesField.set( instance, context.getObjectFactory().cast( usesField.getType(), wrappedInstance ) );
            }
            else
            {
                throw new ObjectInstantiationException( "@Uses field " + usesField.getName() + " in class " + mixin.getFragmentClass().getName() + " could not be resolved for composite " + context.getCompositeInterface().getCompositeInterface().getName() + "." );
            }
        }

        List<Field> dependencyFields = mixin.getDependencyFields();
        for( Field dependencyField : dependencyFields )
        {
            if( dependencyField.getType().equals( ObjectFactory.class ) )
            {
                dependencyField.set( instance, context.getObjectFactory() );
            }
            else if( dependencyField.getType().equals( FragmentFactory.class ) )
            {
                dependencyField.set( instance, context.getFragmentFactory() );
            }
            else
            {
                throw new ObjectInstantiationException( "@Dependency field " + dependencyField.getName() + " in class " + mixin.getFragmentClass().getName() + " could not be resolved." );
            }
        }

        // Successfully instantiated
        mixins.put( aProxyInterface, instance );
        return instance;
    }

    private Object invokeObject( Object proxy, Method method, Object[] args )
    {
        if( method.getName().equals( "hashCode" ) )
        {
            if( context.getCompositeInterface().isAssignableFrom( Identity.class ) )
            {
                String id = ( (Identity) proxy ).getIdentity();
                if( id != null )
                {
                    return id.hashCode();
                }
                else
                {
                    return 0;
                }
            }
            else
            {
                return 0; // TODO ?
            }
        }
        if( method.getName().equals( "equals" ) )
        {
            if( context.getCompositeInterface().isAssignableFrom( Identity.class ) )
            {
                String id = ( (Identity) proxy ).getIdentity();
                return id != null && id.equals( ( (Identity) args[ 0 ] ).getIdentity() );
            }
            else
            {
                return false;
            }
        }
        if( method.getName().equals( "toString" ) )
        {
            if( context.getCompositeInterface().isAssignableFrom( Identity.class ) )
            {
                String id = ( (Identity) proxy ).getIdentity();
                return id != null ? id : "";
            }
            else
            {
                return "";
            }
        }

        return null;
    }

    protected Object getDecoratedInstance()
    {
        return null;
    }
}
