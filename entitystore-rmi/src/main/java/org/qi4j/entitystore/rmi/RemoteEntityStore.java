package org.qi4j.entitystore.rmi;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityState;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Interface for remote EntityStore
 */
public interface RemoteEntityStore
        extends Remote
{
    EntityState getEntityState(EntityReference reference)
            throws IOException;

    void prepare(Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityReference> removedStates)
            throws IOException, ConcurrentEntityStateModificationException;
}
