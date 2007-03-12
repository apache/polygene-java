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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;

/**
 * TODO: Figure out how to do this in the most efficient manner.
 * TODO: Handle Dynamic type pointcut (i.e. the interceptor that implements InvocationHandler)
 * TODO: Need to have AspectRegistry listener.
 *
 * @since 1.0.0
 */
public final class AspectRegistry
{
    private final Map<Class, Set<Pointcut>> m_classPointcuts;

    public AspectRegistry()
    {
        m_classPointcuts = new HashMap<Class, Set<Pointcut>>();
    }

    /**
     * Returns list of pointcuts that matches the joinpoint descriptor. Returns an empty list if there is no match.
     *
     * @param descriptor The joinpoint descriptor. This argument must not be {@code null}.
     *
     * @return list of pointcuts that matches the joinpoint descriptor.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code descriptor} argument is {@code null}.
     * @since 1.0.0
     */
    final List<Pointcut> getPointcuts( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        Set<Pointcut> poincuts = getProbableMatchPointcuts( descriptor );
        List<Pointcut> matchedPointcuts = new LinkedList<Pointcut>();
        for( Pointcut pointcut : poincuts )
        {
            if( pointcut.isIntersect( descriptor ) )
            {
                matchedPointcuts.add( pointcut );
            }
        }

        return matchedPointcuts;
    }

    /**
     * Returns the most probable match pointcuts.
     * Returns both static and dynamic pointcuts that probably matches the descriptor. The returned values must be
     * iterated to see whether the pointcuts do indeed match the descriptor.
     *
     * @param descriptor The descriptor.
     *
     * @return A list of most probable match pointcuts given the joinpoint descriptor.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code descriptor} is {@code null}.
     * @since 1.0.0
     */
    private Set<Pointcut> getProbableMatchPointcuts( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        // Add static pointcuts
        Set<Pointcut> poincuts = new HashSet<Pointcut>();
        Class[] targetClasses = descriptor.getTargetClasses();
        for( Class targetClass : targetClasses )
        {
            synchronized( this )
            {
                Set<Pointcut> staticPointcuts = m_classPointcuts.get( targetClass );
                if( staticPointcuts != null )
                {
                    poincuts.addAll( staticPointcuts );
                }
            }
        }

        // Add dynamic pointcuts
        synchronized( this )
        {
            Set<Pointcut> dynamicPointcuts = m_classPointcuts.get( Object.class );
            if( dynamicPointcuts != null )
            {
                poincuts.addAll( dynamicPointcuts );
            }
        }

        return poincuts;
    }

    public final void registerPointcut( Pointcut pointcut )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( pointcut, "pointcut" );

        Set<Class> joinpointClasses = pointcut.getJoinpointClasses();
        for( Class aClass : joinpointClasses )
        {
            synchronized( this )
            {
                Set<Pointcut> pointcuts = m_classPointcuts.get( aClass );
                if( pointcuts == null )
                {
                    pointcuts = new HashSet<Pointcut>();
                    m_classPointcuts.put( aClass, pointcuts );
                }

                pointcuts.add( pointcut );
            }
        }
    }

    public final void unregisterPointcut( Pointcut pointcut )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( pointcut, "pointcut" );

        Set<Class> joinpointClasses = pointcut.getJoinpointClasses();
        for( Class aClass : joinpointClasses )
        {
            synchronized( this )
            {
                Set<Pointcut> pointcuts = m_classPointcuts.get( aClass );
                if( pointcuts != null )
                {
                    pointcuts.remove( pointcut );

                    if( pointcuts.isEmpty() )
                    {
                        m_classPointcuts.remove( aClass );
                    }
                }
            }
        }
    }
}
