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
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.AbstractCompositeException;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeState;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.persistence.Identity;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler
    implements InvocationHandler, CompositeState
{
    protected CompositeContextImpl context;
    private ConcurrentHashMap<Class, Object> mixins;
    private static final Method METHOD_GETIDENTITY;

    static
    {
        try
        {
            METHOD_GETIDENTITY = Identity.class.getMethod( "getIdentity", null );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( Identity.class + " is corrupt." );
        }
    }

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
            else
            {
                throw new AbstractCompositeException( "Implementation missing for " + mixinType.getName() + " in " + context.getCompositeModel().getCompositeClass().getName() );
            }
        }
        // Invoke
        return context.getInvocationInstance( method ).invoke( proxy, method, args, mixin, mixinType );
    }

    protected Object getMixin( Class aProxyInterface, Object aProxy )
    {
        Object mixin = mixins.get( aProxyInterface );
        if( mixin == null && !aProxyInterface.equals( Object.class ) )
        {
            mixin = initializeMixin( aProxyInterface, aProxy );
        }
        return mixin;
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    public void setMixins( Map<Class, Object> mixins, boolean keep )
    {
        if( keep && mixins instanceof ConcurrentHashMap )
        {
            this.mixins = (ConcurrentHashMap) mixins;
        }
        else
        {
            this.mixins = new ConcurrentHashMap<Class, Object>();
            this.mixins.putAll( mixins );
        }
    }

    public CompositeContext getContext()
    {
        return context;
    }

    // Private -------------------------------------------------------
    private Object initializeMixin( Class mixinType, Object proxy )
    {
        MixinModel mixinModel = findMixinModel( mixinType );
        if( mixinModel == null )
        {
            return null;
        }

        Object instance = context.getFragmentFactory().newFragment( mixinModel, context.getCompositeModel() );
        resolveUsesFields( mixinModel, proxy, instance );

        List<Field> dependencyFields = mixinModel.getDependencyFields();
        for( Field dependencyField : dependencyFields )
        {
            context.resolveDependency( dependencyField, instance );
        }

        // Successfully instantiated
        mixins.put( mixinType, instance );
        return instance;
    }

    private void resolveUsesFields( MixinModel mixinModel, Object proxy, Object instance )
    {
        // Resolution of @Uses in Mixins (only!).
        List<Field> usesFields = mixinModel.getUsesFields();
        for( Field usesField : usesFields )
        {
            try
            {
                // The Composite implements the Type in the field.
                Class<?> type = usesField.getType();
                if( type.isInstance( proxy ) )
                {
                    // Current proxy
                    usesField.set( instance, proxy );
                }
                else
                {
                    Object directMixin = getMixin( type, proxy );
                    if( directMixin != null )
                    {
                        usesField.set( instance, directMixin );
                    }
                    // If the @Uses field is not optional, throw exception
                    else if( !usesField.getAnnotation( Uses.class ).optional() )
                    {
                        throw new CompositeInstantiationException( "@Uses field " + usesField.getName() + " in class " + mixinModel.getFragmentClass().getName() + " could not be resolved for composite " + context.getCompositeModel().getCompositeClass().getName() + "." );
                    }
                }
            }
            catch( IllegalAccessException e )
            {
                throw new CompositeInstantiationException( "The @Uses field " + usesField.getName() + " in mixin " + mixinModel.getFragmentClass().getName() + " is not accessible.", e );
            }
        }
    }

    private MixinModel findMixinModel( Class aProxyInterface )
    {
        MixinModel mixinModel = context.getCompositeModel().locateMixin( aProxyInterface );
        return mixinModel;
    }

    protected Object invokeObject( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( method.getName().equals( "hashCode" ) )
        {
            if( context.getCompositeModel().isAssignableFrom( Identity.class ) )
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
            if( context.getCompositeModel().isAssignableFrom( Identity.class ) )
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
            if( context.getCompositeModel().isAssignableFrom( Identity.class ) )
            {
                String id = (String) invoke( proxy, METHOD_GETIDENTITY, null );
                return id != null ? id : "";
            }
            else
            {
                return "";
            }
        }

        return null;
    }
}
