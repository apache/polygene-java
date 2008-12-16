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

package org.qi4j.entity.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;

/**
 * Concern that helps EntityStores do concurrent modification checks.
 *
 * It caches the versions of state that it loads, and forgets them when
 * the state is committed. For normal operation this means that it does
 * not have to go down to the underlying store to get the current version.
 * Whenever there is a concurrent modification the store will most likely
 * have to check with the underlying store what the current version is.
 *
 * This concern requires that the store has a Configuration entity, so that
 * it can access the service descriptor.
 */
public abstract class ConcurrentModificationCheckConcern
    extends ConcernOf<EntityStore>
    implements EntityStore
{
    private @This ServiceComposite service;

    private final Map<QualifiedIdentity, Long> versions = new WeakHashMap<QualifiedIdentity, Long>();

    public EntityState getEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        EntityState entityState = next.getEntityState( anIdentity );
        rememberVersion( entityState.qualifiedIdentity(), entityState.version() );
        return entityState;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException, ConcurrentEntityStateModificationException
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
                versions.remove( loadedState.qualifiedIdentity() );
            }
        }
    }

    private void rememberVersion( QualifiedIdentity identity, long version )
    {
        synchronized( versions )
        {
            versions.put( identity, version );
        }
    }

    private void checkForConcurrentModification( Iterable<EntityState> loadedStates )
        throws ConcurrentEntityStateModificationException
    {
        Collection<QualifiedIdentity> concurrentModifications = null;
        for( EntityState loadedState : loadedStates )
        {
            if( hasBeenModified( loadedState.qualifiedIdentity(), loadedState.version() ) )
            {
                if( concurrentModifications == null )
                {
                    concurrentModifications = new ArrayList<QualifiedIdentity>();
                }
                concurrentModifications.add( loadedState.qualifiedIdentity() );
            }
        }

        if( concurrentModifications != null )
        {
            throw new ConcurrentEntityStateModificationException( service.serviceDescriptor().identity(), concurrentModifications );
        }
    }

    private boolean hasBeenModified( QualifiedIdentity qualifiedIdentity, long oldVersion )
    {
        // Try version cache first
        Long rememberedVersion;
        synchronized( versions )
        {
            rememberedVersion = versions.get( qualifiedIdentity );
        }

        if( rememberedVersion != null )
        {
            return rememberedVersion != oldVersion;
        }

        // Miss! Load state and compare
        EntityState state = getEntityState( qualifiedIdentity );
        return state.version() != oldVersion;
    }

}
