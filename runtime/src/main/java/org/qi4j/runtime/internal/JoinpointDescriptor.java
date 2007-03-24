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
package org.qi4j.runtime.internal;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.qi4j.runtime.internal.util.CollectionUtil;
import org.qi4j.runtime.mixin.QiMixin;

public final class JoinpointDescriptor
{
    private static final Class[] BLANK_CLASS_ARRAY = new Class[0];
    public static int COUNTER1 = 0;
    public static int COUNTER2 = 0;
    public static int COUNTER3 = 0;
    public static int COUNTER4 = 0;
    public static int COUNTER5 = 0;
    private final Method m_method;
    private final Class[] m_targetClasses;
    private final int m_hashCode;

    JoinpointDescriptor( Method method, Class... targetClasses )
        throws IllegalArgumentException
    {
        m_method = method;
        m_targetClasses = getAllDeclaringInterfaces( targetClasses );
        m_hashCode = method.hashCode();
    }

    private static Class[] getAllDeclaringInterfaces( Class... classes )
    {
        Set<Class> results = new HashSet<Class>();
        Stack<Class> stack = new Stack<Class>();
        for( Class aClass : classes )
        {
            if( !results.add( aClass ) )
            {
                continue;
            }

            stack.add( aClass );
            while( !stack.isEmpty() )
            {
                Class current = stack.pop();

                Class[] interfaces = current.getInterfaces();
                for( Class aInterface : interfaces )
                {
                    if( !results.contains( aInterface ) )
                    {
                        stack.add( aInterface );
                    }
                }

                Annotation qiMixin = current.getAnnotation( QiMixin.class );
                if( qiMixin != null )
                {
                    results.add( current );
                }
            }
        }

        return results.toArray( BLANK_CLASS_ARRAY );
    }

    final Method getMethod()
    {
        return m_method;
    }

    final Class[] getTargetClasses()
    {
        return m_targetClasses;
    }

    public boolean equals( Object o )
    {
        COUNTER1++;
        if( this == o )
        {
            return true;
        }
        COUNTER2++;

        if( o == null || JoinpointDescriptor.class != o.getClass() )
        {
            return false;
        }
        COUNTER3++;

        JoinpointDescriptor that = (JoinpointDescriptor) o;
        if( ! m_method.equals( that.m_method ) )
        {
            return false;
        }
        COUNTER4++;

        if( m_targetClasses.length != that.m_targetClasses.length )
        {
            return false;
        }
        COUNTER5++;

        return CollectionUtil.isAllPartOf( m_targetClasses, that.m_targetClasses );
    }

    public final int hashCode()
    {
        return m_hashCode;
    }

    public final String toString()
    {
        StringWriter strWriter = new StringWriter();
        strWriter.append( "method [" ).append( m_method.toString() ).append( "], " );
        strWriter.append( "targetClassess[" );
        int i = 0;
        int length = m_targetClasses.length;
        for( Class targetClass : m_targetClasses )
        {
            strWriter.append( targetClass.getName() );

            if( i < length - 1 )
            {
                strWriter.append( ", " );
            }
            i++;
        }
        strWriter.append( "]" );

        return strWriter.toString();
    }
}
