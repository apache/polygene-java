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
import org.ops4j.orthogon.IntroductionUnavailableException;

public final class AspectRoutingHandler
    implements InvocationHandler
{
    private static final Object DUMMY = new Object();

    private IntroductionFactory m_introductionFactory;
    private AdviceFactory m_adviceFactory;
    private final HashMap<Class, Object> m_aspectInstances;

    public AspectRoutingHandler( IntroductionFactory introductionFactory, Set<Class> introductionInterfaces, AdviceFactory adviceFactory )
    {
        m_adviceFactory = adviceFactory;
        m_introductionFactory = introductionFactory;
        m_aspectInstances = new HashMap<Class, Object>();
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
            m_introductionFactory.checkExistence( invokedOn );
            instance = m_aspectInstances.get( invokedOn );
            if( instance == null || instance == DUMMY )
            {
                instance = m_introductionFactory.create( invokedOn );
                if( instance == null )
                {
                    throw new IntroductionUnavailableException( invokedOn );
                }
                instance = m_adviceFactory.create( invokedOn, instance );
                m_aspectInstances.put( invokedOn, instance );
            }
        }
        return method.invoke( instance, args );
    }

    public Set<Class> getIntroductionInterfaces()
    {
        synchronized( m_aspectInstances )
        {
            return Collections.unmodifiableSet( m_aspectInstances.keySet() );
        }
    }

    public void addIntroductionInterface( Class introductionInterface )
    {
        synchronized( m_aspectInstances )
        {
            m_aspectInstances.put( introductionInterface, DUMMY );
        }
    }

    public void removeIntroductionInterface( Class introductionInterface )
    {
        synchronized( m_aspectInstances )
        {
            m_aspectInstances.remove( introductionInterface );
        }
    }
}
