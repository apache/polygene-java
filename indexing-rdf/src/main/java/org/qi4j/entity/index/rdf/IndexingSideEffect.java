/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.index.rdf;

import java.io.IOException;
import java.util.Map;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.SerializationStore;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public abstract class IndexingSideEffect
    implements SerializationStore
{

    @SideEffectFor SerializationStore serializationStore;
    @Service Indexer indexer;

    public StateCommitter prepare( Map<SerializedEntity, SerializedState> newEntities,
                                   Map<SerializedEntity, SerializedState> updatedEntities,
                                   Iterable<SerializedEntity> removedEntities ) throws IOException
    {
        indexer.index( newEntities, updatedEntities, removedEntities );
        indexer.toRDF( System.out );
        return null;
    }
}
