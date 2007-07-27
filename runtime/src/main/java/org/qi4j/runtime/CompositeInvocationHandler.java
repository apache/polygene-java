/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.CompositeState;
import org.qi4j.api.persistence.Identity;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.CompositeInstantiationException;

public abstract class CompositeInvocationHandler
    implements InvocationHandler, CompositeState
{
    protected CompositeContextImpl context;
    protected static final Method METHOD_GETIDENTITY;

    static
    {
        try
        {
            METHOD_GETIDENTITY = Identity.class.getMethod( "getIdentity" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( Identity.class + " is corrupt." );
        }
    }

    public CompositeInvocationHandler( CompositeContextImpl aContext )
    {
        context = aContext;
    }

    public static RegularCompositeInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (RegularCompositeInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    public CompositeContext getContext()
    {
        return context;
    }

    protected MixinModel findMixinModel( Class mixinType )
    {
        return context.getCompositeModel().locateMixin( mixinType );
    }

    protected void resolveUsesFields( MixinModel mixinModel, Object proxy, Object instance )
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

    protected Object initializeMixin( Class mixinType, Object proxy )
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
        putMixin( mixinType, instance );
        return instance;
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
            if( args[0] == null )
            {
                return false;
            }
            if( context.getCompositeModel().isAssignableFrom( Identity.class ) )
            {
                String id = ( (Identity) proxy ).getIdentity();
                Identity other = ( (Identity) args[ 0 ] );
                return id != null && id.equals( other.getIdentity() );
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

    protected abstract Object getMixin( Class mixinType, Object value );

    protected abstract void putMixin( Class mixinType, Object value );
}
