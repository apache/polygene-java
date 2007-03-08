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
import java.util.Arrays;
import org.ops4j.lang.NullArgumentException;

final class JoinpointDescriptor
{
    private final Class[] m_targetClasses;

    private final Method m_method;

    JoinpointDescriptor( Method method, Class... targetClasses )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( method, "method" );
        NullArgumentException.validateNotNull( targetClasses, "targetClasses" );
        m_method = method;
        m_targetClasses = targetClasses;
    }

    Class[] getTargetClasses()
    {
        return m_targetClasses;
    }

    Method getMethod()
    {
        return m_method;
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        JoinpointDescriptor that = (JoinpointDescriptor) o;

        if( !m_method.equals( that.m_method ) )
        {
            return false;
        }
        if( !Arrays.equals( m_targetClasses, that.m_targetClasses ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = Arrays.hashCode( m_targetClasses );
        result = 31 * result + m_method.hashCode();
        return result;
    }
}
