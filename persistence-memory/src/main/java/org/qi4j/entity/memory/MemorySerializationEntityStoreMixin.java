package org.qi4j.entity.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
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
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * In-memory implementation of EntityStore
 */
public class MemorySerializationEntityStoreMixin
    implements EntityStore
{
    private final Map<String, Map<EntityId, SerializedObject<SerializableState>>> store;

    public MemorySerializationEntityStoreMixin()
    {
        store = new HashMap<String, Map<EntityId, SerializedObject<SerializableState>>>();
    }

    public EntityState newEntityState( CompositeDescriptor compositeDescriptor, EntityId identity ) throws EntityStoreException
    {
        Map<EntityId, SerializedObject<SerializableState>> typeStore;
        synchronized( store )
        {
            typeStore = store.get( identity.getCompositeType() );
        }

        if( typeStore != null )
        {
            synchronized( typeStore )
            {
                if( typeStore.containsKey( identity ) )
                {
                    throw new EntityAlreadyExistsException( "Serialization store", identity.getIdentity() );
                }
            }
        }

        return new EntityStateInstance( 0, identity, EntityStatus.NEW, new HashMap<String, Object>(), new HashMap<String, EntityId>(), new HashMap<String, Collection<EntityId>>() );
    }

    public EntityState getEntityState( CompositeDescriptor compositeDescriptor, EntityId identity ) throws EntityStoreException
    {
        try
        {
            Map<EntityId, SerializedObject<SerializableState>> typeStore;
            synchronized( store )
            {
                typeStore = store.get( identity.getCompositeType() );
                if( typeStore == null )
                {
                    throw new EntityNotFoundException( "Serialization store", identity.getIdentity() );
                }
            }

            SerializedObject<SerializableState> serializableObject;
            synchronized( typeStore )
            {
                serializableObject = typeStore.get( identity );
            }

            if( serializableObject == null )
            {
                throw new EntityNotFoundException( "Serialization store", identity.getIdentity() );
            }

            SerializableState serializableState = serializableObject.getObject( (CompositeBuilderFactory) null, null );

            return new EntityStateInstance( serializableState.getEntityVersion(), identity, EntityStatus.LOADED, serializableState.getProperties(), serializableState.getAssociations(), serializableState.getManyAssociations() );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<EntityId> removedStates, ModuleBinding moduleBinding ) throws EntityStoreException
    {
        final Map<EntityId, SerializedObject<SerializableState>> updatedState = new HashMap<EntityId, SerializedObject<SerializableState>>();


        for( EntityState entityState : newStates )
        {
            EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
            SerializableState state = new SerializableState( entityState.getEntityVersion(), entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
            SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
            updatedState.put( entityState.getIdentity(), serializedObject );
        }

        for( EntityState entityState : loadedStates )
        {
            EntityStateInstance entityStateInstance = (EntityStateInstance) entityState;
            SerializableState state = new SerializableState( entityState.getEntityVersion(), entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
            SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
            updatedState.put( entityState.getIdentity(), serializedObject );
        }

        return new StateCommitter()
        {
            public void commit()
            {
                synchronized( store )
                {
                    // Remove state
                    for( EntityId removedEntityId : removedStates )
                    {
                        Map<EntityId, SerializedObject<SerializableState>> typeStore = store.get( removedEntityId.getCompositeType() );
                        if( typeStore != null )
                        {
                            typeStore.remove( removedEntityId );
                        }
                    }

                    // Update state
                    for( Map.Entry<EntityId, SerializedObject<SerializableState>> entityIdSerializedObjectEntry : updatedState.entrySet() )
                    {
                        Map<EntityId, SerializedObject<SerializableState>> typeStore = store.get( entityIdSerializedObjectEntry.getKey().getCompositeType() );
                        if( typeStore == null )
                        {
                            typeStore = new HashMap<EntityId, SerializedObject<SerializableState>>();
                            store.put( entityIdSerializedObjectEntry.getKey().getCompositeType(), typeStore );
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
}
