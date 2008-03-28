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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.PersistenceException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.serialization.SerializedState;

/**
 * JDBM implementation of SerializationStore
 */
public class JdbmSerializationStoreMixin
    implements SerializationStore, Activatable
{
    private @Structure Qi4jSPI spi;
    private @ThisCompositeAs ReadWriteLock lock;

    private RecordManager recordManager;
    private HTree index;
    private File dataFile;

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

        initializeIndex();
    }

    public void passivate() throws Exception
    {
        recordManager.close();
    }

    // SerializationStore implementation
    @WriteLock
    public SerializedState get( EntityId entityIdId, UnitOfWork unitOfWork ) throws IOException
    {
        String indexKey = entityIdId.toString();
        Long stateIndex = (Long) index.get( indexKey );

        if( stateIndex == null )
        {
            return null;
        }

        SerializedObject<SerializedState> serializedState = (SerializedObject<SerializedState>) recordManager.fetch( stateIndex.longValue() );

        if( serializedState == null )
        {
            return null;
        }

        try
        {
            return serializedState.getObject( unitOfWork.getCompositeBuilderFactory(), spi );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
    }

    @WriteLock
    public boolean contains( EntityId entityIdId ) throws IOException
    {
        String indexKey = entityIdId.toString();
        return index.get( indexKey ) != null;
    }

    public StateCommitter prepare( Map<EntityId, SerializedState> newEntities, Map<EntityId, SerializedState> updatedEntities, Iterable<EntityId> removedEntities )
        throws IOException
    {
        lock.writeLock().lock();

        try
        {
            // Add state
            for( Map.Entry<EntityId, SerializedState> entry : newEntities.entrySet() )
            {
                SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue() );
                long stateIndex = recordManager.insert( serializedObject );
                String indexKey = entry.getKey().toString();
                index.put( indexKey, stateIndex );
            }

            // Update state
            for( Map.Entry<EntityId, SerializedState> entry : updatedEntities.entrySet() )
            {
                SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue() );
                String indexKey = entry.getKey().toString();
                Long stateIndex = (Long) index.get( indexKey );
                recordManager.update( stateIndex, serializedObject );
            }

            // Remove state
            for( EntityId removedEntityId : removedEntities )
            {
                String indexKey = removedEntityId.toString();
                Long stateIndex = (Long) index.get( indexKey );
                index.remove( indexKey );
                recordManager.delete( stateIndex );
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
        catch( IOException e )
        {
            lock.writeLock().unlock();
            throw e;
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