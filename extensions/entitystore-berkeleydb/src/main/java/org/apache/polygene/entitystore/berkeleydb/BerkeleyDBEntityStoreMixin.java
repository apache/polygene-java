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
package org.apache.polygene.entitystore.berkeleydb;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.library.fileconfig.FileConfiguration;
import org.apache.polygene.library.locking.ReadLock;
import org.apache.polygene.library.locking.WriteLock;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;

/**
 * BDB JE implementation of MapEntityStore.
 */
public class BerkeleyDBEntityStoreMixin
    implements BerkeleyDBEntityStoreActivation, MapEntityStore
{
    @Optional
    @Service
    private FileConfiguration fileConfiguration;

    @This
    private Configuration<BerkeleyDBEntityStoreConfiguration> config;

    @Uses
    private ServiceDescriptor descriptor;

    @Structure
    private Application application;

    private Database database;
    private Environment envHandle;

    @Override
    public void setUpBdbJe()
        throws Exception
    {
        initialize();
    }

    @Override
    public void tearDownBdbJe()
        throws Exception
    {
        closeDown();
    }

    @ReadLock
    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try
        {
            String indexKey = entityReference.toString();
            DatabaseEntry key = new DatabaseEntry( indexKey.getBytes( "UTF-8" ) );
            DatabaseEntry result = new DatabaseEntry();
            OperationStatus operationStatus = database.get( null, key, result, LockMode.DEFAULT );
            if( operationStatus == OperationStatus.NOTFOUND )
            {
                throw new EntityNotFoundException( entityReference );
            }
            return new StringReader( new String( result.getData(), "UTF-8" ) );
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
        Transaction transaction = envHandle.beginTransaction( null, TransactionConfig.DEFAULT );
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
                            String indexKey = ref.toString();
                            DatabaseEntry theKey = new DatabaseEntry( indexKey.getBytes( "UTF-8" ) );
                            DatabaseEntry theData = new DatabaseEntry( toString().getBytes( "UTF-8" ) );
                            database.put( transaction, theKey, theData );
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
                            String indexKey = mapChange.reference().identity().toString();
                            DatabaseEntry theKey = new DatabaseEntry( indexKey.getBytes( "UTF-8" ) );
                            DatabaseEntry theData = new DatabaseEntry( toString().getBytes( "UTF-8" ) );
                            database.put( transaction, theKey, theData );
                        }
                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor descriptor )
                    throws EntityNotFoundException
                {
                    try
                    {
                        String indexKey = ref.toString();
                        DatabaseEntry theKey = new DatabaseEntry( indexKey.getBytes( "UTF-8" ) );
                        database.delete( transaction, theKey );
                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException( e );
                    }
                }
            } );
            transaction.commit();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            transaction.abort();
            if( ( e instanceof IOException ) )
            {
                throw (IOException) e;
            }
            else if( !( e instanceof EntityStoreException ) )
            {
                throw new IOException( e );
            }
            else
            {
                throw (EntityStoreException) e;
            }
        }
    }

    @Override
    public Stream<Reader> entityStates()
        throws IOException
    {
        return StreamSupport.stream( new RecordIterable( database ).spliterator(), false );
    }

    private File getDataDirectory()
    {
        File dataDir;
        String pathname = config.get().dataDirectory().get();
        if( pathname != null )
        {
            dataDir = new File( pathname );
        }
        else
        {
            if( fileConfiguration != null )
            {
                dataDir = new File( fileConfiguration.dataDirectory(), application.name() + "/" + descriptor.identity() );
            }
            else
            {
                dataDir = new File( System.getProperty( "user.dir" ) );
            }
            dataDir = new File( dataDir, "data" );
        }
        //noinspection ResultOfMethodCallIgnored
        dataDir.mkdirs();
        return dataDir;
    }

    private void closeDown()
    {
        if( database != null )
        {
            database.close();
        }
        if( envHandle != null )
        {
            envHandle.close();
        }
    }

    private void initialize()
        throws IOException
    {
        File dataDirectory = getDataDirectory();
        EnvironmentConfig configuration = createConfiguration();

        envHandle = new Environment( dataDirectory, configuration );
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate( configuration.getAllowCreate() );
        dbConfig.setTransactional( configuration.getTransactional() );
        database = envHandle.openDatabase( null, config.get().databaseName().get(), dbConfig );
    }

    private EnvironmentConfig createConfiguration()
    {
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        BerkeleyDBEntityStoreConfiguration storeConfiguration = config.get();
        Boolean allowCreate = storeConfiguration.allowCreate().get();
        environmentConfig.setAllowCreate( allowCreate );
        environmentConfig.setLocking( storeConfiguration.locking().get() );
        environmentConfig.setLockTimeout( storeConfiguration.lockTimeout().get(), TimeUnit.MILLISECONDS );
        environmentConfig.setNodeName( storeConfiguration.nodeName().get() );
        environmentConfig.setReadOnly( storeConfiguration.readOnly().get() );
        environmentConfig.setSharedCache( storeConfiguration.sharedCache().get() );
        environmentConfig.setTransactional( storeConfiguration.transactional().get() );
        environmentConfig.setTxnTimeout( storeConfiguration.txnTimeout().get(), TimeUnit.MILLISECONDS );
        environmentConfig.setTxnSerializableIsolation( storeConfiguration.txnSerializableIsolation().get() );
        environmentConfig.setCacheMode( storeConfiguration.cacheMode().get() );
        environmentConfig.setCachePercent( storeConfiguration.cachePercent().get() );
        environmentConfig.setCacheSize( storeConfiguration.cacheSize().get() );
        environmentConfig.setOffHeapCacheSize( storeConfiguration.cacheHeapCacheSize().get() );
        environmentConfig.setDurability( Durability.parse( storeConfiguration.durability().get() ) );
        return environmentConfig;
    }

    private static class RecordIterable
        implements Iterable<Reader>, Iterator<Reader>
    {
        private Cursor cursor;
        private DatabaseEntry foundKey;
        private DatabaseEntry foundData;
        private boolean success;

        private RecordIterable( Database db )
            throws IOException
        {
            try
            {
                cursor = db.openCursor( null, null );
                foundKey = new DatabaseEntry();
                foundData = new DatabaseEntry();
            }
            catch( DatabaseException e )
            {
                throw new IOException( "Unknown problem in Berkeley DB", e );
            }
        }

        @Override
        @SuppressWarnings( "NullableProblems" )
        public Iterator<Reader> iterator()
        {
            forward();
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return success;
        }

        @Override
        public Reader next()
        {
            byte[] data = foundData.getData();
            forward();
            try
            {
                return new StringReader( new String( data, "UTF-8" ) );
            }
            catch( UnsupportedEncodingException e )
            {
                // can not happen.
                return new StringReader( "" );
            }
        }

        private void forward()
        {
            OperationStatus status = cursor.getNext( foundKey, foundData, LockMode.DEFAULT );
            if( status == OperationStatus.NOTFOUND )
            {
                // End of Cursor, and need to close.
                cursor.close();
            }
            success = status == OperationStatus.SUCCESS;
        }
    }
}