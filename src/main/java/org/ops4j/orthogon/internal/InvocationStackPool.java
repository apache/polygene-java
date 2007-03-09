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
import java.util.LinkedList;
import org.ops4j.lang.NullArgumentException;

/**
 * TODO: Removal of entries that are not used for a period of time
 * TODO: Thread safe
 */
public class InvocationStackPool
{
    private HashMap<JoinpointDescriptor, LinkedList<PoolEntry>> m_pool;

    private InvocationStackFactory m_factory;

    public InvocationStackPool( InvocationStackFactory factory )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( factory, "factory" );
        m_pool = new HashMap<JoinpointDescriptor, LinkedList<PoolEntry>>();
        m_factory = factory;
    }

    public InvocationStack getInvocationStack( JoinpointDescriptor descriptor )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( descriptor, "descriptor" );

        LinkedList<PoolEntry> entries = m_pool.get( descriptor );
        if( entries == null )
        {
            entries = new LinkedList<PoolEntry>();
            m_pool.put( descriptor, entries );
        }

        if( entries.isEmpty() )
        {
            return m_factory.create( descriptor );
        }

        PoolEntry poolEntry = entries.remove();
        return poolEntry.stack;
    }

    public void release( InvocationStack stack )
    {
        JoinpointDescriptor descriptor = stack.getDescriptor();
        PoolEntry entry = new PoolEntry( stack );
        LinkedList<PoolEntry> list = m_pool.get( descriptor );
        if( list == null )
        {
            list = new LinkedList<PoolEntry>();
            m_pool.put( descriptor, list );
        }
        list.addFirst( entry );
    }

    private static class PoolEntry
    {
        InvocationStack stack;
        long lastused;

        public PoolEntry( InvocationStack stack )
        {
            this.stack = stack;
            lastused = System.currentTimeMillis();
        }
    }
}
