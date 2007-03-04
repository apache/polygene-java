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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import org.ops4j.orthogon.MixinUnavailableException;
import org.ops4j.orthogon.Advice;

public final class AspectRoutingHandler
    implements InvocationHandler
{
    private static final Object DUMMY = new Object();

    private MixinFactory m_mixinFactory;
    private AdviceFactory m_adviceFactory;
    private final HashMap<Class, Object> m_aspectInstances;

    public AspectRoutingHandler( MixinFactory mixinFactory, Set<Class> mixinInterfaces, AdviceFactory adviceFactory )
    {
        m_adviceFactory = adviceFactory;
        m_mixinFactory = mixinFactory;
        m_aspectInstances = new HashMap<Class, Object>();
        for( Class cls : mixinInterfaces )
        {
            addMixinInterface( cls );
        }
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Object instance;
        synchronized( this )
        {
            Class invokedOn = method.getDeclaringClass();
            m_mixinFactory.checkExistence( invokedOn );
            instance = m_aspectInstances.get( invokedOn );
            if( instance == null || instance == DUMMY )
            {
                instance = m_mixinFactory.create( invokedOn );
                if( instance == null )
                {
                    throw new MixinUnavailableException( invokedOn );
                }
                instance = m_adviceFactory.create( invokedOn, instance );
                m_aspectInstances.put( invokedOn, instance );
            }
        }
        return method.invoke( instance, args );
    }

    public Set<Class> getMixinInterfaces()
    {
        synchronized( m_aspectInstances )
        {
            return Collections.unmodifiableSet( m_aspectInstances.keySet() );
        }
    }

    public void addMixinInterface( Class mixinInterface )
    {
        synchronized( m_aspectInstances )
        {
            m_aspectInstances.put( mixinInterface, DUMMY );
        }
    }

    public void removeMixinInterface( Class mixinInterface )
    {
        synchronized( m_aspectInstances )
        {
            m_aspectInstances.remove( mixinInterface );
        }
    }
}
