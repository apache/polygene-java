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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class DefaultEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private List<UnitOfWorkEvent> events = new ArrayList<UnitOfWorkEvent>();

    private EntityStoreEvents entityStore;
    private String identity;
    private Usecase usecaseMetaInfo;
    private MetaInfo unitOfWorkMetaInfo;

    public DefaultEntityStoreUnitOfWork( EntityStoreEvents entityStore, String identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        this.entityStore = entityStore;
        this.identity = identity;
        this.usecaseMetaInfo = usecaseMetaInfo;
        this.unitOfWorkMetaInfo = unitOfWorkMetaInfo;
    }

    public void addEvent( UnitOfWorkEvent event )
    {
        events.add( event );
    }

    public String identity()
    {
        return identity;
    }

    // EntityStore
    public EntityState newEntityState( EntityReference anIdentity ) throws EntityStoreException
    {
        addEvent( new NewEntityEvent( anIdentity ) );

        return entityStore.newEntityState( this, anIdentity, usecaseMetaInfo, unitOfWorkMetaInfo );
    }

    public EntityState getEntityState( EntityReference anIdentity ) throws EntityStoreException, EntityNotFoundException
    {
        EntityState entityState = entityStore.getEntityState( this, anIdentity, usecaseMetaInfo, unitOfWorkMetaInfo );
        addEvent( new GetEntityEvent( anIdentity, entityState.version() ) );
        return entityState;
    }

    public void removeEntityState( EntityReference anIdentity )
    {
        addEvent( new RemoveEntityEvent( anIdentity ) );
    }

    // EntityState
    public void addEntityType( EntityReference identity, EntityTypeReference entityType )
    {
        addEvent( new AddEntityTypeEvent( identity, entityType ) );
    }

    public void removeEntityType( EntityReference identity, EntityTypeReference entityType )
    {
        addEvent( new RemoveEntityTypeEvent( identity, entityType ) );
    }

    public void setProperty( EntityReference anIdentity, StateName stateName, String json )
    {
        addEvent( new SetPropertyEvent( anIdentity, stateName, json ) );
    }

    public void setAssociation( EntityReference anIdentity, StateName stateName, EntityReference associatedEntity )
    {
        addEvent( new SetAssociationEvent( anIdentity, stateName, associatedEntity ) );
    }

    // ManyAssociationState
    public void addManyAssociation( EntityReference anIdentity, StateName stateName, int index, EntityReference associatedEntity )
    {
        addEvent( new AddManyAssociationEvent( anIdentity, stateName, index, associatedEntity ) );
    }

    public void removeManyAssociation( EntityReference anIdentity, StateName stateName, EntityReference associatedEntity )
    {
        addEvent( new RemoveManyAssociationEvent( anIdentity, stateName, associatedEntity ) );
    }

    public List<UnitOfWorkEvent> events()
    {
        return events;
    }

    public void refresh( DefaultEntityState entityState )
    {
        DefaultEntityState refreshedEntityState = (DefaultEntityState) entityStore.getEntityState( this, entityState.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
        if( refreshedEntityState.version().equals( entityState.version() ) )
        {
            return; // No changes
        }

        // Copy new state
        refreshedEntityState.copyTo( entityState );

/*
        // Re-apply events for this EntityState
        int size = events.size();
        for( UnitOfWorkEvent event : events )
        {
            if (event instanceof EntityEvent)
            {
                EntityEvent entityEvent = (EntityEvent) event;
                event.applyTo( this );
            }
        }
        // Remove duplicate events
        while (events.size()> size)
        {
            events.remove( events.size()-1 );
        }
*/

        // Update get-events
        for( UnitOfWorkEvent event : events )
        {
            if( event instanceof GetEntityEvent )
            {
                GetEntityEvent getEntityEvent = (GetEntityEvent) event;
                if( getEntityEvent.identity().equals( entityState.identity() ) )
                {
                    getEntityEvent.refresh( refreshedEntityState.version() );
                }
            }
        }
    }
}
