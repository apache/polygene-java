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
import org.ops4j.orthogon.mixin.MixinUnavailableException;

public final class AspectRoutingHandler
    implements InvocationHandler
{
    private static final Object DUMMY = new Object();

    private MixinFactory m_mixinFactory;
    private AdviceFactoryImpl m_adviceFactory;
    private final HashMap<Class, Object> m_introductionInstances;

    public AspectRoutingHandler( MixinFactory mixinFactory, Set<Class> introductionInterfaces,
                                 AdviceFactoryImpl adviceFactory
    )
    {
        m_adviceFactory = adviceFactory;
        m_mixinFactory = mixinFactory;
        m_introductionInstances = new HashMap<Class, Object>();
        for( Class cls : introductionInterfaces )
        {
            addIntroductionInterface( cls );
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
            instance = m_introductionInstances.get( invokedOn );
            if( instance == null || instance == DUMMY )
            {
                instance = m_mixinFactory.create( invokedOn );
                if( instance == null )
                {
                    throw new MixinUnavailableException( invokedOn );
                }
                instance = m_adviceFactory.create( invokedOn, instance );
                m_introductionInstances.put( invokedOn, instance );
            }
        }
        return method.invoke( instance, args );
    }

    public Set<Class> getIntroductionInterfaces()
    {
        synchronized( m_introductionInstances )
        {
            return Collections.unmodifiableSet( m_introductionInstances.keySet() );
        }
    }

    public void addIntroductionInterface( Class introductionInterface )
    {
        synchronized( m_introductionInstances )
        {
            m_introductionInstances.put( introductionInterface, DUMMY );
        }
    }

    public void removeIntroductionInterface( Class introductionInterface )
    {
        synchronized( m_introductionInstances )
        {
            m_introductionInstances.remove( introductionInterface );
        }
    }
}
