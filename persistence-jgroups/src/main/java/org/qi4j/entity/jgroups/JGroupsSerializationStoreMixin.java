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

package org.qi4j.entity.jgroups;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.library.framework.locking.WriteLock;
import org.qi4j.service.Activatable;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.serialization.SerializedState;

/**
 * JGroups implementation of SerializationStore
 */
public class JGroupsSerializationStoreMixin
    implements SerializationStore, Activatable
{
    private @Structure Qi4jSPI spi;
    private @ThisCompositeAs ReadWriteLock lock;

    private ReplicatedHashMap<String, SerializedObject<SerializedState>> replicatedMap;
    private JChannel channel;

    // Activatable implementation
    public void activate() throws Exception
    {
        channel = new JChannel();
        channel.connect( "entitystore" );
        replicatedMap = new ReplicatedHashMap<String, SerializedObject<SerializedState>>( channel, false );
        replicatedMap.setBlockingUpdates( true );
    }

    public void passivate() throws Exception
    {
        channel.close();
    }

    // SerializationStore implementation
    @WriteLock
    public SerializedState get( SerializedEntity entityId, UnitOfWork unitOfWork ) throws IOException
    {
        SerializedObject<SerializedState> serializedState = replicatedMap.get( entityId.toString() );

        if( serializedState == null )
        {
            return null;
        }

        try
        {
            return serializedState.getObject( unitOfWork, spi );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
    }

    @WriteLock
    public boolean contains( SerializedEntity entityId ) throws IOException
    {
        String indexKey = entityId.toString();
        return replicatedMap.containsKey( indexKey );
    }

    public StateCommitter prepare( Map<SerializedEntity, SerializedState> newEntities, Map<SerializedEntity, SerializedState> updatedEntities, Iterable<SerializedEntity> removedEntities )
        throws IOException
    {
        lock.writeLock().lock();

        // Add state
        for( Map.Entry<SerializedEntity, SerializedState> entry : newEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue(), spi );
            replicatedMap.putIfAbsent( entry.getKey().toString(), serializedObject );
        }

        // Update state
        for( Map.Entry<SerializedEntity, SerializedState> entry : updatedEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue(), spi );
            replicatedMap.replace( entry.getKey().toString(), serializedObject );
        }

        // Remove state
        for( SerializedEntity removedEntity : removedEntities )
        {
            String indexKey = removedEntity.toString();
            replicatedMap.remove( indexKey );
        }

        return new StateCommitter()
        {
            public void commit()
            {
                lock.writeLock().unlock();
            }

            public void cancel()
            {

                lock.writeLock().unlock();
            }
        };
    }

}