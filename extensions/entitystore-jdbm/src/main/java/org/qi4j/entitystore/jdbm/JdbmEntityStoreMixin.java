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
package org.qi4j.entitystore.jdbm;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.Serializer;
import jdbm.btree.BTree;
import jdbm.helper.ByteArrayComparator;
import jdbm.helper.DefaultSerializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import jdbm.recman.CacheRecordManager;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.io.Files;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.library.locking.ReadLock;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;

/**
 * JDBM implementation of MapEntityStore.
 */
public class JdbmEntityStoreMixin
    implements JdbmEntityStoreActivation, MapEntityStore, BackupRestore
{
    @Optional
    @Service
    FileConfiguration fileConfiguration;

    @This
    private Configuration<JdbmConfiguration> config;

    @Uses
    private ServiceDescriptor descriptor;

    private RecordManager recordManager;
    private BTree index;
    private Serializer serializer;

    @This
    ReadWriteLock lock;

    @Override
    public void setUpJdbm()
        throws Exception
    {
        initialize();
    }

    @Override
    public void tearDownJdbm()
        throws Exception
    {
        recordManager.close();
    }

    @ReadLock
    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try
        {
            Long stateIndex = getStateIndex( entityReference.identity() );

            if( stateIndex == null )
            {
                throw new EntityNotFoundException( entityReference );
            }

            byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

            if( serializedState == null )
            {
                throw new EntityNotFoundException( entityReference );
            }

            return new StringReader( new String( serializedState, "UTF-8" ) );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @WriteLock
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

                            byte[] stateArray = toString().getBytes( "UTF-8" );
                            long stateIndex = recordManager.insert( stateArray, serializer );
                            String indexKey = ref.toString();
                            index.insert( indexKey.getBytes( "UTF-8" ), stateIndex, false );
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

                            Long stateIndex = getStateIndex( ref.toString() );
                            byte[] stateArray = toString().getBytes( "UTF-8" );
                            recordManager.update( stateIndex, stateArray, serializer );
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor descriptor )
                    throws EntityNotFoundException
                {
                    try
                    {
                        Long stateIndex = getStateIndex( ref.toString() );
                        recordManager.delete( stateIndex );
                        index.remove( ref.toString().getBytes( "UTF-8" ) );
                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException( e );
                    }
                }
            } );

            recordManager.commit();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            recordManager.rollback();
            if( e instanceof IOException )
            {
                throw (IOException) e;
            }
            else if( e instanceof EntityStoreException )
            {
                throw (EntityStoreException) e;
            }
            else
            {
                throw new IOException( e );
            }
        }
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
                lock.writeLock().lock();

                try
                {
                    output.receiveFrom( new Sender<Reader, IOException>()
                    {
                        @Override
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
                        {
                            final TupleBrowser browser = index.browse();
                            final Tuple tuple = new Tuple();

                            while( browser.getNext( tuple ) )
                            {
                                String id = new String( (byte[]) tuple.getKey(), "UTF-8" );

                                Long stateIndex = getStateIndex( id );

                                if( stateIndex == null )
                                {
                                    continue;
                                } // Skip this one

                                byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

                                receiver.receive( new StringReader( new String( serializedState, "UTF-8" ) ) );
                            }
                        }
                    } );
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        };
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
                lock.readLock().lock();

                try
                {
                    output.receiveFrom( new Sender<String, IOException>()
                    {
                        @Override
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super String, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
                        {
                            final TupleBrowser browser = index.browse();
                            final Tuple tuple = new Tuple();

                            while( browser.getNext( tuple ) )
                            {
                                String id = new String( (byte[]) tuple.getKey(), "UTF-8" );

                                Long stateIndex = getStateIndex( id );

                                if( stateIndex == null )
                                {
                                    continue;
                                } // Skip this one

                                byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

                                receiver.receive( new String( serializedState, "UTF-8" ) );
                            }
                        }
                    } );
                }
                finally
                {
                    lock.readLock().unlock();
                }
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
                File dbFile = new File( getDatabaseName() + ".db" );
                File lgFile = new File( getDatabaseName() + ".lg" );

                // Create temporary store
                File tempDatabase = Files.createTemporayFileOf( dbFile );

                final RecordManager recordManager = RecordManagerFactory.createRecordManager( tempDatabase.getAbsolutePath(), new Properties() );
                ByteArrayComparator comparator = new ByteArrayComparator();
                final BTree index = BTree.createInstance( recordManager, comparator, serializer, DefaultSerializer.INSTANCE, 16 );
                recordManager.setNamedObject( "index", index.getRecid() );
                recordManager.commit();

                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        int counter = 0;

                        @Override
                        public void receive( String item )
                            throws IOException
                        {
                            // Commit one batch
                            if( ( counter++ % 1000 ) == 0 )
                            {
                                recordManager.commit();
                            }

                            String id = item.substring( "{\"identity\":\"".length() );
                            id = id.substring( 0, id.indexOf( '"' ) );

                            // Insert
                            byte[] stateArray = item.getBytes( "UTF-8" );
                            long stateIndex = recordManager.insert( stateArray, serializer );
                            index.insert( id.getBytes( "UTF-8" ), stateIndex, false );
                        }
                    } );
                }
                catch( IOException e )
                {
                    recordManager.close();
                    tempDatabase.delete();
                    throw e;
                }
                catch( Throwable senderThrowableType )
                {
                    recordManager.close();
                    tempDatabase.delete();
                    throw (SenderThrowableType) senderThrowableType;
                }

                // Import went ok - continue
                recordManager.commit();
                // close file handles otherwise Microsoft Windows will fail to rename database files.
                recordManager.close();

                lock.writeLock().lock();
                try
                {
                    // Replace old database with new
                    JdbmEntityStoreMixin.this.recordManager.close();

                    boolean deletedOldDatabase = true;
                    deletedOldDatabase &= dbFile.delete();
                    deletedOldDatabase &= lgFile.delete();
                    if( !deletedOldDatabase )
                    {
                        throw new IOException( "Could not remove old database" );
                    }

                    boolean renamedTempDatabase = true;
                    renamedTempDatabase &= new File( tempDatabase.getAbsolutePath() + ".db" ).renameTo( dbFile );
                    renamedTempDatabase &= new File( tempDatabase.getAbsolutePath() + ".lg" ).renameTo( lgFile );

                    if( !renamedTempDatabase )
                    {
                        throw new IOException( "Could not replace database with temp database" );
                    }

                    // Start up again
                    initialize();
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        };
    }

    private String getDatabaseName()
    {
        String pathname = config.get().file().get();
        if( pathname == null )
        {
            if( fileConfiguration != null )
            {
                File dataDir = fileConfiguration.dataDirectory();
                File jdbmDir = new File( dataDir, descriptor.identity() + "/jdbm.data" );
                pathname = jdbmDir.getAbsolutePath();
            }
            else
            {
                pathname = System.getProperty( "user.dir" ) + "/qi4j/jdbm.data";
            }
        }
        File dataFile = new File( pathname );
        File directory = dataFile.getAbsoluteFile().getParentFile();
        directory.mkdirs();
        String name = dataFile.getAbsolutePath();
        return name;
    }

    private Properties getProperties()
    {
        JdbmConfiguration config = this.config.get();

        Properties properties = new Properties();

        properties.put( RecordManagerOptions.AUTO_COMMIT, config.autoCommit().get().toString() );
        properties.put( RecordManagerOptions.DISABLE_TRANSACTIONS, config.disableTransactions().get().toString() );

        return properties;
    }

    private Long getStateIndex( String identity )
        throws IOException
    {
        return (Long) index.find( identity.getBytes( "UTF-8" ) );
    }

    private void initialize()
        throws IOException
    {
        String name = getDatabaseName();
        Properties properties = getProperties();

        recordManager = RecordManagerFactory.createRecordManager( name, properties );
        serializer = DefaultSerializer.INSTANCE;
        recordManager = new CacheRecordManager( recordManager, 1000, false );
        long recid = recordManager.getNamedObject( "index" );
        if( recid != 0 )
        {
            index = BTree.load( recordManager, recid );
        }
        else
        {
            ByteArrayComparator comparator = new ByteArrayComparator();
            index = BTree.createInstance( recordManager, comparator, serializer, DefaultSerializer.INSTANCE, 16 );
            recordManager.setNamedObject( "index", index.getRecid() );
        }
        recordManager.commit();
    }
}