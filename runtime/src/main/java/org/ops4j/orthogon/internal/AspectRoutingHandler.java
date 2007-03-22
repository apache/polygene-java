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
package org.ops4j.orthogon.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.Identity;
import org.ops4j.orthogon.mixin.MixinUnavailableException;

public final class AspectRoutingHandler
    implements InvocationHandler
{
    private static final Object DUMMY = new Object();
    private static final Method METHOD_EQUALS;
    private static final Method METHOD_GET_IDENTITY;
    private static final Method METHOD_TO_STRING;

    static
    {
        Method equalsMethod = null;
        Method toStringMethod = null;
        Method getIdentityMethod = null;
        try
        {
            equalsMethod = Object.class.getMethod( "equals", Object.class );
            getIdentityMethod = Identity.class.getMethod( "getIdentity" );
            toStringMethod = Object.class.getMethod( "toString" );
        }
        catch( NoSuchMethodException e )
        {
            // Should not happened.
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
        METHOD_EQUALS = equalsMethod;
        METHOD_TO_STRING = toStringMethod;
        METHOD_GET_IDENTITY = getIdentityMethod;
    }

    private final Class m_primaryAspect;
    private final AspectFactoryImpl m_aspectFactory;
    private final HashMap<Class, Object> m_mixinInstances;

    AspectRoutingHandler( Class primaryAspect, AspectFactoryImpl aspectFactory )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( aspectFactory, "aspectFactory" );

        m_primaryAspect = primaryAspect;

        m_aspectFactory = aspectFactory;
        m_mixinInstances = new HashMap<Class, Object>();
    }

    public final void addMixinInterface( Class mixinInterface )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );

        synchronized( m_mixinInstances )
        {
            m_mixinInstances.put( mixinInterface, DUMMY );
        }
    }

    public final Set<Class> getMixinInterfaces()
    {
        synchronized( m_mixinInstances )
        {
            return Collections.unmodifiableSet( m_mixinInstances.keySet() );
        }
    }

    private Object handleNonObjectClassInvocation( Method method, Object proxy, Object[] args )
        throws Throwable
    {
        Object instance;
        Class invokedOn = method.getDeclaringClass();
        synchronized( this )
        {
            // Make sure that the Mixin is still valid
            boolean valid = m_aspectFactory.isMixinInterfaceExists( invokedOn );
            if( !valid )
            {
                throw new MixinUnavailableException( invokedOn );
            }

            instance = m_mixinInstances.get( invokedOn );
            if( instance == null || instance == DUMMY )
            {
                instance = m_aspectFactory.createMixin( invokedOn, m_primaryAspect, proxy );
                if( instance == null )
                {
                    throw new MixinUnavailableException( invokedOn );
                }
                m_mixinInstances.put( invokedOn, instance );
            }
        }

        InvocationStack stack = m_aspectFactory.getInvocationStack( method, proxy );
        if( stack == null )
        {
            return method.invoke( instance, args );
        }

        try
        {
            stack.resolveDependencies( proxy );
            stack.setTarget( instance );
            return stack.invoke( method, args );
        }
        catch( InvocationTargetException ute )
        {
            throw ute.getCause();
        }
        finally
        {
            m_aspectFactory.release( stack );
        }
    }

    private Object handleObjectClassInvocation( Method method, Object[] args, Object proxy )
        throws Throwable
    {
        if( METHOD_EQUALS.equals( method ) )
        {
            Object other = args[ 0 ];
            if( !( other instanceof Identity ) )
            {
                return false;
            }

            Identity otherIdentity = (Identity) other;
            String otherId = otherIdentity.getIdentity();
            String id = (String) invoke( proxy, METHOD_GET_IDENTITY, null );

            return id.equals( otherId );
        }
        else if( METHOD_TO_STRING.equals( method ) )
        {
            String id = (String) invoke( proxy, METHOD_GET_IDENTITY, null );
            return id;
        }
        else
        {
            // TODO: Unhandled case
        }

        return null;
    }

    public final Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Class<?> invokedOn = method.getDeclaringClass();
        if( Object.class.equals( invokedOn ) )
        {
            return handleObjectClassInvocation( method, args, proxy );
        }
        else
        {
            return handleNonObjectClassInvocation( method, proxy, args );
        }
    }

    public final void removeMixinInterface( Class mixinInterface )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinInterface, "mixinInterface" );

        synchronized( m_mixinInstances )
        {
            m_mixinInstances.remove( mixinInterface );
        }
    }
}
