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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.advice.Advice;

final class InvocationStackFactory
{
    private final AspectRegistry m_registry;

    InvocationStackFactory( AspectRegistry registry )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( registry, "registry" );
        m_registry = registry;
    }

    /**
     * Creates an invocation stack given the specified {@code descriptor} argument.
     *
     * @param descriptor The joinpoint descriptor. This argument must not be {@code null}.
     *
     * @return The invocation stack.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code descriptor} is {@code null}.
     * @since 1.0.0
     */
    InvocationStack create( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        List<Pointcut> pointcuts = m_registry.getPointcuts( descriptor );
        List<Advice> advices;
        if( pointcuts.isEmpty() )
        {
            advices = Collections.emptyList();
        }
        else
        {
            advices = new LinkedList<Advice>();
            for( Pointcut pointcut : pointcuts )
            {
                List<Advice> createdAdvices = pointcut.createAdvices();
                advices.addAll( createdAdvices );
            }
        }

        Method method = descriptor.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        return new InvocationStack( descriptor, targetClass, advices );
    }
}
