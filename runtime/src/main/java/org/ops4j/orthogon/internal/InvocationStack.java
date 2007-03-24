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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.advice.Advice;

public final class InvocationStack
{
    private final JoinpointDescriptor m_descriptor;
    private final Advice m_first;
    private final Advice m_last;
    private final Class m_targetClass;
    private Object m_target;

    InvocationStack( JoinpointDescriptor descriptor, Class targetClass, List<Advice> advices )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( targetClass, "targetClass" );
        NullArgumentException.validateNotNull( advices, "advices" );
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        m_descriptor = descriptor;
        m_targetClass = targetClass;
        if( advices.size() > 0 )
        {
            Iterator<Advice> it = advices.iterator();
            m_first = it.next();
            Advice current = m_first;
            while( it.hasNext() )
            {
                Advice next = it.next();
                current.setTarget( m_targetClass, next );
                current = next;
            }
            m_last = current;
        }
        else
        {
            m_first = null;
            m_last = null;
        }
    }

    void release()
    {
        m_last.setTarget( m_targetClass, null );
        m_target = null;
    }

    Object invoke( Method method, Object[] args )
        throws IllegalAccessException, InvocationTargetException, IllegalArgumentException
    {
        NullArgumentException.validateNotNull( method, "method" );
        if( m_first == null )
        {
            return method.invoke( m_target, args );
        }
        Object result = method.invoke( m_first, args );
        return result;
    }

    /**
     * The @QiDependency fields in the advice chain must be populated with the proxy object, if possible.
     *
     * @param proxy The thisClass instance.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code proxy} argument is {@code null}.
     * @since 1.0.0
     */
    void resolveDependencies( Object proxy )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( proxy, "proxy" );

        if( m_first == null )
        {
            return;
        }

        Advice current = m_first;
        while( true )
        {
            current.resolveDependency( proxy );
            
            Object next = getNext( current );
            if( current == m_last )
            {
                break;
            }
            current = (Advice) next;
        }
    }

    /**
     * Locate the "next" in the advice chain.
     *
     * @param advice The advice to be examined for the 'next' field.
     *
     * @return The next advice in the chain.
     *
     * @since 1.0.0
     */
    private Object getNext( Advice advice )
    {
        return advice.getTarget( m_targetClass );
    }

    public void setTarget( Object target )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( target, "target" );
        if( m_last != null )
        {
            m_last.setTarget( m_targetClass, target );
        }
        m_target = target;
    }

    JoinpointDescriptor getDescriptor()
    {
        return m_descriptor;
    }
}
