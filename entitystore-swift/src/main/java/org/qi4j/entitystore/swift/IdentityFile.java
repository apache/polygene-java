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
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * For Slot 0
 * [version]     -  4 bytes
 * [noOfEntries] -  4 bytes
 * [slotSize]    -  4 bytes
 *
 * For Slot 1..n
 * [isExtended]  -  1 byte
 * [position]    -  8 bytes
 * [identity]    -  [slotSize-16] bytes
 */
public class IdentityFile
{
    private static final int CURRENT_VERSION = 1;

    private RandomAccessFile identityStore;
    private int entries;
    private int slotSize;
    private boolean closed;
    private BucketManager bucketManager;

    private IdentityFile( RandomAccessFile store, File bucketDir, int slotSize, int entries )
        throws IOException
    {
        this.closed = false;
        identityStore = store;
        bucketManager = new BucketManager( bucketDir );
        this.slotSize = slotSize;
        this.entries = entries;
    }

    int entries()
    {
        return entries;
    }

    long find( QualifiedIdentity identity )
        throws IOException
    {
        if( closed )
        {
            throw new IdentityFileClosedException();
        }
        if( identity.toString().length() > slotSize - 16 )
        {
            throw new IdentityTooLongException( identity );
        }
        final int slot = getSlot( identity );
        identityStore.seek( slot * slotSize );
        boolean isExtended = identityStore.readBoolean();
        if( !isExtended )
        {
            long pos = identityStore.readLong();
            String idString = identityStore.readUTF();
            if( idString.length() == 0 )
            {
                return -1;
            }
            QualifiedIdentity id = QualifiedIdentity.parseQualifiedIdentity( idString );
            if( id.equals( identity ) )
            {
                return pos;
            }
            return -1;
        }
        RandomAccessFile buckets = bucketManager.get( slot );
        int next = 0;
        while( next * slotSize < buckets.length() )
        {
            buckets.seek( next * slotSize );
            boolean isUsed = buckets.readBoolean();
            long pos = buckets.readLong();
            QualifiedIdentity id = QualifiedIdentity.parseQualifiedIdentity( buckets.readUTF() );
            if( isUsed && id.equals( identity ) )
            {
                return pos;
            }
            next++;
        }
        return -1;
    }

    void remember( QualifiedIdentity identity, long pos )
        throws IOException
    {
        if( closed )
        {
            throw new IdentityFileClosedException();
        }
        if( identity.toString().length() > slotSize - 16 )
        {
            throw new IdentityTooLongException( identity );
        }
        final int slot = getSlot( identity );
        identityStore.seek( slot * slotSize );
        boolean isExtended = identityStore.readBoolean();
        if( isExtended )
        {
            RandomAccessFile bucket = bucketManager.get( slot );
            bucket.seek( 0 );
            int next = 0;
            while( next * slotSize < bucket.length() )
            {
                bucket.seek( next * slotSize );
                boolean isUsed = bucket.readBoolean();
                if( !isUsed )
                {
                    break;
                }
                next++;
            }
            bucket.seek( next * slotSize );
            bucket.writeBoolean( true );
            bucket.writeLong( pos );
            bucket.writeUTF( identity.toString() );
            fillExtras( bucket, next, slotSize );
        }
        else
        {
            long existingPos = identityStore.readLong();
            if( existingPos == -1 )
            {
                // Not used yet.
                identityStore.seek( slot * slotSize );
                identityStore.writeBoolean( false );
                identityStore.writeLong( pos );
                identityStore.writeUTF( identity.toString() );
            }
            else
            {
                // Move existing record over to a new bucket.
                RandomAccessFile bucket = bucketManager.get( slot );
                bucket.seek( 0 );
                bucket.writeBoolean( true );
                bucket.writeLong( existingPos );
                bucket.writeUTF( identityStore.readUTF() );
                fillExtras( bucket, 0, slotSize );
                bucket.seek( slotSize );
                bucket.writeBoolean( true );
                bucket.writeLong( pos );
                bucket.writeUTF( identity.toString() );
                fillExtras( bucket, 1, slotSize );
                identityStore.seek( slot * slotSize );
                identityStore.writeBoolean( true );
                identityStore.writeLong( -1 );
                identityStore.writeUTF( "" );
                fillExtras( identityStore, slot, slotSize );
            }
        }
    }

    void drop( QualifiedIdentity identity )
        throws IOException
    {
        if( closed )
        {
            throw new IdentityFileClosedException();
        }
        if( identity.toString().length() > slotSize - 16 )
        {
            throw new IdentityTooLongException( identity );
        }
        final int slot = getSlot( identity );
        identityStore.seek( slot * slotSize );
        boolean isExtended = identityStore.readBoolean();
        if( isExtended )
        {
            RandomAccessFile buckets = bucketManager.get( slot );
            int next = 0;
            while( next * slotSize < buckets.length() )
            {
                buckets.seek( next * slotSize );
                boolean isUsed = buckets.readBoolean();
                buckets.readLong();  //ignore, should probably be changed to skip(8);
                QualifiedIdentity id = QualifiedIdentity.parseQualifiedIdentity( buckets.readUTF() );
                if( isUsed && id.equals( identity ) )
                {
                    buckets.seek( next * slotSize );
                    buckets.writeBoolean( false );
                    return;
                }
                next++;
            }
        }
        else
        {
            identityStore.readLong(); // ignore the pos
            QualifiedIdentity storedId = QualifiedIdentity.parseQualifiedIdentity( identityStore.readUTF() );
            if( identity.equals( storedId ) )
            {
                // found, no erase.
                identityStore.seek( slot * slotSize );
                identityStore.writeBoolean( false );
                identityStore.writeLong( -1 );
                identityStore.writeUTF( "" );
                fillExtras( identityStore, slot, slotSize );
            }
        }
    }

    private int getSlot( QualifiedIdentity identity )
    {
        int hashCode = identity.hashCode();
        hashCode = hashCode < 0 ? -hashCode : hashCode;
        int slot = ( hashCode % entries ) + 1;
        return slot;
    }

    public void close()
        throws IOException
    {
        bucketManager.close();
        identityStore.close();
        closed = true;
    }

    private static void initialize( RandomAccessFile newFile, int entries, int slotSize )
        throws IOException
    {
        for( int i = 1; i <= entries + 1; i++ )
        {
            newFile.seek( i * slotSize );
            newFile.writeBoolean( false );  // Extended
            newFile.writeLong( -1 );        // Position
            newFile.writeUTF( "" );         // Identity
            fillExtras( newFile, i, slotSize );
        }
        newFile.seek( 0 );
        newFile.writeInt( CURRENT_VERSION );
        newFile.writeInt( entries );
        newFile.writeInt( slotSize );
    }

    private static void fillExtras( RandomAccessFile accessFile, int slot, int slotSize )
        throws IOException
    {
        long pointer = accessFile.getFilePointer();
        long fillTo = ( slot + 1 ) * slotSize - 1;
        long arraysize = fillTo - pointer;
        if( arraysize < 0 )
        {
            System.err.println( "Negative Array Size detected:" + arraysize );
        }
        byte[] extras = new byte[(int) arraysize];
        accessFile.write( extras );
    }

    public static IdentityFile use( File identityDir )
        throws MalformedIdentityDirectoryException, IOException
    {
        File idFile = new File( identityDir, "id-hash.data" );
        if( !idFile.exists() )
        {
            throw new MalformedIdentityDirectoryException( identityDir );
        }
        File bucketDir = new File( identityDir, "buckets" );
        if( !bucketDir.exists() )
        {
            throw new MalformedIdentityDirectoryException( identityDir );
        }
        RandomAccessFile store = new RandomAccessFile( idFile, "rw" );
        int version = store.readInt(); // Read Version
        int entries = store.readInt(); // Read entries
        int slotSize = store.readInt(); // Read slotSize
        return new IdentityFile( store, bucketDir, slotSize, entries );
    }

    public static IdentityFile create( File identityDir, int slotSize, int idEntries )
        throws IOException
    {
        FileUtils.delete( identityDir );
        identityDir.mkdirs();
        File idFile = new File( identityDir, "id-hash.data" );
        RandomAccessFile store = new RandomAccessFile( idFile, "rw" );
        initialize( store, idEntries, slotSize );
        File bucketDir = new File( identityDir, "buckets" );
        return new IdentityFile( store, bucketDir, slotSize, idEntries );
    }
}
