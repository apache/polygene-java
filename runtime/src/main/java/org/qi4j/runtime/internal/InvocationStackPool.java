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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;

/**
 * TODO: Removal of entries that are not used for a period of time
 * TODO: Thread safe
 */
final class InvocationStackPool
{
    private final InvocationStackFactory m_factory;
    private final Map<JoinpointDescriptor, List<InvocationStack>> m_pool;
    private final Set<JoinpointDescriptor> m_noInvocationStack;

    InvocationStackPool( InvocationStackFactory factory )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( factory, "factory" );

        m_factory = factory;
        m_pool = new HashMap<JoinpointDescriptor, List<InvocationStack>>();
        m_noInvocationStack = new HashSet<JoinpointDescriptor>();
    }

    final InvocationStack getInvocationStack( JoinpointDescriptor descriptor )
    {
        if( descriptor == null )
        {
            return null;
        }

        synchronized( m_noInvocationStack )
        {
            if( m_noInvocationStack.contains( descriptor ) )
            {
                return null;
            }
        }

        synchronized( this )
        {
            List<InvocationStack> entries = m_pool.get( descriptor );
            if( entries != null && !entries.isEmpty() )
            {
                return entries.remove( 0 );
            }
        }

        InvocationStack stack = m_factory.create( descriptor );
        if( stack == null )
        {
            synchronized( m_noInvocationStack )
            {
                m_noInvocationStack.add( descriptor );
            }
        }

        return stack;
    }

    final void release( InvocationStack stack )
    {
        if( stack == null )
        {
            return;
        }

        JoinpointDescriptor descriptor = stack.getDescriptor();
        synchronized( this )
        {
            List<InvocationStack> entries = m_pool.get( descriptor );
            if( entries == null )
            {
                entries = new ArrayList<InvocationStack>();
                m_pool.put( descriptor, entries );
            }
            entries.add( 0, stack );
        }
    }
}
