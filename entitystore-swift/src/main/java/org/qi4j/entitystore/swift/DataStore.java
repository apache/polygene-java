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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * This class handles the Heap Data file.
 * The format of the file is as follows;
 *
 * <code><pre>
 * At OFFSET = 0
 * [cleanShutDown]  1 byte
 * [formatVersion]  4 bytes
 * [noOfEntries]    4 bytes
 * [noOfIDentries]  4 bytes
 *
 * At OFFSET 256
 * [blockSize]     4 bytes
 * [usage]         1 byte    (0=Unused, 1=prime, 2=mirror, 3=primeChanged, 4=mirrorChanged)
 * [instanceVersion] 8 bytes
 * [schemaVersion] 4 bytes
 * [identitySize]  1 byte
 * [identity]      IDENTITY_MAX_LENGTH bytes
 * [mirrorPointer] 8 bytes
 * [primeDataLength] 4 bytes
 * [primeData]     n bytes
 * [mirrorDataLength] 4 bytes
 * [mirrorData]    n bytes
 *
 * At OFFSET 256 + [blockSize]
 * same as above, repeat until [blockSize] == -1 marking end of DataArea.
 * </pre></code>
 * The <b>mirrorPointer</b> points to the mirrorData block.
 */
public class DataStore
{
    static final long DATA_AREA_OFFSET = 256;
    private static final int CURRENT_VERSION = 1;
    private static final String HEAP_DATA_FILENAME = "heap.data";

    private RandomAccessFile dataFile;
    private IdentityFile identityFile;
    private int identityMaxLength;
    private UndoManager undoManager;
    private int entries;
    private File dataDir;

    public DataStore( File dataDirectory, UndoManager undoManager )
        throws IOException
    {
        this.undoManager = undoManager;
        identityMaxLength = 128; // Default value...
        this.dataDir = dataDirectory.getAbsoluteFile();
        dataDir.mkdirs();
        File file = new File( dataDir, HEAP_DATA_FILENAME );
        if( !file.exists() )
        {
            file.createNewFile();
        }
        dataFile = new RandomAccessFile( file, "rw" );
        boolean cleanShutDown;
        if( file.length() > 0 )
        {
            dataFile.seek( 0 );
            cleanShutDown = dataFile.readBoolean();
            dataFile.seek( 0 );
            dataFile.writeBoolean( false );
            dataFile.writeInt( CURRENT_VERSION );  // Write Version.
            entries = dataFile.readInt();
            identityMaxLength = dataFile.readInt();
        }
        else
        {
            cleanShutDown = false;
            dataFile.writeBoolean( false );
            entries = 0;
            dataFile.writeInt( CURRENT_VERSION );  // Write Version.
            dataFile.writeInt( entries );
            dataFile.writeInt( identityMaxLength );
            dataFile.seek( DATA_AREA_OFFSET - 1 );
            dataFile.writeByte( 0 );
        }
        // Ensure full flush, then reopen...
        dataFile.close();

        dataFile = new RandomAccessFile( file, "rw" );

        if( !cleanShutDown )
        {
            reIndex();
        }
        else
        {
            File idDir = new File( dataDir, "idx" );
            try
            {
                identityFile = IdentityFile.use( idDir );
            }
            catch( MalformedIdentityDirectoryException e )
            {
                reIndex();
            }
        }
        if( identityFile.entries() < entries * 2 )
        {
            reIndex();
        }
    }

    RandomAccessFile dataFile()
    {
        return dataFile;
    }

    IdentityFile identityFile()
    {
        return identityFile;
    }

    DataBlock readData( QualifiedIdentity identity ) throws IOException
    {
        long pos = identityFile.find( identity );
        if( pos < 0 )
        {
            return null;
        }
        dataFile.seek( pos );
        dataFile.skipBytes( 4 ); // Skip BlockSize
        byte usage = dataFile.readByte();
        long instanceVersion = dataFile.readLong();
        int schemaVersion = dataFile.readInt();
        QualifiedIdentity existingIdentity = readIdentity();
        if( !existingIdentity.equals( identity ) )
        {
            throw new EntityStoreException( "Inconsistent Data Heap." );
        }
        if( usage == 2 )
        {
            long mirror = dataFile.readLong();
            dataFile.seek( mirror );
        }
        else
        {
            dataFile.skipBytes( 8 ); // skip the MirrorPointer
        }
        int dataSize = dataFile.readInt();
        byte[] data = new byte[dataSize];
        dataFile.read( data );
        return new DataBlock( identity, data, instanceVersion, schemaVersion );
    }

    void putData( DataBlock data )
        throws IOException
    {
        long pos = identityFile.find( data.identity );
        if( pos < 0 )
        {
            pos = addData( data );
            UndoNewIdentityCommand undoNewIdentityCommand = new UndoNewIdentityCommand( data.identity );
            undoManager.saveUndoCommand( undoNewIdentityCommand );
            identityFile.remember( data.identity, pos );
        }
        else
        {
            dataFile.seek( pos );
            int blockSize = dataFile.readInt();
            long usagePointer = dataFile.getFilePointer();
            byte usage = dataFile.readByte();
            int dataAreaSize = ( blockSize - 64 ) / 2;
            if( dataAreaSize < ( data.data.length + 4 ) )
            {
                long newPosition = addData( data );
                UndoModifyCommand undoModifyCommand = new UndoModifyCommand( pos, usage, data.instanceVersion, data.schemaVersion );
                undoManager.saveUndoCommand( undoModifyCommand );
                dataFile.seek( usagePointer );
                dataFile.writeByte(0);
                UndoDropIdentityCommand undoDropIdentityCommand = new UndoDropIdentityCommand( data.identity, pos );
                undoManager.saveUndoCommand( undoDropIdentityCommand );
                identityFile.remember( data.identity, newPosition );
            }
            else
            {
                dataFile.skipBytes( 12 ); // Skip instanceVersion and schemaVersion
                QualifiedIdentity existingIdentity = readIdentity();
                if( !existingIdentity.equals( data.identity ) )
                {
                    throw new EntityStoreException( "Inconsistent Data Heap: was " + existingIdentity + ", expected " + data.identity );
                }
                long mirror = dataFile.readLong();
                if( usage == 1 )
                {
                    dataFile.seek( mirror );
                }
                UndoModifyCommand undoModifyCommand = new UndoModifyCommand( pos, usage, data.instanceVersion, data.schemaVersion );
                undoManager.saveUndoCommand( undoModifyCommand );

                dataFile.writeInt( data.data.length );
                dataFile.write( data.data );
                dataFile.seek( usagePointer );
                dataFile.writeByte( usage == 1 ? 2 : 1 );
            }
        }
    }

    public void delete( QualifiedIdentity identity )
        throws IOException
    {
        long pos = identityFile.find( identity );
        if( pos < 0 )
        {
            // Doesn't exist.
            return;
        }
        dataFile.seek( pos );
        dataFile.skipBytes( 4 ); // Skip BlockSize
        byte usage = dataFile.readByte();
        if( usage == 0 )
        {
            // Not used?? Why is the IdentityFile pointing to it then?? Should the following line actually be
            // executed here.
            //    identityFile.drop( identity );
            return;
        }
        UndoDropIdentityCommand undoDropIdentityCommand = new UndoDropIdentityCommand( identity, pos );
        undoManager.saveUndoCommand( undoDropIdentityCommand );

        UndoDeleteCommand undoDeleteCommand = new UndoDeleteCommand( pos, usage );
        undoManager.saveUndoCommand( undoDeleteCommand );

        identityFile.drop( identity );
        dataFile.skipBytes( -1 );
        dataFile.writeByte( 0 );
    }

    void close() throws IOException
    {
        identityFile.close();
        dataFile.seek( 0 );
        dataFile.writeBoolean( true );
        dataFile.writeInt( entries );
        dataFile.close();
    }

    private long addData( DataBlock data )
        throws IOException
    {
        dataFile.seek( dataFile.length() );
        long blockStart = dataFile.getFilePointer();
        int dataAreaSize = ( data.data.length + 4 ) * 4;
        UndoExtendCommand undoExtendCommand = new UndoExtendCommand( blockStart );
        undoManager.saveUndoCommand( undoExtendCommand );

        dataFile.writeInt( dataAreaSize + identityMaxLength + 26 );
        dataFile.writeByte( 3 ); // In-progress
        dataFile.writeLong( data.instanceVersion );
        dataFile.writeInt( data.schemaVersion );
        writeIdentity( data.identity );

        long mirrorPosition = blockStart + dataAreaSize / 2;
        dataFile.writeLong( mirrorPosition );
        dataFile.writeInt( data.data.length );
        dataFile.write( data.data );
        return blockStart;
    }

    private void compact()
        throws IOException
    {
/*
        File newFileName = new File( dataDir, "heap-compacting.data" );
        RandomAccessFile newFile = new RandomAccessFile( newFileName, "rw" );
        File oldFileName = new File( dataDir, "heap.data" );
        RandomAccessFile oldFile = new RandomAccessFile( oldFileName, "r" );

        oldFile.seek( DATA_AREA_OFFSET ); // Skip initial bytes;
        newFile.seek( DATA_AREA_OFFSET ); // Skip initial bytes;

        int counter = 0;

        // Move the Records!!

        entries = counter;

        newFile.writeBoolean( false );
        newFile.writeInt( CURRENT_VERSION );  // Write Version.

        newFile.writeInt( entries );
        reIndex( dataDir );
        dataFile.close();
        newFile.close();

        File standardFilename = new File( dataDir, "heap.data" );
        newFileName.renameTo( standardFilename );
        dataFile = new RandomAccessFile( standardFilename, "rw" );
*/
    }

    private void reIndex()
        throws IOException
    {
        identityFile = IdentityFile.create( new File( dataDir, "idx" ), identityMaxLength + 16, entries < 10000 ? 10000 : entries * 2 );

        dataFile.seek( DATA_AREA_OFFSET );
        while( dataFile.getFilePointer() < dataFile.length() )
        {
            long blockStart = dataFile.getFilePointer();
            int blockSize = dataFile.readInt();
            byte usage = dataFile.readByte();
            dataFile.skipBytes( 12 ); // Skip instanceVersion and schemaVersion
            QualifiedIdentity identity = readIdentity();
            if( usage != 0 )
            {
                identityFile.remember( identity, blockStart );
            }
            dataFile.seek( blockStart + blockSize );
        }
    }

    private void writeIdentity( QualifiedIdentity identity ) throws IOException
    {
        byte[] idBytes = identity.toString().getBytes();
        if( idBytes.length > identityMaxLength )
        {
            throw new EntityStoreException( "Identity is too long. Only " + identityMaxLength + " characters are allowed in this EntityStore." );
        }
        byte[] id = new byte[identityMaxLength];
        System.arraycopy( idBytes, 0, id, 0, idBytes.length );
        dataFile.writeByte( idBytes.length );
        dataFile.write( id );
    }

    private QualifiedIdentity readIdentity()
        throws IOException
    {
        int idSize = dataFile.readByte();
        if( idSize < 0 )
        {
            idSize = idSize + 256;  // Fix 2's-complement negative values of bytes into unsigned 8 bit.
        }
        byte[] idData = new byte[idSize];
        dataFile.read( idData );
        dataFile.skipBytes( identityMaxLength - idSize );
        return QualifiedIdentity.parseQualifiedIdentity( new String( idData ) );
    }

    public Iterator<QualifiedIdentity> iterator()
    {
        File file = new File( dataDir, HEAP_DATA_FILENAME );
        try
        {
            RandomAccessFile store = new RandomAccessFile( file, "r" );
            return new StoreIterator( store, identityMaxLength );
        }
        catch( FileNotFoundException e )
        {
            return new NullIterator();
        }
    }

    private static class NullIterator
        implements Iterator<QualifiedIdentity>
    {
        public boolean hasNext()
        {
            return false;
        }

        public QualifiedIdentity next()
        {
            return null;
        }

        public void remove()
        {
        }
    }
}
