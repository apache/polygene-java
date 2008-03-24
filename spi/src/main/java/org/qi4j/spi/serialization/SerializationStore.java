/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.spi.serialization;

import java.io.IOException;
import java.util.Map;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.spi.entity.StateCommitter;

/**
 * SerializedEntityStoreMixin backends should implement this interface. Then
 * create a ServiceComposite with it.
 */
public interface SerializationStore
{
    /**
     * Get the serialized state for the given entity.
     *
     * @param entityId
     * @param unitOfWork
     * @return the state for the entityId, or null if not found
     * @throws IOException
     */
    SerializedState get( SerializedEntity entityId, UnitOfWork unitOfWork )
        throws IOException;

    /**
     * Check whether a particular entity exists or not.
     *
     * @param entityId
     * @return true if the entity with the given entityId exists
     * @throws IOException
     */
    boolean contains( SerializedEntity entityId )
        throws IOException;

    /**
     * Prepare to store the given new, updated and remove state in the store.
     *
     * @param newEntities
     * @param updatedEntities
     * @param removedEntities
     * @return
     * @throws IOException
     */
    StateCommitter prepare( Map<SerializedEntity, SerializedState> newEntities, Map<SerializedEntity, SerializedState> updatedEntities, Iterable<SerializedEntity> removedEntities )
        throws IOException;
}
