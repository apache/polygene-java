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
package org.apache.polygene.entitystore.jdbm;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.library.fileconfig.FileConfiguration;
import org.apache.polygene.library.locking.ReadLock;
import org.apache.polygene.library.locking.WriteLock;
import org.apache.polygene.spi.entitystore.BackupRestore;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;

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
    private Configuration<JdbmEntityStoreConfiguration> config;

    @Uses
    private ServiceDescriptor descriptor;

    private RecordManager recordManager;
    private BTree index;
    private Serializer serializer;
    private File tempDirectory;

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
                public Writer newEntity( EntityReference ref, EntityDescriptor descriptor )
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
                public Writer updateEntity( MapChange mapChange )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();

                            Long stateIndex = getStateIndex( mapChange.reference().identity() );
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
                        Long stateIndex = getStateIndex( ref.identity() );
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
    public Stream<Reader> entityStates()
    {
        return backup().map( StringReader::new );
    }

    @Override
    public Stream<String> backup()
    {
        lock.writeLock().lock();
        TupleBrowser browser;
        try
        {
            browser = index.browse();
        }
        catch( IOException ex )
        {
            lock.writeLock().unlock();
            throw new EntityStoreException( ex );
        }
        return StreamSupport.stream(
            new Spliterators.AbstractSpliterator<String>( Long.MAX_VALUE, Spliterator.ORDERED )
            {
                private final Tuple tuple = new Tuple();

                @Override
                public boolean tryAdvance( final Consumer<? super String> action )
                {
                    try
                    {
                        if( !browser.getNext( tuple ) )
                        {
                            return false;
                        }
                        Identity identity = new StringIdentity( (byte[]) tuple.getKey() );
                        Long stateIndex = getStateIndex( identity );
                        if( stateIndex == null )
                        {
                            return false;
                        }
                        byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );
                        String state = new String( serializedState, "UTF-8" );
                        action.accept( state );
                        return true;
                    }
                    catch( IOException ex )
                    {
                        lock.writeLock().unlock();
                        throw new EntityStoreException( ex );
                    }
                }
            },
            false
        ).onClose( () -> lock.writeLock().unlock() );
    }

    @Override
    public void restore( final Stream<String> states )
    {
        File dbFile = new File( getDatabaseName() + ".db" );
        File lgFile = new File( getDatabaseName() + ".lg" );

        // Create temporary store
        File tempDatabase = createTemporaryDatabase();
        final RecordManager recordManager;
        final BTree index;
        try
        {
            recordManager = RecordManagerFactory.createRecordManager( tempDatabase.getAbsolutePath(),
                                                                      new Properties() );
            ByteArrayComparator comparator = new ByteArrayComparator();
            index = BTree.createInstance( recordManager, comparator, serializer, DefaultSerializer.INSTANCE, 16 );
            recordManager.setNamedObject( "index", index.getRecid() );
            recordManager.commit();
        }
        catch( IOException ex )
        {
            throw new EntityStoreException( ex );
        }
        try
        {
            // TODO NO NEED TO SYNCHRONIZE HERE
            AtomicLong counter = new AtomicLong();
            Consumer<String> stateConsumer = state ->
            {
                try
                {
                    // Commit one batch
                    if( ( counter.incrementAndGet() % 1000 ) == 0 )
                    {
                        recordManager.commit();
                    }

                    String id = state.substring( "{\"reference\":\"".length() );
                    id = id.substring( 0, id.indexOf( '"' ) );

                    // Insert
                    byte[] stateArray = state.getBytes( "UTF-8" );
                    long stateIndex = recordManager.insert( stateArray, serializer );
                    index.insert( id.getBytes( "UTF-8" ), stateIndex, false );
                }
                catch( IOException ex )
                {
                    throw new UncheckedIOException( ex );
                }
            };
            states.forEach( stateConsumer );
            // Import went ok - continue
            recordManager.commit();
            // close file handles otherwise Microsoft Windows will fail to rename database files.
            recordManager.close();
        }
        catch( IOException | UncheckedIOException ex )
        {
            try
            {
                recordManager.close();
            }
            catch( IOException ignore ) { }
            tempDatabase.delete();
            throw new EntityStoreException( ex );
        }
        try
        {

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
                    throw new EntityStoreException( "Could not remove old database" );
                }

                boolean renamedTempDatabase = true;
                renamedTempDatabase &= new File( tempDatabase.getAbsolutePath() + ".db" ).renameTo( dbFile );
                renamedTempDatabase &= new File( tempDatabase.getAbsolutePath() + ".lg" ).renameTo( lgFile );

                if( !renamedTempDatabase )
                {
                    throw new EntityStoreException( "Could not replace database with temp database" );
                }

                // Start up again
                initialize();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
        catch( IOException ex )
        {
            tempDatabase.delete();
            throw new EntityStoreException( ex );
        }
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
                pathname = System.getProperty( "user.dir" ) + "/polygene/jdbm.data";
            }
        }
        File dataFile = new File( pathname );
        File directory = dataFile.getAbsoluteFile().getParentFile();
        directory.mkdirs();
        String name = dataFile.getAbsolutePath();
        return name;
    }

    private File createTemporaryDatabase()
    {
        try
        {
            File tempDatabase = Files.createTempFile( getTemporaryDirectory().toPath(),
                                                      descriptor.identity().toString(),
                                                      "write" ).toFile();
            tempDatabase.deleteOnExit();
            return tempDatabase;
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    private File getTemporaryDirectory() throws IOException
    {
        if( tempDirectory != null )
        {
            return tempDirectory;
        }
        String storeId = descriptor.identity().toString();
        tempDirectory = fileConfiguration != null
                        ? new File( fileConfiguration.temporaryDirectory(), storeId )
                        : new File( new File( System.getProperty( "java.io.tmpdir" ) ),
                                    storeId );
        if( !tempDirectory.exists() )
        {
            java.nio.file.Files.createDirectories( tempDirectory.toPath() );
        }
        return tempDirectory;
    }

    private Properties getProperties()
    {
        JdbmEntityStoreConfiguration config = this.config.get();

        Properties properties = new Properties();

        properties.put( RecordManagerOptions.AUTO_COMMIT, config.autoCommit().get().toString() );
        properties.put( RecordManagerOptions.DISABLE_TRANSACTIONS, config.disableTransactions().get().toString() );

        return properties;
    }

    private Long getStateIndex( Identity identity )
        throws IOException
    {
        return (Long) index.find( identity.toBytes() );
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