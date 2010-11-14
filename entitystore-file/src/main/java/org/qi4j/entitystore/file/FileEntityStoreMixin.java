/*  Copyright 2008 Rickard Öberg.
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
package org.qi4j.entitystore.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.service.ServiceDescriptor;

/**
 * JDBM implementation of SerializationStore
 */
public class FileEntityStoreMixin
    implements Activatable, MapEntityStore, BackupRestore
{
    @This
    private ReadWriteLock lock;

    @This
    private Configuration<FileEntityStoreConfiguration> config;

    @Uses
    private ServiceDescriptor descriptor;

    private File dataDirectory;
    private int slices;

    @SuppressWarnings( { "ResultOfMethodCallIgnored" } )
    public void activate()
        throws Exception
    {
        String pathName = config.configuration().directory().get();
        if( pathName == null )
        {
            pathName = System.getProperty( "user.dir" ) + "/qi4j/filestore/";
        }
        File rootDirectory = new File( pathName ).getAbsoluteFile();
        dataDirectory = new File( rootDirectory, "data" );
        if( !dataDirectory.exists() )
        {
            dataDirectory.mkdirs();
        }
        File slicesFile = new File( dataDirectory, "slices" );
        if( slicesFile.exists() )
        {
            slices = readIntegerInFile( slicesFile );
        }
        if( slices < 1 )
        {
            Integer slicesConf = config.configuration().slices().get();
            if( slicesConf == null )
            {
                slices = 10;
            }
            else
            {
                slices = slicesConf;
            }
            writeIntegerToFile( slicesFile, slices );
        }
    }

    private void writeIntegerToFile( File file, int value )
        throws IOException
    {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try
        {
            fw = new FileWriter( file );
            bw = new BufferedWriter( fw );
            bw.write( "" + value );
            bw.flush();
        }
        finally
        {
            if( bw != null )
            {
                bw.close();
            }
            if( fw != null )
            {
                fw.close();
            }
        }
    }

    private int readIntegerInFile( File file )
        throws IOException
    {
        FileReader fis = null;
        BufferedReader br = null;
        try
        {
            fis = new FileReader( file );
            br = new BufferedReader( fis );
            return Integer.parseInt( br.readLine() );
        }
        finally
        {
            if( br != null )
            {
                br.close();
            }
            if( fis != null )
            {
                fis.close();
            }
        }
    }

    public void passivate()
        throws Exception
    {
    }

    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try
        {
            File f = getDataFile( entityReference );

            if( !f.exists() )
            {
                throw new EntityNotFoundException( entityReference );
            }

            byte[] serializedState = fetch( f );
            return new StringReader( new String( serializedState, "UTF-8" ) );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private byte[] readDataFromStream( BufferedInputStream in, byte[] buf )
        throws IOException
    {
        int size = in.read( buf );
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 2000 );
        while( size > 0 )
        {
            baos.write( buf, 0, size );
            size = in.read( buf );
        }
        return baos.toByteArray();
    }

    public void applyChanges( MapChanges changes )
        throws IOException
    {
        try
        {
            changes.visitMap( new MapChanger()
            {
                public Writer newEntity( final EntityReference ref, EntityType entityType )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            byte[] stateArray = this.toString().getBytes( "UTF-8" );
                            File dataFile = getDataFile( ref );
                            store( dataFile, stateArray );
                        }
                    };
                }

                public Writer updateEntity( final EntityReference ref, EntityType entityType )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            byte[] stateArray = this.toString().getBytes( "UTF-8" );
                            File dataFile = getDataFile( ref );
                            store( dataFile, stateArray );
                        }
                    };
                }

                @SuppressWarnings( { "ResultOfMethodCallIgnored" } )
                public void removeEntity( EntityReference ref, EntityType entityType )
                    throws EntityNotFoundException
                {
                    File dataFile = getDataFile( ref );
                    if( !dataFile.exists() )
                    {
                        throw new EntityNotFoundException( ref );
                    }
                    dataFile.delete();
                }
            } );
        }
        catch( RuntimeException e )
        {
            if( e instanceof EntityStoreException )
            {
                throw (EntityStoreException) e;
            }
            else
            {
                IOException exception = new IOException();
                exception.initCause( e );
                throw exception;
            }
        }
    }

    public Input<String, IOException> backup()
    {
        return new Input<String, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<String, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<String, IOException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<String, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        for( File sliceDirectory : dataDirectory.listFiles() )
                        {
                            for( File file : sliceDirectory.listFiles() )
                            {
                                byte[] stateArray = fetch( file );
                                receiver.receive( new String( stateArray, "UTF-8" ) );
                            }
                        }
                    }
                });
            }
        };
    }

    public Output<String, IOException> restore()
    {
        return new Output<String, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                sender.sendTo( new Receiver<String, IOException>()
                {
                    public void receive( String item )
                        throws IOException
                    {
                        String id = item.substring( "{\"identity\":\"".length() );
                        id = id.substring( 0, id.indexOf( '"' ) );
                        byte[] stateArray = item.getBytes( "UTF-8" );
                        store( getDataFile( id ), stateArray );
                    }
                });
            }
        };
    }

    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        for( File sliceDirectory : dataDirectory.listFiles() )
                        {
                            for( File file : sliceDirectory.listFiles() )
                            {
                                byte[] serializedState = fetch( file );
                                receiver.receive( new StringReader( new String( serializedState, "UTF-8" ) ) );
                            }
                        }
                    }
                });
            }
        };
    }

    private File getDataFile( String identity )
    {
        String slice = "" + ( Math.abs( identity.hashCode() ) % slices );
        File sliceDirectory = new File( dataDirectory, slice );
        if( !sliceDirectory.exists() )
        {
            sliceDirectory.mkdirs();
        }
        return new File( sliceDirectory, identity + ".json" );
    }

    private File getDataFile( EntityReference ref )
    {
        return getDataFile( ref.identity() );
    }

    private byte[] fetch( File dataFile )
        throws IOException
    {
        byte[] buf = new byte[1000];
        BufferedInputStream in = null;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( dataFile );
            in = new BufferedInputStream( fis );
            return readDataFromStream( in, buf );
        }
        finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                }
                catch( IOException e )
                {
                    // Ignore ??
                }
            }
            if( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch( IOException e )
                {
                    // ignore??
                }
            }
        }
    }

    private void store( File dataFile, byte[] stateArray )
        throws IOException
    {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try
        {
            fos = new FileOutputStream( dataFile, false );
            bos = new BufferedOutputStream( fos );
            bos.write(stateArray);
        }
        finally
        {
            if( bos != null )
            {
                try
                {
                    bos.close();
                }
                catch( IOException e )
                {
                    // ignore??
                }
            }
            if( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch( IOException e )
                {
                    // ignore??
                }
            }
        }
    }
}