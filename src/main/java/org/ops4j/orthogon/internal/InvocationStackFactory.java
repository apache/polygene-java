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

import java.util.Collections;
import java.util.List;
import java.lang.reflect.Method;
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

    InvocationStack create( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        List<Pointcut> pointcuts = m_registry.getPointcuts( descriptor );
        Method method = descriptor.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        List<Advice> advices;
        if( pointcuts.isEmpty() )
        {
            advices = Collections.emptyList();
        } else
        {
            // TODO
            advices = null;
        }

        return new InvocationStack( descriptor, targetClass, advices );
    }
}
