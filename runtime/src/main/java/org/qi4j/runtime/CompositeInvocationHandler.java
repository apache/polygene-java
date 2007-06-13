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
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.persistence.Identity;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler
    implements InvocationHandler
{
    private CompositeContextImpl context;
    private Map<Class, Object> mixins;

    public CompositeInvocationHandler( CompositeContextImpl aContext )
    {
        this.context = aContext;
        mixins = new IdentityHashMap<Class, Object>();
    }

    public static CompositeInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (CompositeInvocationHandler) Proxy.getInvocationHandler( aProxy );
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
                mixin = initializeMixin( proxyInterface, proxy, getWrappedInstance() );
            }
        }

        // Invoke
        return context.getInvocationInstance( method ).invoke( proxy, method, args, mixin );
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    public CompositeContext getContext()
    {
        return context;
    }

    // Private -------------------------------------------------------
    protected synchronized Object initializeMixin( Class aProxyInterface, Object proxy, Object wrappedInstance )
        throws IllegalAccessException
    {
        MixinModel mixinModel = context.getCompositeObject().getMixin( aProxyInterface );

        // Check if implementation is latent in decorated object
        if( mixinModel == null && wrappedInstance != null )
        {
            // Check if wrapped instance can handle the call directly
            if( aProxyInterface.isInstance( wrappedInstance ) )
            {
                return wrappedInstance;
            }

            CompositeObject wrappedComposite = context.getCompositeObject().getWrappedCompositeModel();
            if( wrappedComposite != null )
            {
                mixinModel = wrappedComposite.getMixin( aProxyInterface );
            }
        }

        Object instance = null;
        if( mixinModel == null )
        {
            // Try the interface itself
            try
            {
                instance = context.getFragmentFactory().newFragment( new MixinModel( aProxyInterface ), context.getCompositeObject() );
            }
            catch( CompositeInstantiationException e )
            {
                // Didn't work
                throw new CompositeInstantiationException( "Could not find implementation for " + aProxyInterface.getName() + " in composite " + context.getCompositeObject().getCompositeInterface().getName() );
            }

        }

        if( instance == null )
        {
            instance = context.getFragmentFactory().newFragment( mixinModel, context.getCompositeObject() );
        }

        List<Field> usesFields = mixinModel.getUsesFields();
        for( Field usesField : usesFields )
        {
            if( usesField.getType().isInstance( proxy ) )
            {
                // Current proxy
                usesField.set( instance, proxy );
            }
            else if( usesField.getType().isInstance( wrappedInstance ) )
            {
                // The wrapped object is of the required type
                usesField.set( instance, wrappedInstance );
            }
            else if( context.getCompositeFactory().isInstance( usesField.getType(), proxy ) )
            {
                usesField.set( instance, context.getCompositeFactory().cast( usesField.getType(), proxy ) );
            }
            else if( context.getCompositeFactory().isInstance( usesField.getType(), wrappedInstance ) )
            {
                usesField.set( instance, context.getCompositeFactory().cast( usesField.getType(), wrappedInstance ) );
            }
            else
            {
                throw new CompositeInstantiationException( "@Uses field " + usesField.getName() + " in class " + mixinModel.getFragmentClass().getName() + " could not be resolved for composite " + context.getCompositeObject().getCompositeInterface().getName() + "." );
            }
        }


        List<? extends DependencyResolver> resolvers = context.getDependencyResolvers();
        List<Field> dependencyFields = mixinModel.getDependencyFields();
        for( Field dependencyField : dependencyFields )
        {
            Object dependency = null;
            for( DependencyResolver resolver : resolvers )
            {
                dependency = resolver.resolveField( dependencyField, context );
                if( dependency != null )
                {
                    break;
                }
            }
            if( dependency == null )
            {
                throw new CompositeInstantiationException( "@Dependency field " + dependencyField.getName() + " in class " + mixinModel.getFragmentClass().getName() + " could not be resolved." );
            }
            dependencyField.set( instance, dependency );
        }

        // Successfully instantiated
        mixins.put( aProxyInterface, instance );
        return instance;
    }

    private Object invokeObject( Object proxy, Method method, Object[] args )
    {
        if( method.getName().equals( "hashCode" ) )
        {
            if( context.getCompositeObject().isAssignableFrom( Identity.class ) )
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
            if( context.getCompositeObject().isAssignableFrom( Identity.class ) )
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
            if( context.getCompositeObject().isAssignableFrom( Identity.class ) )
            {
                String id = context.getCompositeFactory().cast( Identity.class, proxy ).getIdentity();
                return id != null ? id : "";
            }
            else
            {
                return "";
            }
        }

        return null;
    }

    protected Object getWrappedInstance()
    {
        return null;
    }
}
