package org.qi4j.entity.memory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.composite.scope.Structure;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.serialization.SerializedState;

/**
 * In-memory implementation of SerializationStore
 */
public class MemorySerializationStoreMixin
    implements SerializationStore
{
    private @Structure Qi4jSPI spi;
    private Map<EntityId, SerializedObject<SerializedState>> store;

    public MemorySerializationStoreMixin()
    {
        store = new ConcurrentHashMap<EntityId, SerializedObject<SerializedState>>();
    }

    public SerializedState get( EntityId entityIdId, UnitOfWork unitOfWork ) throws IOException
    {
        SerializedObject<SerializedState> serializedState = store.get( entityIdId );

        if( serializedState == null )
        {
            return null;
        }

        try
        {
            return serializedState.getObject( unitOfWork.getCompositeBuilderFactory(), spi );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
    }

    public boolean contains( EntityId entityIdId ) throws IOException
    {
        return store.containsKey( entityIdId );
    }

    public StateCommitter prepare( Map<EntityId, SerializedState> newEntities, Map<EntityId, SerializedState> updatedEntities, final Iterable<EntityId> removedEntities )
    {
        final Map<EntityId, SerializedObject<SerializedState>> updatedState = new HashMap<EntityId, SerializedObject<SerializedState>>();


        for( Map.Entry<EntityId, SerializedState> entry : newEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue() );
            updatedState.put( entry.getKey(), serializedObject );
        }

        for( Map.Entry<EntityId, SerializedState> entry : updatedEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue() );
            updatedState.put( entry.getKey(), serializedObject );
        }

        return new StateCommitter()
        {
            public void commit()
            {
                synchronized( store )
                {
                    // Remove state
                    for( EntityId removedEntityId : removedEntities )
                    {
                        store.remove( removedEntityId );
                    }

                    // Update state
                    store.putAll( updatedState );
                }
            }

            public void cancel()
            {
                // Do nothing
            }
        };
    }
}
