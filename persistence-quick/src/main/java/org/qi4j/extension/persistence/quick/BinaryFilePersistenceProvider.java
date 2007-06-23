/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.MarshalledObject;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.persistence.ObjectNotFoundException;
import org.qi4j.spi.persistence.SerializablePersistenceSpi;
import org.qi4j.spi.persistence.SerializedObject;

public final class BinaryFilePersistenceProvider
    implements SerializablePersistenceSpi
{
    private static final String INDEX_FILE = "oos-index.qi4j";
    private File storageDir;
    private Properties fileMap;
    private Random random;

    public BinaryFilePersistenceProvider( File aStorageDir )
    {
        random = new Random();
        storageDir = aStorageDir;
        loadIndexFile();
    }

    public void putInstance( String anId, Map<Class, SerializedObject> mixins )
    {
        synchronized( this )
        {
            ObjectOutputStream oos = null;
            FileOutputStream fos = null;
            try
            {
                File storage = getStorageFileForId( anId );
                fos = new FileOutputStream( storage );
                oos = new ObjectOutputStream( fos );
                oos.writeObject( anId );
                oos.writeObject( mixins );
                oos.flush();
            }
            catch( IOException e )
            {
                // TODO; How to handle exceptions?
                e.printStackTrace();
            }
            finally
            {
                close( oos );
                close( fos );
            }
        }
    }

    public Map<Class, SerializedObject> getInstance( String anId )
    {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try
        {
            File storage = getStorageFileForId( anId );
            fis = new FileInputStream( storage );
            bis = new BufferedInputStream( fis );
            ois = new ObjectInputStream( bis );
            // First instance in the ObjectInputStream is the ID.
            String idOnFile = (String) ois.readObject();
            if( !idOnFile.equals( anId ) )
            {
                throw new IllegalStateException( "The object retrieved for [" + anId + "] contained another ID: " + idOnFile );
            }
            return (Map<Class, SerializedObject>) ois.readObject();
        }
        catch( FileNotFoundException e )
        {
            throw new ObjectNotFoundException( anId );
        }
        catch( IOException e )
        {
            throw new CompositeInstantiationException( "Error reading I/O for object [" + anId + "].", e );
        }
        catch( ClassNotFoundException e )
        {
            throw new CompositeInstantiationException( "Class(es) for object [" + anId + "] can not be found.", e );
        }
        finally
        {
            close( ois );
            close( bis );
            close( fis );
        }
    }

    public void removeInstance( String anId )
    {
        File store = getStorageFileForId( anId );
        if( store.exists() )
        {
            store.delete();
            fileMap.remove( anId );
            storeIndexFile();
        }
    }

    public void close() throws IOException
    {
    }

    private File getStorageFileForId( String id )
    {
        String filename = fileMap.getProperty( id );
        if( filename == null )
        {
            do
            {
                filename = Integer.toString( Math.abs( random.nextInt() ) );
            }
            while( fileMap.get( filename ) != null );
            fileMap.put( id, filename );
            storeIndexFile();
        }
        return new File( storageDir, filename );
    }

    private synchronized void loadIndexFile()
    {
        InputStream stream = null;
        FileInputStream fis = null;
        fileMap = new Properties();
        try
        {
            File indexFile = new File( storageDir, INDEX_FILE );
            if( indexFile.exists() )
            {
                fis = new FileInputStream( indexFile );
                stream = new BufferedInputStream( fis );
                fileMap.load( stream );
            }
        }
        catch( IOException e )
        {
            // TODO: How to handle exceptions?
            e.printStackTrace();
        }
        finally
        {
            close( stream );
            close( fis );
        }
    }

    private synchronized void storeIndexFile()
    {
        OutputStream fos = null;
        OutputStream stream = null;
        try
        {
            File indexFile = new File( storageDir, INDEX_FILE );
            fos = new FileOutputStream( indexFile );
            stream = new BufferedOutputStream( fos );
            fileMap.store( stream, "DO NOT EDIT!!!!!" );
        }
        catch( IOException e )
        {
            // TODO: How to do exception handling?
            e.printStackTrace();
        }
        finally
        {
            close( stream );
            close( fos );

        }
    }

    private void close( InputStream stream )
    {
        try
        {
            if( stream != null )
            {
                stream.close();
            }
        }
        catch( IOException e )
        {
            // TODO:
            e.printStackTrace();
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
                // TODO:
                e.printStackTrace();
            }
        }
    }
}
