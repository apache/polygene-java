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
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.CompositeState;
import org.qi4j.api.persistence.Identity;
import org.qi4j.api.Composite;

public abstract class CompositeInvocationHandler<T extends Composite>
    implements InvocationHandler, CompositeState
{
    protected CompositeContextImpl<T> context;
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

    public CompositeInvocationHandler( CompositeContextImpl<T> aContext )
    {
        context = aContext;
    }

    public static <T extends Composite> RegularCompositeInvocationHandler<T> getInvocationHandler( T aProxy )
    {
        return (RegularCompositeInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    public CompositeContext<T> getContext()
    {
        return context;
    }

    protected Object initializeMixin( Class mixinType, T proxy )
    {
        MixinModel mixinModel = context.getCompositeModel().getMixin( mixinType );
        if( mixinModel == null )
        {
            return null;
        }
        Object instance = context.newFragment( mixinModel, proxy, null );
        // Successfully instantiated
        putMixin( mixinType, instance );
        return instance;
    }

    protected Object invokeObject( T proxy, Method method, Object[] args )
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

    protected abstract Object getMixin( Class mixinType, T composite );

    protected abstract void putMixin( Class mixinType, Object value );
}
