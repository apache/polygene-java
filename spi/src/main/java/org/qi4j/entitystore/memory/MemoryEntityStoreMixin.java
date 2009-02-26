package org.qi4j.entitystore.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.serialization.FastObjectInputStream;
import org.qi4j.spi.serialization.FastObjectOutputStream;
import org.qi4j.spi.serialization.SerializableState;

/**
 * In-memory implementation of EntityStore.
 */
public class MemoryEntityStoreMixin extends EntityTypeRegistryMixin
{
    private final Map<String, Map<QualifiedIdentity, byte[]>> store;
    private @Uses ServiceDescriptor descriptor;

    public MemoryEntityStoreMixin()
    {
        store = new HashMap<String, Map<QualifiedIdentity, byte[]>>();
    }

    public EntityState newEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        Map<QualifiedIdentity, byte[]> typeStore;
        synchronized( store )
        {
            typeStore = store.get( identity.type() );
        }

        if( typeStore != null )
        {
            // This synchronization is on the Store for the particular type and is semantically correct.
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized( typeStore )
            {
                if( typeStore.containsKey( identity ) )
                {
                    throw new EntityAlreadyExistsException( descriptor.identity(), identity );
                }
            }
        }

        EntityType entityType = getEntityType( identity.type() );

        return new DefaultEntityState( identity, entityType );
    }

    public EntityState getEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }

        try
        {
            Map<QualifiedIdentity, byte[]> typeStore;
            synchronized( store )
            {
                typeStore = store.get( identity.type() );
                if( typeStore == null )
                {
                    throw new EntityNotFoundException( "Serialization store", identity );
                }
            }

            byte[] stateArray;
            // This synchronization is on the Store for the particular type and is semantically correct.
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized( typeStore )
            {
                stateArray = typeStore.get( identity );
            }

            if( stateArray == null )
            {
                throw new EntityNotFoundException( "Serialization store", identity );
            }

            SerializableState serializableState = loadSerializableState( typeStore, identity );
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
            throw new EntityStoreException( "Could not get serialized state", e );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates,
                                   Iterable<EntityState> loadedStates,
                                   final Iterable<QualifiedIdentity> removedStates )
        throws EntityStoreException
    {
        final Map<QualifiedIdentity, byte[]> updatedState = new HashMap<QualifiedIdentity, byte[]>();

        long currentTime = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            for( EntityState entityState : newStates )
            {
                byte[] stateArray = serializeState( entityState, currentTime, baos );
                updatedState.put( entityState.qualifiedIdentity(), stateArray );
            }

            for( EntityState entityState : loadedStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                if( entityStateInstance.isModified() )
                {
                    byte[] stateArray = serializeState( entityState, currentTime, baos );
                    updatedState.put( entityState.qualifiedIdentity(), stateArray );
                }
            }
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }

        return new StateCommitter()
        {
            public void commit()
            {
                synchronized( store )
                {
                    // Remove state
                    for( QualifiedIdentity removedEntityId : removedStates )
                    {
                        Map<QualifiedIdentity, byte[]> typeStore = store.get( removedEntityId.type() );
                        if( typeStore != null )
                        {
                            typeStore.remove( removedEntityId );
                        }
                    }

                    // Update state
                    for( Map.Entry<QualifiedIdentity, byte[]> entityIdSerializedObjectEntry : updatedState.entrySet() )
                    {
                        Map<QualifiedIdentity, byte[]> typeStore = store.get( entityIdSerializedObjectEntry.getKey().type() );
                        if( typeStore == null )
                        {
                            typeStore = new HashMap<QualifiedIdentity, byte[]>();
                            store.put( entityIdSerializedObjectEntry.getKey().type(), typeStore );
                        }
                        typeStore.put( entityIdSerializedObjectEntry.getKey(), entityIdSerializedObjectEntry.getValue() );
                    }
                }
            }

            public void cancel()
            {
                // Do nothing
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        final Iterator<Map<QualifiedIdentity, byte[]>> typeIterator = store.values().iterator();

        return new Iterator<EntityState>()
        {
            Iterator<QualifiedIdentity> qidIterator = null;

            public boolean hasNext()
            {
                if( qidIterator == null )
                {
                    if( typeIterator.hasNext() )
                    {
                        qidIterator = typeIterator.next().keySet().iterator();
                    }
                    else
                    {
                        return false;
                    }
                }

                return qidIterator.hasNext();
            }

            public EntityState next()
            {
                return getEntityState( qidIterator.next() );
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private SerializableState loadSerializableState( Map<QualifiedIdentity, byte[]> typeStore, QualifiedIdentity identity )
        throws IOException, ClassNotFoundException
    {
        byte[] serializedState = typeStore.get( identity );

        if( serializedState == null )
        {
            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream( serializedState );
        ObjectInputStream oin = new FastObjectInputStream( bin, false );
        return (SerializableState) oin.readObject();
    }

    private byte[] serializeState( EntityState entityState,
                                   long lastModified,
                                   ByteArrayOutputStream bout )
        throws IOException
    {
        DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;

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
        return stateArray;
    }


}
