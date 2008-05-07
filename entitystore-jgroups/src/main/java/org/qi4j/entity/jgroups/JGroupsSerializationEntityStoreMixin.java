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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.This;
import org.qi4j.library.framework.locking.ReadLock;
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
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.structure.Module;

/**
 * JGroups implementation of EntityStore
 */
public class JGroupsSerializationEntityStoreMixin
    implements EntityStore, Activatable
{
    private @Structure Qi4jSPI spi;
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
    public EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        if( replicatedMap.containsKey( identity.toString() ) )
        {
            throw new EntityAlreadyExistsException( "JGroups store", identity.getIdentity() );
        }

        return new EntityStateInstance( 0, identity, EntityStatus.NEW, new HashMap<String, Object>(), new HashMap<String, QualifiedIdentity>(), new HashMap<String, Collection<QualifiedIdentity>>() );
    }

    @ReadLock
    public EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        try
        {
            SerializedObject<SerializableState> serializableObject;
            serializableObject = replicatedMap.get( identity.toString() );

            if( serializableObject == null )
            {
                throw new EntityNotFoundException( "JGroups store", identity.getIdentity() );
            }

            SerializableState serializableState = serializableObject.getObject( (CompositeBuilderFactory) null, null );

            return new EntityStateInstance( serializableState.entityVersion(),
                                            identity,
                                            EntityStatus.LOADED,
                                            serializableState.properties(),
                                            serializableState.associations(),
                                            serializableState.manyAssociations() );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }


    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates, Module module ) throws EntityStoreException
    {
        lock.writeLock().lock();
        try
        {
            for( EntityState entityState : newStates )
            {
                EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
                SerializableState state = new SerializableState( entityState.getIdentity(),
                                                                 entityState.getEntityVersion(),
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                replicatedMap.put( entityState.getIdentity().toString(), serializedObject );
            }

            for( EntityState entityState : loadedStates )
            {
                EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
                SerializableState state = new SerializableState( entityState.getIdentity(),
                                                                 entityState.getEntityVersion() + 1,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
                replicatedMap.put( entityState.getIdentity().toString(), serializedObject );
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