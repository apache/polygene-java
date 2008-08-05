package org.qi4j.entity.memory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.structure.Module;

/**
 * In-memory implementation of EntityStore
 */
public class MemorySerializationEntityStoreMixin
    implements EntityStore
{
    private final Map<String, Map<QualifiedIdentity, SerializedObject<SerializableState>>> store;

    public MemorySerializationEntityStoreMixin()
    {
        store = new HashMap<String, Map<QualifiedIdentity, SerializedObject<SerializableState>>>();
    }

    public EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
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

        HashMap<String, Object> properties = new HashMap<String, Object>();
        HashMap<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
        for( AssociationDescriptor associationDescriptor : compositeDescriptor.state().associations() )
        {
            Method accessor = associationDescriptor.accessor();
            if( ListAssociation.class.isAssignableFrom( accessor.getReturnType() ) )
            {
                manyAssociations.put( associationDescriptor.qualifiedName(), new ArrayList<QualifiedIdentity>() );
            }
            else if( SetAssociation.class.isAssignableFrom( accessor.getReturnType() ) )
            {
                manyAssociations.put( associationDescriptor.qualifiedName(), new LinkedHashSet<QualifiedIdentity>() );
            }
            else if( ManyAssociation.class.isAssignableFrom( accessor.getReturnType() ) )
            {
                manyAssociations.put( associationDescriptor.qualifiedName(), new HashSet<QualifiedIdentity>() );
            }
        }
        return new DefaultEntityState( 0, System.currentTimeMillis(), identity, EntityStatus.NEW, properties, new HashMap<String, QualifiedIdentity>(), manyAssociations );
    }

    public EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity )
        throws EntityStoreException
    {
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
                                           identity, EntityStatus.LOADED, serializableState.properties(), serializableState.associations(), serializableState.manyAssociations() );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, final Iterable<QualifiedIdentity> removedStates, Module module ) throws EntityStoreException
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
