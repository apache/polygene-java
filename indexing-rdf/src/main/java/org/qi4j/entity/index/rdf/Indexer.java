package org.qi4j.entity.index.rdf;

import java.io.Writer;
import java.io.OutputStream;
import java.util.Map;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

public interface Indexer
{
    void index( Map<SerializedEntity, SerializedState> newEntities,
                Map<SerializedEntity, SerializedState> updatedEntities,
                Iterable<SerializedEntity> removedEntities );

    /**
     * Temporary debug only.
     */
    void toRDF( OutputStream outputStream );
}
