/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateCommitter;

/**
 * JAVADOC
 */
public class EntityStoreChanges
{
    private Map<EntityReference, EntityStateChanges> newEntityStates;
    private Map<EntityReference, EntityStateChanges> updatedEntityStates;
    private List<EntityReference> removedEntityStates;

    public StateCommitter applyTo( EntityStore store )
    {
        // New state
        List<EntityState> newStates = new ArrayList<EntityState>();
        for( Map.Entry<EntityReference, EntityStateChanges> entityReferenceEntityStateChangesEntry : newEntityStates.entrySet() )
        {
            EntityState state = store.getEntityState( entityReferenceEntityStateChangesEntry.getKey() );
            entityReferenceEntityStateChangesEntry.getValue().applyTo( state );
            newStates.add( state );
        }

        // Updated states
        List<EntityState> updatedStates = new ArrayList<EntityState>();
        for( Map.Entry<EntityReference, EntityStateChanges> entityReferenceEntityStateChangesEntry : updatedEntityStates.entrySet() )
        {
            EntityState state = store.getEntityState( entityReferenceEntityStateChangesEntry.getKey() );
            entityReferenceEntityStateChangesEntry.getValue().applyTo( state );
            updatedStates.add( state );
        }

        return store.prepare( newStates, updatedStates, removedEntityStates );
    }
}
