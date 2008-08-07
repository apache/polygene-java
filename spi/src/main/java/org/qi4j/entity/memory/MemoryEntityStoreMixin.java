package org.qi4j.entity.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.spi.entity.AbstractEntityStoreMixin;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.SerializedObject;

/**
 * In-memory implementation of EntityStore
 */
public class MemoryEntityStoreMixin
    extends AbstractEntityStoreMixin
{
    private final Map<String, Map<QualifiedIdentity, SerializedObject<SerializableState>>> store;

    public MemoryEntityStoreMixin()
    {
        store = new HashMap<String, Map<QualifiedIdentity, SerializedObject<SerializableState>>>();
    }

    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        Map<QualifiedIdentity, SerializedObject<SerializableState>> typeStore;
        synchronized( store )
        {
            typeStore = store.get( identity.type() );
        }

        if( typeStore != null )
        {
            synchronized( typeStore )
            {
                if( typeStore.containsKey( identity ) )
                {
                    throw new EntityAlreadyExistsException( "Serialization store", identity.identity() );
                }
            }
        }

        EntityType entityType = getEntityType( identity );

        return new DefaultEntityState( identity, entityType );
    }

    public EntityState getEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }

        try
        {
            Map<QualifiedIdentity, SerializedObject<SerializableState>> typeStore;
            synchronized( store )
            {
                typeStore = store.get( identity.type() );
                if( typeStore == null )
                {
                    throw new EntityNotFoundException( "Serialization store", identity.identity() );
                }
            }

            SerializedObject<SerializableState> serializableObject;
            synchronized( typeStore )
            {
                serializableObject = typeStore.get( identity );
            }

            if( serializableObject == null )
            {
                throw new EntityNotFoundException( "Serialization store", identity.identity() );
            }

            SerializableState serializableState = serializableObject.getObject( (CompositeBuilderFactory) null, null );

            return new DefaultEntityState( serializableState.version(), serializableState.lastModified(),
                                           identity, EntityStatus.LOADED, entityType, serializableState.properties(), serializableState.associations(), serializableState.manyAssociations() );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        final Map<QualifiedIdentity, SerializedObject<SerializableState>> updatedState = new HashMap<QualifiedIdentity, SerializedObject<SerializableState>>();

        long currentTime = System.currentTimeMillis();
        for( EntityState entityState : newStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
            SerializableState state = new SerializableState( entityState.qualifiedIdentity(), entityState.version(), currentTime, entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
            SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
            updatedState.put( entityState.qualifiedIdentity(), serializedObject );
        }

        for( EntityState entityState : loadedStates )
        {
            DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
            SerializableState state = new SerializableState( entityState.qualifiedIdentity(), entityState.version() + 1, currentTime, entityStateInstance.getProperties(), entityStateInstance.getAssociations(), entityStateInstance.getManyAssociations() );
            SerializedObject<SerializableState> serializedObject = new SerializedObject<SerializableState>( state );
            updatedState.put( entityState.qualifiedIdentity(), serializedObject );
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
                        Map<QualifiedIdentity, SerializedObject<SerializableState>> typeStore = store.get( removedEntityId.type() );
                        if( typeStore != null )
                        {
                            typeStore.remove( removedEntityId );
                        }
                    }

                    // Update state
                    for( Map.Entry<QualifiedIdentity, SerializedObject<SerializableState>> entityIdSerializedObjectEntry : updatedState.entrySet() )
                    {
                        Map<QualifiedIdentity, SerializedObject<SerializableState>> typeStore = store.get( entityIdSerializedObjectEntry.getKey().type() );
                        if( typeStore == null )
                        {
                            typeStore = new HashMap<QualifiedIdentity, SerializedObject<SerializableState>>();
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
        return null;
    }
}
