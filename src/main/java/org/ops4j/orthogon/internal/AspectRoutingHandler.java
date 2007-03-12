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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.mixin.MixinUnavailableException;

public final class AspectRoutingHandler
    implements InvocationHandler
{

    private static final Object DUMMY = new Object();

    private final AspectFactoryImpl m_aspectFactory;
    private final HashMap<Class, Object> m_mixinInstances;

    AspectRoutingHandler( AspectFactoryImpl aspectFactory )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( aspectFactory, "aspectFactory" );

        m_aspectFactory = aspectFactory;
        m_mixinInstances = new HashMap<Class, Object>();
    }

    public final Object invoke( Object proxy, Method method, Object[] args )
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
                instance = m_aspectFactory.createMixin( invokedOn );
                if( instance == null )
                {
                    throw new MixinUnavailableException( invokedOn );
                }
                m_mixinInstances.put( invokedOn, instance );
            }
        }

        InvocationStack stack = m_aspectFactory.getInvocationStack( method, proxy );
        Object values;
        try
        {
            stack.resolveDependencies( proxy );
            stack.setTarget( instance );
            values = stack.invoke( method, args );
        }
        finally
        {
            m_aspectFactory.release( stack );
        }

        return values;
    }

    public final Set<Class> getMixinInterfaces()
    {
        synchronized( m_mixinInstances )
        {
            return Collections.unmodifiableSet( m_mixinInstances.keySet() );
        }
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
