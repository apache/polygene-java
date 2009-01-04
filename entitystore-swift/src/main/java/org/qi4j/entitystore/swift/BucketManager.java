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
package org.qi4j.entitystore.swift;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import org.qi4j.spi.entity.EntityStoreException;

public class BucketManager
    implements Runnable
{
    private HashMap<Integer, LruEntry> cache;
    private File bucketdir;
    private Thread cleanUpThread;
    private boolean running;

    public BucketManager( File bucketdir )
    {
        cache = new HashMap<Integer, LruEntry>();
        this.bucketdir = bucketdir;
        bucketdir.mkdirs();
        cleanUpThread = new Thread( this, "QuickEntityStore-cleanup" );
        cleanUpThread.start();
    }

    synchronized RandomAccessFile get( int hash )
        throws IOException
    {
        LruEntry entry = cache.get( hash );
        if( entry != null )
        {
            return entry.file;
        }
        File bucketFile = new File( bucketdir, Integer.toHexString( hash ) );
        RandomAccessFile bucket = new RandomAccessFile( bucketFile, "rw" );
        entry = new LruEntry( bucket, hash );
        cache.put( hash, entry );
        return bucket;
    }

    synchronized void close()
        throws IOException
    {
        running = false;
        cleanUpThread.interrupt();
        for( LruEntry entry : cache.values() )
        {
            entry.file.close();
        }
    }

    private void cleanUp()
        throws IOException
    {
        if( cache.size() < 30 )
        {
            return;
        }
        LinkedList<LruEntry> sorting = new LinkedList<LruEntry>();
        sorting.addAll( cache.values() );
        Collections.sort( sorting, new Comparator<LruEntry>()
        {
            public int compare( LruEntry lruEntry1, LruEntry lruEntry2 )
            {
                if( lruEntry1.created == lruEntry2.created )
                {
                    return 0;
                }
                if( lruEntry1.created > lruEntry2.created )
                {
                    return 1;
                }

                return -1;
            }
        } );
        while( cache.size() > 20 )
        {
            LruEntry entry = sorting.removeFirst(); // Check if this is at the right end;
            cache.remove( entry.hash );
        }
    }

    public void run()
    {
        running = true;
        try
        {
            while( running )
            {
                synchronized( this )
                {
                    wait( 15000 );
                    cleanUp();
                }
            }
        }
        catch( InterruptedException e )
        {
            // ignore, normal shutdown
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "What the hell!!!???", e );
        }
    }

    private static class LruEntry
    {
        private long created;
        private RandomAccessFile file;
        private int hash;

        public LruEntry( RandomAccessFile bucket, int hash )
        {
            this.file = bucket;
            this.hash = hash;
            created = System.currentTimeMillis();
        }
    }
}
