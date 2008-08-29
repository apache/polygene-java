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
package org.qi4j.entity.jdbm;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.nio.charset.Charset;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.ByteArrayComparator;
import jdbm.helper.ByteArraySerializer;
import jdbm.helper.LongSerializer;
import jdbm.helper.Serializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Uses;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializableState;

/**
 * JDBM implementation of SerializationStore
 */
public class JdbmEntityStoreMixin
    extends EntityTypeRegistryMixin
    implements Activatable
{
    private @This ReadWriteLock lock;
    private @This Configuration<JdbmConfiguration> config;
    private @Uses ServiceDescriptor descriptor;

    private RecordManager recordManager;
    private BTree index;
    private Serializer serializer;
    private long registryId;

    // Activatable implementation
    public void activate() throws Exception
    {

        File dataFile = new File( config.configuration().file().get() );
        System.out.println( "JDBM store:" + dataFile.getAbsolutePath() );
        File directory = dataFile.getParentFile();
        String name = dataFile.getAbsolutePath();
        Properties properties;
        try
        {
            properties = getProperties( directory );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Unable to read properties from " + directory + "/qi4j.properties", e );
        }
        recordManager = RecordManagerFactory.createRecordManager( name, properties );
        serializer = new ByteArraySerializer();

        initializeIndex();
        initializeRegistry();
    }

    public void passivate() throws Exception
    {
        saveRegistry();
        recordManager.close();
    }

    // EntityStore implementation
    @WriteLock
    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );

        try
        {
            Long stateIndex = getStateIndex( identity .identity());

            if( stateIndex != null )
            {
                throw new EntityAlreadyExistsException( descriptor.identity(), identity );
            }
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }

        return new DefaultEntityState( identity, entityType );
    }

    @WriteLock
    public EntityState getEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );

        try
        {

            try
            {
                SerializableState serializableState = loadSerializableState( identity.identity() );
                if (serializableState == null)
                    throw new EntityNotFoundException( descriptor.identity(), identity );

                DefaultEntityState state = new DefaultEntityState( serializableState.version(),
                                                                   serializableState.lastModified(),
                                                                   identity,
                                                                   EntityStatus.LOADED,
                                                                   entityType,
                                                                   serializableState.properties(),
                                                                   serializableState.associations(),
                                                                   serializableState.manyAssociations() );
                return state;
            }
            catch( ClassNotFoundException e )
            {
                throw new EntityStoreException( e );
            }
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        boolean turbo = config.configuration().turboMode().get();
        lock.writeLock().lock();

        long lastModified = System.currentTimeMillis();
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            storeNewStates( newStates, turbo, lastModified, bout );

            storeLoadedStates( loadedStates, turbo, lastModified, bout );

            removeStates( removedStates );
        }
        catch( Throwable e )
        {
            lock.writeLock().unlock();
            if( e instanceof EntityStoreException )
            {
                throw (EntityStoreException) e;
            }
            else
            {
                throw new EntityStoreException( e );
            }
        }

        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    recordManager.commit();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }

            public void cancel()
            {

                try
                {
                    recordManager.rollback();
                    initializeIndex(); // HTree indices are invalid after rollbacks according to the JDBM docs
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
        };
    }

    private void removeStates( Iterable<QualifiedIdentity> removedStates )
        throws IOException
    {
        for( QualifiedIdentity removedState : removedStates )
        {
            removeState( removedState.identity() );
        }
    }

    private void removeState( String removedState )
        throws IOException
    {
        Long stateIndex = getStateIndex( removedState );
        recordManager.delete( stateIndex );
        index.remove( removedState.getBytes("UTF-8") );
    }

    private void storeLoadedStates( Iterable<EntityState> loadedStates, boolean turbo, long lastModified, ByteArrayOutputStream bout )
        throws IOException
    {
        for( EntityState entityState : loadedStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;

            if( entityStateInstance.isModified() )
            {
                long newVersion = entityState.version() + 1;
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 newVersion,
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                ObjectOutputStream out = new FastObjectOutputStream( bout, turbo );
                out.writeObject( state );
                out.close();
                Long stateIndex = getStateIndex( entityState.qualifiedIdentity().identity() );
                byte[] stateArray = bout.toByteArray();
                bout.reset();
                recordManager.update( stateIndex, stateArray, serializer );
            }
        }
    }

    private void storeNewStates( Iterable<EntityState> newStates, boolean turbo, long lastModified, ByteArrayOutputStream bout )
        throws IOException
    {
        for( EntityState entityState : newStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
            SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                             entityState.version(),
                                                             lastModified,
                                                             entityStateInstance.getProperties(),
                                                             entityStateInstance.getAssociations(),
                                                             entityStateInstance.getManyAssociations() );
            ObjectOutputStream out = new FastObjectOutputStream( bout, turbo );
            out.writeObject( state );
            out.close();
            byte[] stateArray = bout.toByteArray();
            long stateIndex = recordManager.insert( stateArray, serializer );
            bout.reset();
            String indexKey = entityState.qualifiedIdentity().identity();
            index.insert( indexKey.getBytes( "UTF-8"), stateIndex, false );
        }
    }

    public Iterator<EntityState> iterator()
    {
        try
        {
            final TupleBrowser browser = index.browse();
            final Tuple tuple = new Tuple();

            return new Iterator<EntityState>()
            {
                public boolean hasNext()
                {
                    try
                    {
                        return browser.getNext( tuple );
                    }
                    catch( IOException e )
                    {
                        return false;
                    }
                }

                public EntityState next()
                {
                    try
                    {
                        String id = new String((byte[])tuple.getKey(), "UTF-8");
                        SerializableState serializableState = loadSerializableState( id );
                        if (serializableState == null)
                            throw new EntityNotFoundException( descriptor.identity(), new QualifiedIdentity( id, "") );

                        DefaultEntityState state = new DefaultEntityState( serializableState.version(),
                                                                           serializableState.lastModified(),
                                                                           serializableState.qualifiedIdentity(),
                                                                           EntityStatus.LOADED,
                                                                           getEntityType( serializableState.qualifiedIdentity().type() ),
                                                                           serializableState.properties(),
                                                                           serializableState.associations(),
                                                                           serializableState.manyAssociations() );
                        return state;
                    }
                    catch( Exception e )
                    {
                        throw new EntityStoreException( e );
                    }
                }

                public void remove()
                {
                }
            };
        }
        catch( IOException e )
        {
            return Collections.EMPTY_LIST.iterator();
        }
    }

    private Properties getProperties( File directory )
        throws IOException
    {
        Properties properties = new Properties();
        File propertiesFile = new File( directory, "qi4j.properties" );
        if( propertiesFile.exists() )
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( propertiesFile );
                BufferedInputStream bis = new BufferedInputStream( fis );
                properties.load( bis );
            }
            finally
            {
                if( fis != null )
                {
                    fis.close();
                }
            }
        }
        return properties;
    }

    private Long getStateIndex( String identity )
        throws IOException
    {
        Long stateIndex = (Long) index.find( identity.getBytes("UTF-8") );
        return stateIndex;
    }

    private void initializeIndex()
        throws IOException
    {
        long recid = recordManager.getNamedObject( "index" );
        if( recid != 0 )
        {
            System.out.println( "Using existing index" );
            index = BTree.load( recordManager, recid );
        }
        else
        {
            System.out.println( "Creating new index" );
            index = BTree.createInstance( recordManager, new ByteArrayComparator(), new ByteArraySerializer(), new LongSerializer(), 16 );
            recordManager.setNamedObject( "index", index.getRecid() );
        }
    }

    private void initializeRegistry()
        throws IOException
    {
        registryId = recordManager.getNamedObject( "registry" );
        if( registryId != 0 )
        {
            System.out.println( "Using existing registry" );
            List<EntityType> registry = (List) recordManager.fetch( registryId );
            for( EntityType entityType : registry )
            {

                registerEntityType( entityType );
            }
        }
        else
        {
            System.out.println( "Creating new registry" );
            registryId = recordManager.insert( new ArrayList() );
            recordManager.setNamedObject( "registry", registryId );
        }
    }

    private void saveRegistry()
        throws IOException
    {
        List<EntityType> registry = new ArrayList(super.entityTypes.values());
        recordManager.update( registryId, registry );
        recordManager.commit();
    }

    private SerializableState loadSerializableState( String identity )
        throws IOException, ClassNotFoundException
    {
        Long stateIndex = getStateIndex( identity );

        if( stateIndex == null )
        {
            return null;
        }

        byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

        if( serializedState == null )
        {
            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream( serializedState );
        ObjectInputStream oin = new FastObjectInputStream( bin, config.configuration().turboMode().get() );
        return (SerializableState) oin.readObject();
    }
}