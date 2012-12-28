/*
 * Copyright 2012, Paul Merlin.
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
package org.qi4j.entitystore.leveldb;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;

/**
 * LevelDB implementation of MapEntityStore.
 */
public class LevelDBEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{

    @Service
    private FileConfiguration fileConfig;
    @This
    private Configuration<LevelDBEntityStoreConfiguration> configuration;
    @Uses
    private ServiceDescriptor descriptor;
    private Charset charset;
    private DB db;

    @Override
    public void activateService()
        throws Exception
    {
        charset = Charset.forName( "UTF-8" );
        configuration.refresh();
        LevelDBEntityStoreConfiguration config = configuration.get();

        // Choose flavour
        String flavour = config.flavour().get();
        DBFactory factory;
        if( "jni".equalsIgnoreCase( flavour ) )
        {
            factory = newJniDBFactory();
        }
        else if( "java".equalsIgnoreCase( flavour ) )
        {
            factory = newJavaDBFactory();
        }
        else
        {
            factory = newDBFactory();
        }

        // Apply configuration
        Options options = new Options();
        options.createIfMissing( true );
        if( config.blockRestartInterval().get() != null )
        {
            options.blockRestartInterval( config.blockRestartInterval().get() );
        }
        if( config.blockSize().get() != null )
        {
            options.blockSize( config.blockSize().get() );
        }
        if( config.cacheSize().get() != null )
        {
            options.cacheSize( config.cacheSize().get() );
        }
        if( config.compression().get() != null )
        {
            options.compressionType( config.compression().get()
                                     ? CompressionType.SNAPPY
                                     : CompressionType.NONE );
        }
        if( config.maxOpenFiles().get() != null )
        {
            options.maxOpenFiles( config.maxOpenFiles().get() );
        }
        if( config.paranoidChecks().get() != null )
        {
            options.paranoidChecks( config.paranoidChecks().get() );
        }
        if( config.verifyChecksums().get() != null )
        {
            options.verifyChecksums( config.verifyChecksums().get() );
        }
        if( config.writeBufferSize().get() != null )
        {
            options.writeBufferSize( config.writeBufferSize().get() );
        }

        // Open/Create the database
        File dbFile = new File( fileConfig.dataDirectory(), descriptor.identity() );
        db = factory.open( dbFile, options );
    }

    /**
     * Tries in order: JNI and then pure Java LevelDB implementations.
     */
    private DBFactory newDBFactory()
    {
        try
        {
            return newJniDBFactory();
        }
        catch( Exception ex )
        {
            try
            {
                return newJavaDBFactory();
            }
            catch( Exception ex2 )
            {
                throw new RuntimeException( "Unable to create a LevelDB DBFactory instance. "
                                            + "Tried JNI and pure Java. "
                                            + "The stacktrace is the pure Java attempt.", ex2 );
            }
        }
    }

    private DBFactory newJniDBFactory()
        throws Exception
    {
        return (DBFactory) Class.forName( "org.fusesource.leveldbjni.JniDBFactory" ).newInstance();
    }

    private DBFactory newJavaDBFactory()
        throws Exception
    {
        return (DBFactory) Class.forName( "org.iq80.leveldb.impl.Iq80DBFactory" ).newInstance();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        try
        {
            db.close();
        }
        finally
        {
            db = null;
            charset = null;
        }
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        byte[] state = db.get( entityReference.identity().getBytes( charset ) );
        if( state == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        String jsonState = new String( state, charset );
        return new StringReader( jsonState );
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
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        DBIterator iterator = db.iterator();
                        try
                        {
                            for( iterator.seekToFirst(); iterator.hasNext(); iterator.next() )
                            {
                                byte[] state = iterator.peekNext().getValue();
                                String jsonState = new String( state, charset );
                                receiver.receive( new StringReader( jsonState ) );
                            }
                        }
                        finally
                        {
                            iterator.close();
                        }
                    }

                } );
            }

        };
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        final WriteBatch writeBatch = db.createWriteBatch();
        try
        {
            changes.visitMap( new MapChanger()
            {

                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String jsonState = toString();
                            writeBatch.put( ref.identity().getBytes( charset ), jsonState.getBytes( charset ) );
                        }

                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();
                            String jsonState = toString();
                            writeBatch.put( ref.identity().getBytes( charset ), jsonState.getBytes( charset ) );
                        }

                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
                {
                    writeBatch.delete( ref.identity().getBytes( charset ) );
                }

            } );
            db.write( writeBatch );
        }
        finally
        {
            writeBatch.close();
        }
    }

}
