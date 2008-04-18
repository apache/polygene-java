package org.qi4j.entity.rmi;

import java.io.IOException;
import java.rmi.Remote;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Interface for remote EntityStore
 */
public interface RemoteEntityStore
    extends Remote
{
    EntityState getEntityState( QualifiedIdentity identity )
        throws IOException;

    StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates )
        throws IOException;
}
