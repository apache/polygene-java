/*  Copyright 2008 Jan Kronquist.
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
package org.qi4j.entity.javaspaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.locking.WriteLock;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.spaces.SpaceException;
import org.qi4j.library.spaces.SpaceTransaction;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.serialization.SerializableState;

/**
 * Java Spaces implementation of EntityStore.
 */
public class JavaSpacesEntityStoreMixin extends EntityTypeRegistryMixin
    implements EntityStore
{
    @Uses private ServiceDescriptor descriptor;
    @Service private Space space;

    @WriteLock
    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );

        try
        {
            StorageEntry entry = (StorageEntry) space.readIfExists( identity.toString() );
            if( entry != null )
            {
                throw new EntityAlreadyExistsException( descriptor.identity(), identity );
            }
        }
        catch( SpaceException e )
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
                SerializableState serializableState = loadSerializableState( identity );
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

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException, ConcurrentEntityStateModificationException
    {
        final SpaceTransaction tx = space.newTransaction();
        long lastModified = System.currentTimeMillis();
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            storeNewStates( newStates, lastModified, bout );
            storeLoadedStates( loadedStates, lastModified, bout );
            removeStates( removedStates );
        }
        catch( Throwable e )
        {
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
                tx.commit();
            }

            public void cancel()
            {
                tx.abort();
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        return new Iterator<EntityState>()
        {
            public boolean hasNext()
            {
                return false;
            }

            public EntityState next()
            {
                return null;
            }

            public void remove()
            {
            }
        };
    }

    private SerializableState loadSerializableState( QualifiedIdentity identity )
        throws IOException, ClassNotFoundException
    {
        try
        {
            StorageEntry entry = (StorageEntry) space.readIfExists( identity.toString() );
            byte[] serializedState = entry.getData();
            if( serializedState == null )
            {
                throw new EntityNotFoundException( descriptor.identity(), identity );
            }
            ByteArrayInputStream bin = new ByteArrayInputStream( serializedState );
            ObjectInputStream oin = new ObjectInputStream( bin );
            return (SerializableState) oin.readObject();
        }
        catch( SpaceException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    private void removeStates( Iterable<QualifiedIdentity> removedStates )
        throws IOException
    {
        for( QualifiedIdentity removedState : removedStates )
        {
            try
            {
                space.takeIfExists( removedState.toString() );
            }
            catch( SpaceException e )
            {
                e.printStackTrace();
            }
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
                SerializableState state = new SerializableState( entityState.qualifiedIdentity(),
                                                                 newVersion,
                                                                 lastModified,
                                                                 entityStateInstance.getProperties(),
                                                                 entityStateInstance.getAssociations(),
                                                                 entityStateInstance.getManyAssociations() );
                ObjectOutputStream out = new ObjectOutputStream( bout );
                out.writeObject( state );
                out.close();

                QualifiedIdentity identity = entityState.qualifiedIdentity();
                byte[] stateArray = bout.toByteArray();
                try
                {
                    StorageEntry data = (StorageEntry) space.readIfExists( identity.toString() );
                    data.setData( stateArray );
                    space.write( identity.toString(), data );
                }
                catch( SpaceException e )
                {
                    e.printStackTrace();
                }
                bout.reset();
            }
        }
    }

    private void storeNewStates( Iterable<EntityState> newStates, long lastModified, ByteArrayOutputStream bout )
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
            ObjectOutputStream out = new ObjectOutputStream( bout );
            out.writeObject( state );
            out.close();
            byte[] stateArray = bout.toByteArray();
            QualifiedIdentity identity = entityState.qualifiedIdentity();
            StorageEntry data = new StorageEntry( identity );
            data.setData( stateArray );
            try
            {
                space.write( identity.toString(), data );
            }
            catch( SpaceException e )
            {
                throw new EntityStoreException( "Unable to write state to " );
            }
            bout.reset();
        }
    }

    private class StorageEntry
        implements Serializable
    {
        public QualifiedIdentity identity;
        private byte[] data;

        public StorageEntry( QualifiedIdentity identity )
        {
            this.identity = identity;
        }

        public byte[] getData()
        {
            return data;
        }

        public void setData( byte[] data )
        {
            this.data = data;
        }

    }
}
