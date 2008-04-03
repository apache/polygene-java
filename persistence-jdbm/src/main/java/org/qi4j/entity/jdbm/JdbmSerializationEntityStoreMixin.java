/*  Copyright 2008 Rickard …berg.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.ByteArraySerializer;
import jdbm.helper.Serializer;
import jdbm.htree.HTree;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.PersistenceException;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * JDBM implementation of SerializationStore
 */
public class JdbmSerializationEntityStoreMixin
    implements EntityStore, Activatable
{
    private @Structure Qi4jSPI spi;
    private @ThisCompositeAs ReadWriteLock lock;

    private RecordManager recordManager;
    private HTree index;
    private File dataFile;
    private Serializer serializer;

    // Activatable implementation
    public void activate() throws Exception
    {
        dataFile = new File( "qi4j.data" );
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
            throw new PersistenceException( "Unable to read properties from " + directory + "/qi4j.properties", e );
        }
        recordManager = RecordManagerFactory.createRecordManager( name, properties );
        serializer = new ByteArraySerializer();

        initializeIndex();
    }

    public void passivate() throws Exception
    {
        recordManager.close();
    }

    // EntityStore implementation
    @WriteLock
    public EntityState newEntityState( EntityId identity ) throws EntityStoreException
    {
        try
        {
            Long stateIndex = (Long) index.get( identity.getIdentity() );

            if( stateIndex != null )
            {
                throw new EntityAlreadyExistsException( "JDBM store", identity.getIdentity() );
            }
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }

        return new EntityStateInstance( 0, identity, EntityStatus.NEW, new HashMap<String, Object>(), new HashMap<String, EntityId>(), new HashMap<String, Collection<EntityId>>() );
    }

    public EntityState getEntityState( EntityId identity ) throws EntityStoreException
    {
        try
        {
            Long stateIndex = (Long) index.get( identity.getIdentity() );

            if( stateIndex == null )
            {
                throw new EntityNotFoundException( "JDBM Store", identity.getIdentity() );
            }

            byte[] serializedState = (byte[]) recordManager.fetch( stateIndex.longValue(), serializer );

            if( serializedState == null )
            {
                throw new EntityNotFoundException( "JDBM Store", identity.getIdentity() );
            }

            SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( serializedState );

            try
            {
                SerializableState serializableState = serializedObject.getObject( (CompositeBuilderFactory) null, spi );
                return new EntityStateInstance( serializableState.getEntityVersion(), identity, EntityStatus.LOADED, serializableState.getProperties(), serializableState.getAssociations(), serializableState.getManyAssociations() );
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

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<EntityId> removedStates, ModuleBinding moduleBinding ) throws EntityStoreException
    {
        lock.writeLock().lock();

        try
        {
            for( EntityState entityState : newStates )
            {
                EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
                SerializableState state = new SerializableState( entityState.getEntityVersion(), entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                long stateIndex = recordManager.insert( serializedObject.getData(), serializer );
                String indexKey = entityState.getIdentity().getIdentity();
                index.put( indexKey, stateIndex );
            }

            for( EntityState entityState : loadedStates )
            {
                EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
                SerializableState state = new SerializableState( entityState.getEntityVersion(), entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                String indexKey = entityState.getIdentity().getIdentity();
                Long stateIndex = (Long) index.get( indexKey );
                recordManager.update( stateIndex, serializedObject.getData(), serializer );
            }

            for( EntityId removedState : removedStates )
            {
                Long stateIndex = (Long) index.get( removedState.getIdentity() );
                recordManager.delete( stateIndex );
                index.remove( removedState.getIdentity() );
            }
        }
        catch( IOException e )
        {
            lock.writeLock().unlock();
            throw new EntityStoreException( e );
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


    private void initializeIndex()
        throws IOException
    {
        long recid = recordManager.getNamedObject( "index" );
        if( recid != 0 )
        {
            System.out.println( "Using existing index" );
            index = HTree.load( recordManager, recid );
        }
        else
        {
            System.out.println( "Creating new index" );
            index = HTree.createInstance( recordManager );
            recordManager.setNamedObject( "index", index.getRecid() );
        }
    }
}