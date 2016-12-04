/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.entitystore.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.library.fileconfig.FileConfiguration;
import org.apache.zest.spi.entitystore.BackupRestore;
import org.apache.zest.spi.entitystore.EntityAlreadyExistsException;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * FileEntityStore implementation of MapEntityStore.
 */
public class FileEntityStoreMixin
    implements FileEntityStoreActivation, MapEntityStore, BackupRestore
{
    @Optional
    @Service
    FileConfiguration fileConfiguration;

    @This
    private Configuration<FileEntityStoreConfiguration> config;

    private String storeId;
    private File dataDirectory;
    private File tempDirectory;
    private int slices;

    @Override
    public void initialize()
        throws Exception
    {
        config.refresh();
        storeId = config.get().identity().get().toString();
        String pathName = config.get().directory().get();
        if( pathName == null )
        {
            if( fileConfiguration != null )
            {
                pathName = new File( fileConfiguration.dataDirectory(), storeId ).getAbsolutePath();
            }
            else
            {
                pathName = System.getProperty( "user.dir" ) + "/zest/filestore/";
            }
        }
        dataDirectory = new File( pathName ).getAbsoluteFile();
        if( !dataDirectory.exists() )
        {
            Files.createDirectories( dataDirectory.toPath() );
        }
        tempDirectory = fileConfiguration != null
                        ? new File( fileConfiguration.temporaryDirectory(), storeId )
                        : new File( new File( System.getProperty( "java.io.tmpdir" ) ), storeId );
        if( !tempDirectory.exists() )
        {
            Files.createDirectories( tempDirectory.toPath() );
        }
        File slicesFile = new File( dataDirectory, "slices" );
        if( slicesFile.exists() )
        {
            slices = readIntegerInFile( slicesFile );
        }
        if( slices < 1 )
        {
            Integer slicesConf = config.get().slices().get();
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
        Files.write( file.toPath(), String.valueOf( value ).getBytes( UTF_8 ) );
    }

    private int readIntegerInFile( File file )
        throws IOException
    {
        return Integer.parseInt( new String( Files.readAllBytes( file.toPath() ), UTF_8 ) );
    }

    @Override
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

            String serializedState = fetch( f );
            return new StringReader( serializedState );
        }
        catch( FileNotFoundException e )
        {
            // Can't happen, but it does happen.
            throw new EntityNotFoundException( entityReference );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        try
        {
            changes.visitMap( new MapChanger()
            {
                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor descriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String state = this.toString();
                            File dataFile = getDataFile( ref );
                            if( dataFile.exists() )
                            {
                                throw new EntityAlreadyExistsException( ref );
                            }
                            store( dataFile, state );
                        }
                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor descriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String state = this.toString();
                            File dataFile = getDataFile( ref );
                            store( dataFile, state );
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor descriptor )
                    throws EntityNotFoundException
                {
                    File dataFile = getDataFile( ref );
                    if( !dataFile.exists() )
                    {
                        throw new EntityNotFoundException( ref );
                    }
                    //noinspection ResultOfMethodCallIgnored
                    dataFile.delete();
                }
            } );
        }
        catch( RuntimeException e )
        {
            if( e instanceof EntityStoreException )
            {
                throw e;
            }
            else
            {
                throw new IOException( e );
            }
        }
    }

    @Override
    public Input<String, IOException> backup()
    {
        return new Input<String, IOException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super String, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<String, IOException>()
                {
                    @Override
                    public <ThrowableType extends Throwable> void sendTo( Receiver<? super String, ThrowableType> receiver )
                        throws ThrowableType, IOException
                    {
                        for( File sliceDirectory : dataDirectory.listFiles() )
                        {
                            for( File file : sliceDirectory.listFiles() )
                            {
                                receiver.receive( fetch( file ) );
                            }
                        }
                    }
                } );
            }
        };
    }

    @Override
    public Output<String, IOException> restore()
    {
        return new Output<String, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                sender.sendTo( new Receiver<String, IOException>()
                {
                    @Override
                    public void receive( String item )
                        throws IOException
                    {
                        String id = item.substring( "{\"reference\":\"".length() );
                        id = id.substring( 0, id.indexOf( '"' ) );
                        store( getDataFile( id ), item );
                    }
                } );
            }
        };
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    @Override
                    public <ThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ThrowableType> receiver )
                        throws ThrowableType, IOException
                    {
                        for( File sliceDirectory : dataDirectory.listFiles() )
                        {
                            for( File file : sliceDirectory.listFiles() )
                            {
                                String state = fetch( file );
                                receiver.receive( new StringReader( state ) );
                            }
                        }
                    }
                } );
            }
        };
    }

    private File getDataFile( String identity )
    {
        identity = replaceInvalidChars( identity );
        String slice = "" + ( Math.abs( identity.hashCode() ) % slices );
        File sliceDirectory = new File( dataDirectory, slice );
        if( !sliceDirectory.exists() )
        {
            //noinspection ResultOfMethodCallIgnored
            sliceDirectory.mkdirs();
        }
        return new File( sliceDirectory, identity + ".json" );
    }

    /**
     * We need to replace all characters that some file system can't handle.
     * <p>
     * The resulting files should be portable across filesystems.
     * </p>
     *
     * @param identity The reference that needs a file to be stored in.
     *
     * @return A filesystem-safe name.
     */
    private String replaceInvalidChars( String identity )
    {
        StringBuilder b = new StringBuilder( identity.length() + 30 );
        for( int i = 0; i < identity.length(); i++ )
        {
            char ch = identity.charAt( i );
            if( ( ch >= 'a' && ch <= 'z' )
                || ( ch >= 'A' && ch <= 'Z' )
                || ( ch >= '0' && ch <= '9' )
                || ch == '_' || ch == '.' || ch == '-' )
            {
                b.append( ch );
            }
            else
            {
                int value = (int) ch;
                b.append( '~' );
                b.append( toHex( value ) );
            }
        }
        return b.toString();
    }

    private String toHex( int value )
    {
        String result = "000" + Integer.toHexString( value );
        return result.substring( result.length() - 4 );
    }

    private File getDataFile( EntityReference ref )
    {
        return getDataFile( ref.identity().toString() );
    }

    private String uncheckedFetch( File dataFile )
    {
        try
        {
            return fetch( dataFile );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private void uncheckedStore( File dataFile, String state )
    {
        try
        {
            store( dataFile, state );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private String fetch( File dataFile )
        throws IOException
    {
        return new String( Files.readAllBytes( dataFile.toPath() ), UTF_8 );
    }

    private void store( File dataFile, String state )
        throws IOException
    {
        // Write to temporary file first
        Path tempFile = Files.createTempFile( tempDirectory.toPath(), storeId, "write" );
        tempFile.toFile().deleteOnExit();
        Files.write( tempFile, state.getBytes( UTF_8 ) );

        // Replace old file
        Files.move( tempFile, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
    }
}