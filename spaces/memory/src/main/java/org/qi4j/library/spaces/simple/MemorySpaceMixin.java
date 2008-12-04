/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.spaces.simple;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Stack;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.spaces.SpaceTransaction;

public class MemorySpaceMixin
    implements Space
{
    private StackThreadLocal<MemoryTransaction> transactionStack;

    final MultiEntryMap mainStore;


    public MemorySpaceMixin()
    {
        mainStore = new MultiEntryMap();
        transactionStack = new StackThreadLocal<MemoryTransaction>();
    }

    public void write( String id, Serializable entry )
    {
        MemoryTransaction transaction = currentTransaction();
        StorageEntry storage = new StorageEntry( id, entry );
        transaction.put( id, storage );
    }

    public Serializable take( String id, long timeout )
    {
        long now = System.currentTimeMillis();
        long giveUpAt = now + timeout;
        MemoryTransaction transaction = currentTransaction();
        StorageEntry entry;
        synchronized( transaction )
        {
            do
            {
                entry = transaction.take( id );
                now = System.currentTimeMillis();
            }
            while( entry == null && now < giveUpAt );
            if( entry == null )
            {
                return null;
            }
            return entry.data();
        }
    }

    public Serializable takeIfExists( String id )
    {
        return take( id, 0 );
    }

    public Serializable read( String id, long timeout )
    {
        long now = System.currentTimeMillis();
        long giveUpAt = now + timeout;
        MemoryTransaction transaction = currentTransaction();
        StorageEntry entry;
        synchronized( transaction )
        {
            do
            {
                entry = transaction.read( id );
                now = System.currentTimeMillis();
            }
            while( entry == null && now < giveUpAt );

            if( entry == null )
            {
                return null;
            }
            return entry.data();
        }
    }

    public Serializable readIfExists( String id )
    {
        return read( id, 0 );
    }

    public SpaceTransaction newTransaction()
    {
        Stack<MemoryTransaction> curStack = transactionStack.get();
        TransactionImpl tx = new TransactionImpl( this );
        curStack.push( tx );
        return tx;
    }

    public boolean isReady()
    {
        return true;
    }

    public Iterator<Serializable> iterator()
    {
        MemoryTransaction curTransaction = currentTransaction();
        Iterator<StorageEntry> iterator = mainStore.iterator();
        Iterator<StorageEntry> inTx = curTransaction.iterator();
        return new ChainedIterator<StorageEntry, Serializable>( new Deserialization(), inTx, iterator );
    }

    MemoryTransaction currentTransaction()
    {
        Stack<MemoryTransaction> curStack = transactionStack.get();
        if( curStack.size() == 0 )
        {
            return mainStore;
        }
        MemoryTransaction memoryTransaction = curStack.peek();
        if( memoryTransaction == null )
        {
            return mainStore;
        }
        return memoryTransaction;
    }

    void removeTransaction( TransactionImpl transaction )
    {
        Stack<MemoryTransaction> curStack = transactionStack.get();
        curStack.remove( transaction );
    }

    private static class Deserialization
        implements ChainedIterator.Converter<StorageEntry, Serializable>
    {

        public Serializable convert( StorageEntry data )
        {
            return data.data();
        }
    }
}
