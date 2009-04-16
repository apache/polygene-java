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

package org.qi4j.runtime.unitofwork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.StateName;

/**
 * JAVADOC
 */
public class UnitOfWorkStore
    implements EntityStore
{
    private UnitOfWorkInstance unitOfWork;

    public UnitOfWorkStore( UnitOfWorkInstance unitOfWork )
    {
        this.unitOfWork = unitOfWork;
    }

    public EntityState newEntityState( EntityReference identity ) throws EntityStoreException
    {
        UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( 0,
                                                                       System.currentTimeMillis(),
                                                                       identity,
                                                                       EntityStatus.NEW,
                                                                       new HashSet<EntityTypeReference>(),
                                                                       new HashMap<StateName, String>(),
                                                                       new HashMap<StateName, EntityReference>(),
                                                                       new HashMap<StateName, ManyAssociationState>(),
                                                                       null );
        return entityState;
    }

    public EntityState getEntityState( EntityReference identity ) throws EntityStoreException
    {
        EntityState parentState = unitOfWork.getCachedState( identity.toString() );
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.version(),
                                                                                 parentState.lastModified(),
                                                                                 identity,
                                                                                 EntityStatus.LOADED,
                                                                                 new HashSet<EntityTypeReference>(),
                                                                                 new HashMap<StateName, String>(),
                                                                                 new HashMap<StateName, EntityReference>(),
                                                                                 new HashMap<StateName, ManyAssociationState>(),
                                                                                 parentState );
        return unitOfWorkEntityState;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityReference> removedStates ) throws EntityStoreException
    {
        // Unused
        return null;
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }

    public EntityStateStore getEffectiveEntityStateStore( EntityReference identity, EntityType entityType )
    {
        EntityStateStore entityStateStore = unitOfWork.getEffectiveEntityStateStore( identity, entityType );
        if( entityStateStore == null )
        {
            return null;
        }

        EntityState parentState = entityStateStore.state;
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.version(),
                                                                                 parentState.lastModified(),
                                                                                 identity,
                                                                                 EntityStatus.LOADED,
                                                                                 new HashSet<EntityTypeReference>(),
                                                                                 new HashMap<StateName, String>(),
                                                                                 new HashMap<StateName, EntityReference>(),
                                                                                 new HashMap<StateName, ManyAssociationState>(),
                                                                                 parentState );
        entityStateStore = new EntityStateStore();
        entityStateStore.state = unitOfWorkEntityState;
        entityStateStore.store = this;

        return entityStateStore;
    }

    public void visitEntityStates( EntityStateVisitor visitor )
    {
        // ???
    }

    public void mergeWith( Map<String, EntityStateStore> entityStateStores )
    {
        for( Map.Entry<String, EntityStateStore> entry : entityStateStores.entrySet() )
        {
            EntityStateStore ess = unitOfWork.stateCache.get( entry.getKey() );
            if( ess == null )
            {
                unitOfWork.stateCache.put( entry.getKey(), entry.getValue() );
            }
            else
            {
                EntityState parentState = ess.state;

                if( entry.getValue().state.status() == EntityStatus.REMOVED )
                {
                    parentState.remove();
                    ess.instance.refreshState();
                }
                else
                {
                    UnitOfWorkEntityState state = (UnitOfWorkEntityState) entry.getValue().state;
                    state.mergeTo( parentState );
                    ess.instance.refreshState();
                }
            }
        }
    }

    public void refresh( String identity )
    {
        unitOfWork.refresh( identity );
    }
}
