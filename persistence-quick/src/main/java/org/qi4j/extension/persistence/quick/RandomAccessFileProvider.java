/*
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.extension.persistence.quick;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.spi.serialization.SerializablePersistenceSpi;
import org.qi4j.spi.serialization.SerializedObject;

public class RandomAccessFileProvider
    implements SerializablePersistenceSpi
{
    private static final String INDEX_FILE = "raf-index.qi4j";
    private static final String DATA_FILE = "raf-data.qi4j";

    /**
     * The index file contains pointers to where the data resides.
     * The Index file is a simple list of records, where each record looks like;
     * <table>
     * <tr><td>Offset</td><td>Name</td><td>Notes</td></tr>
     * <tr><td>0</td><td>active</td><td>If this field == 0, then the record is not active and will be ignored.</td></tr>
     * <tr><td>1</td><td>dataLocation</td><td>This points to the location within the data file, where the record resides. If this field == -1, then this marks the End-Of-File.</td></tr>
     * <tr><td>9</td><td>valueLocation</td><td>This field points to the value section of the data record in the data file.</td></tr>
     * <tr><td>17</td><td>dataLength</td><td>The length of the value field in the data file.</td></tr>
     * <tr><td>21</td><td>dataSize</td><td>The available space in the value field in the data file.</td></tr>
     * <tr><td>25</td><td>identity</td><td>The identity is UTF-8 encoded binary stream. The first two bytes are the
     * number of bytes to follow, read as unsigned short, limiting the identity to 65535 UTF-8 encoded bytes.</td></tr>
     * </table>
     */
    private RandomAccessFile indexStore;

    private RandomAccessFile dataStore;
    private HashMap<String, RandomAccessRecord> index;
    private boolean validatingStructure;

    /**
     * The file position of the EOF marker in the data file.
     */
    private long lastDataPosition;

    /**
     * The file position of the EOF marker in the index file.
     */
    private long lastIndexPosition;

    public RandomAccessFileProvider( File storageDir )
    {
        validatingStructure = true;
        initializeStore( storageDir );
    }

    public void putInstance( String identity, Map<Class, SerializedObject> mixins )
    {
        try
        {
            byte[] data = serializeData( mixins );

            RandomAccessRecord oldRecord = index.get( identity );
            long indexPosition;
            boolean indexFileIsExtendedAndNeedNewEndOfFileMarker;
            // Prepare the maxSize to the data size, and allow for override when we reuse the space.
            int maxSize = data.length;
            if( oldRecord != null )
            {
                dataStore.seek( oldRecord.valuePointer );
                if( oldRecord.dataLength >= data.length )
                {
                    // write into the existing space for the record.
                    // this is a performance optimization, as the data often will not grow.
                    maxSize = oldRecord.maxSize;
                    dataStore.writeInt( data.length );
                    dataStore.write( data );
                    return;
                }
                else
                {
                    // The new data does not fit into available space.
                    dataStore.writeBoolean( false );   // mark space as unused.
                    dataStore.seek( lastDataPosition );  // Move to end of file
                    // FALL-THRU as if it was a unknown record, but set the Index position to the known
                    // index position, as that should not be written into a new location and a new end of the
                    // file should not be marked.
                    indexPosition = oldRecord.indexPointer;
                    indexFileIsExtendedAndNeedNewEndOfFileMarker = false;
                }
            }
            else
            {
                // Instance does not exist
                indexPosition = lastIndexPosition;
                indexFileIsExtendedAndNeedNewEndOfFileMarker = true;
            }
            long recordPos = lastDataPosition;
            long valuePos = writeDataRecord( recordPos, identity, data );
            lastDataPosition = dataStore.getFilePointer();
            RandomAccessRecord newRecord = new RandomAccessRecord( identity, indexPosition, true, recordPos, valuePos, data.length, maxSize );
            writeIndexRecord( indexPosition, newRecord );
            if( indexFileIsExtendedAndNeedNewEndOfFileMarker )
            {
                markEndOfIndex();
            }
            // Update the in-memory index.
            index.put( identity, newRecord );
        }
        catch( IOException e )
        {
            throw new PersistenceFailedException( "Unable to persist object [" + identity + "].", e );
        }
    }

    public Map<Class, SerializedObject> getInstance( String identity )
    {
        RandomAccessRecord record = index.get( identity );
        if( record == null )
        {
            return null;
        }
        Map<Class, SerializedObject> mixins = null;
        try
        {
            if( validatingStructure )
            {
                dataStore.seek( record.dataPointer );
                boolean active = dataStore.readBoolean();
                if( !active )
                {
                    throw new PersistenceValidationException( "Record found in index, but marked not active in data.", record );
                }
                String id = dataStore.readUTF();
                if( !identity.equals( id ) )
                {
                    throw new PersistenceValidationException( "Index of [" + identity + "] points to object [" + id + "]", record );
                }
            }
            dataStore.seek( record.valuePointer );
            int size = dataStore.readInt();
            byte[] data = new byte[size];
            dataStore.read( data );
            mixins = deserializeData( data );
        }
        catch( IOException e )
        {
            throw new CompositeInstantiationException( "Unable to retrieve object [" + identity + "].", e );
        }
        catch( ClassNotFoundException e )
        {
            throw new CompositeInstantiationException( "Unable to retrieve object [" + identity + "].", e );
        }
        finally
        {
        }
        return mixins;
    }

    public void removeInstance( String identity )
    {
        RandomAccessRecord record = index.remove( identity );
        if( record == null )
        {
            return;
        }
        try
        {
            indexStore.seek( record.indexPointer );
            indexStore.writeBoolean( false );
            dataStore.seek( record.dataPointer );
            dataStore.writeBoolean( false );
        }
        catch( IOException e )
        {
            throw new ObjectDeletionException( "Unable to remove object [" + identity + "]", e );
        }
    }

    public void close()
        throws IOException
    {
        indexStore.close();
        dataStore.close();
    }

    public boolean isValidatingStructure()
    {
        return validatingStructure;
    }

    public void setValidatingStructure( boolean validatingStructure )
    {
        this.validatingStructure = validatingStructure;
    }

    private long writeDataRecord( long recordPos, String identity, byte[] data )
        throws IOException
    {
        dataStore.seek( recordPos );
        dataStore.writeBoolean( true );
        dataStore.writeUTF( identity );
        long valuePos = dataStore.getFilePointer();
        dataStore.writeInt( data.length );
        dataStore.write( data );
        return valuePos;
    }

    private void writeIndexRecord( long position, RandomAccessRecord record )
        throws IOException
    {
        indexStore.seek( position );
        indexStore.writeBoolean( record.active );
        indexStore.writeLong( record.dataPointer );
        indexStore.writeLong( record.valuePointer );
        indexStore.writeInt( record.dataLength );
        indexStore.writeInt( record.maxSize );
        indexStore.writeUTF( record.identity );
    }

    private RandomAccessRecord readIndexRecord( long position )
        throws IOException
    {
        if( indexStore.length() <= position )
        {
            return null;
        }
        indexStore.seek( position );
        try
        {
            boolean active = indexStore.readBoolean();
            long recordPos = indexStore.readLong();
            long valuePos = indexStore.readLong();
            int dataLength = indexStore.readInt();
            int maxSize = indexStore.readInt();
            String identity = indexStore.readUTF();
            RandomAccessRecord record = new RandomAccessRecord( identity, position, active, recordPos, valuePos, dataLength, maxSize );
            return record;
        }
        catch( IOException e )
        {
            System.err.println( "Index Starting position: " + position );
            System.err.println( "Index Current  position: " + indexStore.getFilePointer() );
            throw e;
        }
    }

    private void markEndOfIndex()
        throws IOException
    {
        lastIndexPosition = indexStore.getFilePointer();
        RandomAccessRecord record = new RandomAccessRecord( "", lastIndexPosition, true, -1, -1, 0, 0 );
        writeIndexRecord( lastIndexPosition, record );
    }

    private Map<Class, SerializedObject> deserializeData( byte[] data )
        throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try
        {
            bais = new ByteArrayInputStream( data );
            ois = new ObjectInputStream( bais );
            Map<Class, SerializedObject> mixinsmixins = (Map<Class, SerializedObject>) ois.readObject();
            return mixinsmixins;
        }
        finally
        {
            close( ois );
            close( bais );
        }
    }

    private byte[] serializeData( Map<Class, SerializedObject> mixins )
        throws IOException
    {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try
        {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream( baos );
            oos.writeObject( mixins );
            oos.flush();
            byte[] data = baos.toByteArray();
            return data;
        }
        finally
        {
            close( oos );
            close( baos );
        }
    }


    private void initializeStore( File storageDir )
    {
        File indexFile = new File( storageDir, INDEX_FILE );
        try
        {
            indexStore = new RandomAccessFile( indexFile, "rwd" );
        }
        catch( FileNotFoundException e )
        {
            throw new PersistenceInitializationException( "Unable to establish index file: " + indexFile, e );
        }
        File dataFile = new File( storageDir, DATA_FILE );
        try
        {
            dataStore = new RandomAccessFile( dataFile, "rwd" );
        }
        catch( FileNotFoundException e )
        {
            throw new PersistenceInitializationException( "Unable to establish data file: " + dataFile, e );
        }
        index = readIndex();
    }

    private HashMap<String, RandomAccessRecord> readIndex()
    {
        try
        {
            HashMap<String, RandomAccessRecord> result = new HashMap<String, RandomAccessRecord>();
            while( true )
            {
                long indexPointer = indexStore.getFilePointer();
                RandomAccessRecord record = readIndexRecord( indexPointer );
                if( record == null )
                {
                    break;
                }
                if( record.active )
                {
                    if( record.dataPointer == -1 )
                    {
                        break;
                    }
                    result.put( record.identity, record );
                }
            }
            return result;
        }
        catch( IOException e )
        {
            throw new ObjectIndexingException( "Index system unable to initialize", e );
        }
    }

    private void close( OutputStream stream )
    {
        if( stream != null )
        {
            try
            {
                stream.close();
            }
            catch( IOException e )
            {
                // ignore...
            }
        }
    }

    private void close( InputStream stream )
    {
        if( stream != null )
        {
            try
            {
                stream.close();
            }
            catch( IOException e )
            {
                // ignore
            }
        }
    }
}
