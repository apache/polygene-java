package org.qi4j.spi.entity.helpers;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.MapEntityStore;
import org.qi4j.spi.serialization.FastObjectInputStream;
import org.qi4j.spi.serialization.FastObjectOutputStream;
import org.qi4j.spi.serialization.SerializableState;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore. Implement
 * MapEntityStore and add as mixin to the service using this mixin. See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.
 */
public class MapEntityStoreMixin
    implements EntityStore
{
    private @This MapEntityStore mapEntityStore;

    public EntityState newEntityState( EntityReference identity )
        throws EntityStoreException
    {
       synchronized( mapEntityStore )
       {
           if( mapEntityStore.contains( identity ) )
           {
               throw new EntityAlreadyExistsException( identity );
           }
       }

        return new DefaultEntityState( identity );
    }

    public EntityState getEntityState( EntityReference identity )
        throws EntityStoreException
    {
        try
        {
            SerializableState serializableState = loadSerializableState( identity );

            return getEntityState(serializableState);
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
                                   final Iterable<EntityReference> removedStates )
        throws EntityStoreException
    {
        final Map<EntityReference, InputStream> newState = new HashMap<EntityReference, InputStream>();
        final Map<EntityReference, InputStream> updatedState = new HashMap<EntityReference, InputStream>();

        long currentTime = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            for( EntityState entityState : newStates )
            {
                byte[] stateArray = serializeState( entityState, currentTime, baos );
                newState.put( entityState.identity(), new ByteArrayInputStream(stateArray) );
            }

            for( EntityState entityState : loadedStates )
            {
                DefaultEntityState entityStateInstance = (DefaultEntityState) entityState;
                if( entityStateInstance.isModified() )
                {
                    byte[] stateArray = serializeState( entityState, currentTime, baos );
                    updatedState.put( entityState.identity(), new ByteArrayInputStream(stateArray) );
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
                synchronized( mapEntityStore )
                {
                    mapEntityStore.update(newState, updatedState, removedStates);
                }
            }

            public void cancel()
            {
                // Do nothing
            }
        };
    }

    public void visitEntityStates(final EntityStateVisitor visitor)
    {
        mapEntityStore.visitMap(new MapEntityStore.MapEntityStoreVisitor()
        {
            public void visitEntity(InputStream entityState)
            {
                try
                {
                    ObjectInputStream oin = new FastObjectInputStream( entityState, false );
                    SerializableState state = (SerializableState) oin.readObject();

                    EntityState entity = getEntityState(state);
                    visitor.visitEntityState(entity);
                } catch (Exception e)
                {
                    Logger.getLogger(getClass().getName()).throwing(getClass().getName(), "visitEntityStates", e);
                }
            }
        });
    }

    private EntityState getEntityState(SerializableState serializableState)
    {
        EntityReference identity = serializableState.identity();
        Set<EntityTypeReference> entityTypeReferences = serializableState.entityTypeReferences();

        return new DefaultEntityState( serializableState.version(),
                                                           serializableState.lastModified(),
                                                           identity,
                                                           EntityStatus.LOADED,
                                                           entityTypeReferences,
                                                           serializableState.properties(),
                                                           serializableState.associations(),
                                                           serializableState.manyAssociations() );
    }

    private SerializableState loadSerializableState( EntityReference identity )
        throws IOException, ClassNotFoundException, EntityNotFoundException
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mapEntityStore.get( identity , bout);
        byte[] serializedState = bout.toByteArray();

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
        final EntityReference identity = entityState.identity();
        SerializableState state = new SerializableState( identity,
                                                         newVersion,
                                                         lastModified,
                                                         entityState.entityTypeReferences(),
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
