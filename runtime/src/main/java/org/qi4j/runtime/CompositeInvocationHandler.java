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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.persistence.Identity;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler
    implements InvocationHandler
{
    protected CompositeContextImpl context;
    private ConcurrentHashMap<Class, Object> mixins;

    public CompositeInvocationHandler( CompositeContextImpl aContext )
    {
        this.context = aContext;
        mixins = new ConcurrentHashMap<Class, Object>();
    }

    public static CompositeInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (CompositeInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Class mixinType = method.getDeclaringClass();
        Object mixin = getMixin( mixinType, proxy );
        if( mixin == null )
        {
            if( mixinType.equals( Object.class ) )
            {
                return invokeObject( proxy, method, args );
            }
        }
        // Invoke
        return context.getInvocationInstance( method ).invoke( proxy, method, args, mixin, mixinType );
    }

    protected Object getMixin( Class aProxyInterface, Object aProxy )
    {
        Object mixin = mixins.get( aProxyInterface );
        if( mixin == null && !aProxyInterface.equals( Object.class ))
        {
            mixin = initializeMixin( aProxyInterface, aProxy );
        }
        return mixin;
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
    private Object initializeMixin( Class aProxyInterface, Object proxy )
    {
        MixinModel mixinModel = findMixinModel( aProxyInterface );

        Object instance = null;
        if( mixinModel == null )
        {
            // Check if wrapped instance can handle the call directly
            if( getWrappedInstance() != null && aProxyInterface.isInstance( getWrappedInstance() ) )
            {
                return null;
            }

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

        resolveUsesFields( mixinModel, proxy, instance );

        List<Field> dependencyFields = mixinModel.getDependencyFields();
        for( Field dependencyField : dependencyFields )
        {
            context.resolveDependency( dependencyField, instance );
        }

        // Successfully instantiated
        mixins.put( aProxyInterface, instance );
        return instance;
    }

    private void resolveUsesFields( MixinModel mixinModel, Object proxy, Object instance )
    {
        Object wrappedInstance = getWrappedInstance();
        List<Field> usesFields = mixinModel.getUsesFields();
        for( Field usesField : usesFields )
        {
            try
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
                    // If the @Uses field is not optional, throw exception
                    if( !usesField.getAnnotation( Uses.class ).optional() )
                    {
                        throw new CompositeInstantiationException( "@Uses field " + usesField.getName() + " in class " + mixinModel.getFragmentClass().getName() + " could not be resolved for composite " + context.getCompositeObject().getCompositeInterface().getName() + "." );
                    }
                }
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
        }
    }

    private MixinModel findMixinModel( Class aProxyInterface )
    {
        Object wrappedInstance = getWrappedInstance();

        MixinModel mixinModel = context.getCompositeObject().locateMixin( aProxyInterface );

        // Check if implementation is latent in decorated object
        if( mixinModel == null && wrappedInstance != null )
        {
            // Check if wrapped instance can handle the call directly
            if( aProxyInterface.isInstance( wrappedInstance ) )
            {
                return null;
            }

            CompositeObject wrappedComposite = context.getCompositeObject().getWrappedCompositeModel();
            if( wrappedComposite != null )
            {
                mixinModel = wrappedComposite.locateMixin( aProxyInterface );
            }
        }

        return mixinModel;
    }

    protected Object invokeObject( Object proxy, Method method, Object[] args )
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
