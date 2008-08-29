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

package org.qi4j.entity.jgroups;

import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.injection.scope.This;
import org.qi4j.library.locking.ReadLock;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.service.Activatable;
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
import org.qi4j.spi.serialization.SerializedObject;

/**
 * JGroups implementation of EntityStore
 */
public class JGroupsSerializationEntityStoreMixin
    extends EntityTypeRegistryMixin
    implements Activatable
{
    private @This ReadWriteLock lock;

    private ReplicatedHashMap<String, SerializedObject<SerializableState>> replicatedMap;
    private JChannel channel;

    // Activatable implementation
    public void activate() throws Exception
    {
        channel = new JChannel();
        channel.connect( "entitystore" );
        replicatedMap = new ReplicatedHashMap<String, SerializedObject<SerializableState>>( channel, false );
        replicatedMap.setBlockingUpdates( true );
    }

    public void passivate() throws Exception
    {
        channel.close();
    }

    // EntityStore implementation
    @WriteLock
    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        if( replicatedMap.containsKey( identity.toString() ) )
        {
            throw new EntityAlreadyExistsException( "JGroups store", identity );
        }

        return new DefaultEntityState( identity, getEntityType( identity.type() ) );
    }

    @ReadLock
    public EntityState getEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );

        try
        {
            SerializedObject<SerializableState> serializableObject;
            serializableObject = replicatedMap.get( identity.toString() );

            if( serializableObject == null )
            {
                throw new EntityNotFoundException( "JGroups store", identity );
            }

            SerializableState serializableState = serializableObject.getObject( (CompositeBuilderFactory) null, null );

            return new DefaultEntityState( serializableState.version(),
                                           serializableState.lastModified(),
                                           identity,
                                           EntityStatus.LOADED,
                                           entityType,
                                           serializableState.properties(),
                                           serializableState.associations(),
                                           serializableState.manyAssociations() );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }


    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        lock.writeLock().lock();
        try
        {
            long lastModified = System.currentTimeMillis();
            for( EntityState entityState : newStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 entityState.version(),
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                replicatedMap.put( entityState.qualifiedIdentity().toString(), serializedObject );
            }

            for( EntityState entityState : loadedStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 entityState.version() + 1,
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                replicatedMap.put( entityState.qualifiedIdentity().toString(), serializedObject );
            }

            for( QualifiedIdentity removedState : removedStates )
            {
                replicatedMap.remove( removedState.toString() );
            }
        }
        catch( Exception e )
        {
            lock.writeLock().unlock();
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

    public Iterator<EntityState> iterator()
    {
        return null;
    }
}