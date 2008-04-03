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
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.entity.Identity;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.spi.composite.CompositeState;

public abstract class AbstractCompositeInstance
    implements InvocationHandler, CompositeState
{
    protected static final Method METHOD_IDENTITY;
    protected static final Method METHOD_GET;

    protected CompositeContext context;
    protected Composite proxy;

    static
    {
        try
        {
            METHOD_GET = ImmutableProperty.class.getMethod( "get" );
            METHOD_IDENTITY = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: AbstractCompositeInstance" );
        }
    }

    public static CompositeInstance getCompositeInstance( Object aProxy )
    {
        return (CompositeInstance) Proxy.getInvocationHandler( aProxy );
    }

    public AbstractCompositeInstance( CompositeContext aContext )
    {
        this.context = aContext;
    }

    public CompositeContext getContext()
    {
        return context;
    }

    public Composite getProxy()
    {
        return proxy;
    }

    public void setProxy( Composite proxy )
    {
        this.proxy = proxy;
    }

    protected Object invokeObject( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( method.getName().equals( "hashCode" ) )
        {
            return onHashCode( proxy );
        }
        if( method.getName().equals( "equals" ) )
        {
            return onEquals( proxy, args );
        }
        if( method.getName().equals( "toString" ) )
        {
            return onToString( proxy );
        }

        return null;
    }

    protected Object onHashCode( Object proxy )
    {
        return hashCode();
    }

    protected Object onEquals( Object proxy, Object[] args )
    {
        if( args[ 0 ] == null )
        {
            return false;
        }
        else
        {
            return getCompositeInstance( proxy ) == this;
        }
    }

    protected Object onToString( Object proxy )
        throws Throwable
    {
        if( ImmutableProperty.class.isAssignableFrom( context.getCompositeModel().getCompositeType() ) )
        {
            Object value = invoke( proxy, METHOD_GET, null );
            return value != null ? value.toString() : "";
        }
        else
        {
            return toString();
        }
    }
}
