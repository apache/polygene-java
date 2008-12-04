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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MultiEntryMap
    implements MemoryTransaction
{
    private final HashMap<String, ArrayList<StorageEntry>> store;

    public MultiEntryMap()
    {
        store = new HashMap<String, ArrayList<StorageEntry>>();
    }

    public void put( String id, StorageEntry storage )
    {
        synchronized( store )
        {
            ArrayList<StorageEntry> entries = store.get( id );
            if( entries == null )
            {
                entries = new ArrayList<StorageEntry>();
            }
            entries.add( storage );
        }
    }

    public StorageEntry read( String id )
    {
        synchronized( store )
        {
            ArrayList<StorageEntry> entries = store.get( id );
            if( entries == null )
            {
                return null;
            }
            return entries.get( 0 );
        }
    }

    public Iterator<StorageEntry> iterator()
    {
        synchronized( this )
        {
            ArrayList<StorageEntry> result = new ArrayList<StorageEntry>();
            for( String id : store.keySet() )
            {
                ArrayList<StorageEntry> entries = store.get( id );
                result.addAll( entries );
            }
            return result.iterator();
        }
    }

    public StorageEntry take( String id )
    {
        synchronized( store )
        {
            ArrayList<StorageEntry> entries = store.get( id );
            if( entries == null )
            {
                return null;
            }
            StorageEntry entry = entries.remove( 0 );
            if( entries.isEmpty() )
            {
                store.remove( id );
            }
            return entry;
        }
    }
}
