package org.qi4j.entity.memory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.composite.scope.Structure;
import org.qi4j.entity.EntitySession;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedObject;
import org.qi4j.spi.serialization.SerializedState;

/**
 * In-memory implementation of SerializationStore
 */
public class MemorySerializationStoreMixin
    implements SerializationStore
{
    private @Structure Qi4jSPI spi;
    private Map<SerializedEntity, SerializedObject<SerializedState>> store;

    public MemorySerializationStoreMixin()
    {
        store = new ConcurrentHashMap<SerializedEntity, SerializedObject<SerializedState>>();
    }

    public SerializedState get( SerializedEntity entityId, EntitySession session ) throws IOException
    {
        SerializedObject<SerializedState> serializedState = store.get( entityId );

        if( serializedState == null )
        {
            return null;
        }

        try
        {
            return serializedState.getObject( session, spi );
        }
        catch( ClassNotFoundException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
    }

    public boolean contains( SerializedEntity entityId ) throws IOException
    {
        return store.containsKey( entityId );
    }

    public StateCommitter prepare( Map<SerializedEntity, SerializedState> newEntities, Map<SerializedEntity, SerializedState> updatedEntities, final Iterable<SerializedEntity> removedEntities )
    {
        final Map<SerializedEntity, SerializedObject<SerializedState>> updatedState = new HashMap<SerializedEntity, SerializedObject<SerializedState>>();


        for( Map.Entry<SerializedEntity, SerializedState> entry : newEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue(), spi );
            updatedState.put( entry.getKey(), serializedObject );
        }

        for( Map.Entry<SerializedEntity, SerializedState> entry : updatedEntities.entrySet() )
        {
            SerializedObject<SerializedState> serializedObject = new SerializedObject<SerializedState>( entry.getValue(), spi );
            updatedState.put( entry.getKey(), serializedObject );
        }

        return new StateCommitter()
        {
            public void commit()
            {
                synchronized( store )
                {
                    // Remove state
                    for( SerializedEntity removedEntity : removedEntities )
                    {
                        store.remove( removedEntity );
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
