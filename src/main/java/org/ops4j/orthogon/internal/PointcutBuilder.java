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
import java.util.LinkedList;
import java.util.List;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.pointcut.QiInterceptor;
import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;

final class PointcutBuilder
{
    private Pointcut m_parent;
    private List<QiImplements> m_implements;
    private List<QiProperty> m_property;
    private List<QiMethodExpression> m_methodExpressions;
    private List<QiTargetClass> m_targetClasses;
    private List<QiInterceptor> m_interceptors;

    PointcutBuilder( Pointcut parent )
    {
        m_parent = parent;
    }

    void addImplementsConstraint( QiImplements implementsConstraint )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( implementsConstraint, "implementsConstraint" );

        m_implements = addToList( m_implements, implementsConstraint );
    }

    final void addPropertyConstraint( QiProperty propertyConstraint )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( propertyConstraint, "propertyConstraint" );

        m_property = addToList( m_property, propertyConstraint );
    }

    final void addMethodExpressionConstraint( QiMethodExpression methodExpressionConstraint )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( methodExpressionConstraint, "methodExpressionConstraint" );

        m_methodExpressions = addToList( m_methodExpressions, methodExpressionConstraint );
    }

    final void addTargetClassConstraint( QiTargetClass targetClassConstraint )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( targetClassConstraint, "targetClassConstraint" );

        m_targetClasses = addToList( m_targetClasses, targetClassConstraint );
    }

    final void addInterceptor( QiInterceptor interceptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( interceptor, "interceptor" );

        m_interceptors = addToList( m_interceptors, interceptor );
    }

    final Pointcut create()
    {
        List<QiImplements> implementations = ensureNotNull( m_implements );
        List<QiProperty> properties = ensureNotNull( m_property );
        List<QiMethodExpression> methodExpressions = ensureNotNull( m_methodExpressions );
        List<QiTargetClass> targetClasses = ensureNotNull( m_targetClasses );
        List<QiInterceptor> interceptors = ensureNotNull( m_interceptors );
        return new Pointcut( m_parent, implementations, properties, methodExpressions, targetClasses, interceptors );
    }

    /**
     * Add the specified {@code entry} argument to the {@code list} argument. If the specified {@code list} is
     * {@code null} initialized it with a {@code LinkedList}.
     *
     * @param list  The list to be added.
     * @param entry The entry.
     *
     * @return The specified {@code list} or a new initialized list with {@code entry} as the first element.
     *
     * @since 1.0.0
     */
    private <T> List<T> addToList( List<T> list, T entry )
    {
        if( list == null )
        {
            list = new LinkedList<T>();
        }
        list.add( entry );
        return list;
    }

    /**
     * Returns an empty list if the specified {@code list} argument is {@code null}, returns the specified {@code list}
     * argument otherwise.
     *
     * @param list The list to check.
     *
     * @return The specified {@code list} or empty list.
     *
     * @see Collections#emptyList()
     * @since 1.0.0
     */
    private <T> List<T> ensureNotNull( List<T> list )
    {
        if( list == null )
        {
            return Collections.emptyList();
        }

        return list;
    }
}
