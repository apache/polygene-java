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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.GetEntityEvent;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

import java.util.*;

/**
 * Concern that helps EntityStores do concurrent modification checks.
 *
 * It caches the versions of state that it loads, and forgets them when
 * the state is committed. For normal operation this means that it does
 * not have to go down to the underlying store to get the current version.
 * Whenever there is a concurrent modification the store will most likely
 * have to check with the underlying store what the current version is.
 */
public abstract class ConcurrentModificationCheckConcern extends ConcernOf<EntityStore>
    implements EntityStore
{
    private static final Map<EntityReference, String> versions = new WeakHashMap<EntityReference, String>();

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, MetaInfo unitOfWorkMetaInfo )
    {
        final EntityStoreUnitOfWork uow = next.newUnitOfWork( usecase, unitOfWorkMetaInfo );

        return new ConcurrentCheckingEntityStoreUnitOfWork( uow );
    }

    public StateCommitter apply( String unitOfWorkIdentity, final Iterable<UnitOfWorkEvent> events, Usecase usecase, MetaInfo metaInfo ) throws EntityStoreException
    {
        checkForConcurrentModification( events );

        final StateCommitter stateCommitter = next.apply( unitOfWorkIdentity, events, usecase, metaInfo );
        return new StateCommitter()
        {
            public void commit()
            {
                stateCommitter.commit();
                forgetVersions( events );
            }

            public void cancel()
            {
                stateCommitter.cancel();
                forgetVersions( events );
            }
        };
    }

    private void forgetVersions( Iterable<UnitOfWorkEvent> events )
    {
        synchronized( versions )
        {
            for( UnitOfWorkEvent event : events )
            {
                if( event instanceof GetEntityEvent )
                {
                    GetEntityEvent getEntityEvent = (GetEntityEvent) event;
                    versions.remove( getEntityEvent.identity() );
                }
            }
        }
    }

    private void rememberVersion( EntityReference identity, String version )
    {
        synchronized( versions )
        {
            versions.put( identity, version );
        }
    }

    private void checkForConcurrentModification( Iterable<UnitOfWorkEvent> events )
        throws ConcurrentEntityStateModificationException
    {
        Collection<EntityReference> concurrentModifications = null;
        List<GetEntityEvent> getEvents = new ArrayList<GetEntityEvent>();
        for( UnitOfWorkEvent event : events )
        {
            if( event instanceof GetEntityEvent )
            {
                GetEntityEvent getEntityEvent = (GetEntityEvent) event;
                getEvents.add( getEntityEvent );
            }
        }

        for( GetEntityEvent getEntityEvent : getEvents )
        {
            if( hasBeenModified( getEntityEvent.identity(), getEntityEvent.version() ) )
            {
                if( concurrentModifications == null )
                {
                    concurrentModifications = new ArrayList<EntityReference>();
                }
                concurrentModifications.add( getEntityEvent.identity() );
            }
        }

        if( concurrentModifications != null )
        {
            throw new ConcurrentEntityStateModificationException( concurrentModifications );
        }
    }

    private boolean hasBeenModified( EntityReference identity, String oldVersion )
    {
        // Try version cache first
        String rememberedVersion;
        synchronized( versions )
        {
            rememberedVersion = versions.get( identity );
        }

        if( rememberedVersion != null )
        {
            return !rememberedVersion.equals( oldVersion );
        }

        // Miss! Load state and compare
        EntityStoreUnitOfWork uow = next.newUnitOfWork( UsecaseBuilder.newUsecase( "Check version" ), new MetaInfo() );
        EntityState state = uow.getEntityState( identity );
        return !state.version().equals( oldVersion );
    }

    private class ConcurrentCheckingEntityStoreUnitOfWork implements EntityStoreUnitOfWork
    {
        private final EntityStoreUnitOfWork uow;

        public ConcurrentCheckingEntityStoreUnitOfWork( EntityStoreUnitOfWork uow )
        {
            this.uow = uow;
        }

        public String identity()
        {
            return uow.identity();
        }

        public EntityState newEntityState( EntityReference anIdentity ) throws EntityStoreException
        {
            return uow.newEntityState( anIdentity );
        }

        public EntityState getEntityState( EntityReference anIdentity ) throws EntityStoreException, EntityNotFoundException
        {
            EntityState entityState = uow.getEntityState( anIdentity );
            rememberVersion( entityState.identity(), entityState.version() );
            return entityState;
        }

        public void addEvent( UnitOfWorkEvent event )
        {
            uow.addEvent( event );
        }

        public Iterable<UnitOfWorkEvent> events()
        {
            return uow.events();
        }
    }
}
