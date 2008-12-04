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

import java.util.Iterator;
import org.qi4j.library.spaces.ConcurrentEntryModificationException;
import org.qi4j.library.spaces.InvalidSpaceTransactionException;
import org.qi4j.library.spaces.SpaceException;
import org.qi4j.library.spaces.SpaceTransaction;

class TransactionImpl
    implements SpaceTransaction, MemoryTransaction
{
    private MultiEntryMap data;
    private MemorySpaceMixin memorySpaceMixin;

    TransactionImpl( MemorySpaceMixin memorySpaceMixin )
    {
        this.memorySpaceMixin = memorySpaceMixin;
        data = new MultiEntryMap();
    }

    public void put( String id, StorageEntry storage )
    {
        if( data == null )
        {
            throw new InvalidSpaceTransactionException( "Transaction has been either been committed or aborted." );
        }
        storage.setUpdated();
        data.put( id, storage );
    }

    public StorageEntry read( String id )
    {
        if( data == null )
        {
            throw new InvalidSpaceTransactionException( "Transaction has been either been committed or aborted." );
        }
        StorageEntry storageEntry = data.read( id );
        if( storageEntry == null )
        {
            StorageEntry existing = memorySpaceMixin.mainStore.read( id );
            storageEntry = new StorageEntry( id, existing.payload, existing.version );
            storageEntry.setLoaded();
            data.put( id, storageEntry );
        }
        if( storageEntry.isRemoved() )
        {
            return null;
        }
        return storageEntry;
    }

    public void commit()
        throws SpaceException
    {
        if( data == null )
        {
            throw new InvalidSpaceTransactionException( "Transaction has been either been committed or aborted." );
        }
        synchronized( memorySpaceMixin.mainStore )
        {
            // Ensure that no changes has been made to any entry.
            Iterator<StorageEntry> iterator = data.iterator();
            while( iterator.hasNext() )
            {
                StorageEntry entry = iterator.next();
                StorageEntry oldEntry = memorySpaceMixin.mainStore.read( entry.identity );
                if( oldEntry != null && oldEntry.version != entry.version )
                {
                    throw new ConcurrentEntryModificationException( entry.identity );
                }
            }
            // Update the store
            iterator = data.iterator();
            while( iterator.hasNext() )
            {
                StorageEntry entry = iterator.next();
                if( entry.isRemoved() )
                {
                    memorySpaceMixin.mainStore.take( entry.identity );
                }
                else if( entry.isUpdated() )
                {
                    memorySpaceMixin.mainStore.take( entry.identity );
                    entry.version = entry.version + 1;
                    memorySpaceMixin.mainStore.put( entry.identity, entry );
                }
                entry.setNoState();
            }
            data = null;
            memorySpaceMixin.removeTransaction( this );
        }
    }

    public void abort()
        throws SpaceException
    {
        data = null;
        memorySpaceMixin.removeTransaction( this );
    }

    public Iterator<StorageEntry> iterator()
    {
        return data.iterator();
    }

    public StorageEntry take( String id )
    {
        StorageEntry existing = memorySpaceMixin.mainStore.read( id );
        if( existing == null )
        {
            StorageEntry txEntry = data.read( id );
            if( txEntry == null || txEntry.isRemoved() )
            {
                return null;
            }
            txEntry.setRemoved();
            return txEntry;
        }
        StorageEntry newEntry = new StorageEntry( id, existing.payload, existing.version );
        newEntry.setRemoved();
        data.put( id, newEntry );
        return newEntry;
    }
}