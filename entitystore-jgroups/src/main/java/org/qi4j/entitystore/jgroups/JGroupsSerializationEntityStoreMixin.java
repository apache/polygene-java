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

package org.qi4j.entitystore.jgroups;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.library.locking.ReadLock;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.FastObjectOutputStream;
import org.qi4j.spi.serialization.FastObjectInputStream;

/**
 * JGroups implementation of EntityStore
 */
public class JGroupsSerializationEntityStoreMixin
    extends EntityTypeRegistryMixin
    implements Activatable
{
    private @This ReadWriteLock lock;

    private ReplicatedHashMap<String, byte[]> replicatedMap;
    private JChannel channel;
    private @Uses ServiceDescriptor descriptor;

    // Activatable implementation
    public void activate() throws Exception
    {
        channel = new JChannel();
        channel.connect( "entitystore" );
        replicatedMap = new ReplicatedHashMap<String, byte[]>( channel, false );
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
            try
            {
                SerializableState serializableState = loadSerializableState( identity );
                if( serializableState == null )
                {
                    throw new EntityNotFoundException( descriptor.identity(), identity );
                }

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
        lock.writeLock().lock();
        try
        {
            long lastModified = System.currentTimeMillis();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            storeNewStates( newStates, lastModified, bout );

            storeLoadedStates( loadedStates, lastModified, bout );

            removeStates( removedStates );
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

    private SerializableState loadSerializableState( QualifiedIdentity identity )
        throws IOException, ClassNotFoundException
    {
        byte[] serializedState = replicatedMap.get( identity.toString() );

        if( serializedState == null )
        {
            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream( serializedState );
        ObjectInputStream oin = new FastObjectInputStream( bin, false );
        return (SerializableState) oin.readObject();
    }

    private void storeNewStates( Iterable<EntityState> newStates, long lastModified, ByteArrayOutputStream bout )
        throws IOException
    {
        for( EntityState entityState : newStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
            final QualifiedIdentity identity = entityState.qualifiedIdentity();
            SerializableState state = new SerializableState( identity,
                                                             entityState.version(),
                                                             lastModified,
                                                             entityStateInstance.getProperties(),
                                                             entityStateInstance.getAssociations(),
                                                             entityStateInstance.getManyAssociations() );
            ObjectOutputStream out = new FastObjectOutputStream( bout, false );
            out.writeObject( state );
            out.close();
            byte[] stateArray = bout.toByteArray();
            bout.reset();
            replicatedMap.put( identity.toString(), stateArray );
        }
    }

    private void storeLoadedStates( Iterable<EntityState> loadedStates, long lastModified, ByteArrayOutputStream bout )
        throws IOException
    {
        for( EntityState entityState : loadedStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;

            if( entityStateInstance.isModified() )
            {
                long newVersion = entityState.version() + 1;
                final QualifiedIdentity identity = entityState.qualifiedIdentity();
                SerializableState state = new SerializableState( identity,
                                                                 newVersion,
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                ObjectOutputStream out = new FastObjectOutputStream( bout, false );
                out.writeObject( state );
                out.close();
                byte[] stateArray = bout.toByteArray();
                bout.reset();
                replicatedMap.put( identity.toString(), stateArray );
            }
        }
    }

    private void removeStates( Iterable<QualifiedIdentity> removedStates )
        throws IOException
    {
        for( QualifiedIdentity removedState : removedStates )
        {
            replicatedMap.remove( removedState.toString() );
        }
    }
}