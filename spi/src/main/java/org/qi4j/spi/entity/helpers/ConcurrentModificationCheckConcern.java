/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Concern that helps EntityStores do concurrent modification checks.
 *
 * It caches the versions of state that it loads, and forgets them when
 * the state is committed. For normal operation this means that it does
 * not have to go down to the underlying store to get the current version.
 * Whenever there is a concurrent modification the store will most likely
 * have to check with the underlying store what the current version is.
 *
 */
public abstract class ConcurrentModificationCheckConcern extends ConcernOf<EntityStore>
    implements EntityStore
{
    private final Map<EntityReference, Long> versions = new WeakHashMap<EntityReference, Long>();

    public EntityState getEntityState( EntityReference anIdentity ) throws EntityStoreException
    {
        EntityState entityState = next.getEntityState( anIdentity );
        rememberVersion( entityState.identity(), entityState.version() );
        return entityState;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates,
                                   Iterable<EntityReference> removedStates )
        throws EntityStoreException, ConcurrentEntityStateModificationException
    {
        // Check for concurrent modification
        checkForConcurrentModification( loadedStates );

        try
        {
            return next.prepare( newStates, loadedStates, removedStates );
        }
        finally
        {
            forgetVersions( loadedStates );
        }
    }

    private void forgetVersions( Iterable<EntityState> loadedStates )
    {
        synchronized( versions )
        {
            for( EntityState loadedState : loadedStates )
            {
                versions.remove( loadedState.identity() );
            }
        }
    }

    private void rememberVersion( EntityReference identity, long version )
    {
        synchronized( versions )
        {
            versions.put( identity, version );
        }
    }

    private void checkForConcurrentModification( Iterable<EntityState> loadedStates )
        throws ConcurrentEntityStateModificationException
    {
        Collection<EntityReference> concurrentModifications = null;
        for( EntityState loadedState : loadedStates )
        {
            if( hasBeenModified( loadedState.identity(), loadedState.version() ) )
            {
                if( concurrentModifications == null )
                {
                    concurrentModifications = new ArrayList<EntityReference>();
                }
                concurrentModifications.add( loadedState.identity() );
            }
        }

        if( concurrentModifications != null )
        {
            throw new ConcurrentEntityStateModificationException( concurrentModifications );
        }
    }

    private boolean hasBeenModified( EntityReference identity, long oldVersion )
    {
        // Try version cache first
        Long rememberedVersion;
        synchronized( versions )
        {
            rememberedVersion = versions.get( identity );
        }

        if( rememberedVersion != null )
        {
            return rememberedVersion != oldVersion;
        }

        // Miss! Load state and compare
        EntityState state = getEntityState( identity );
        return state.version() != oldVersion;
    }

}
