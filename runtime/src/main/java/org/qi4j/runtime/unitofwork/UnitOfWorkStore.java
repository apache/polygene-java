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

import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * JAVADOC
*/
public class UnitOfWorkStore
    implements EntityStore
{
    final Map<String, EntityType> entityTypes = new HashMap<String, EntityType>();
    private UnitOfWorkInstance unitOfWork;

    public UnitOfWorkStore( UnitOfWorkInstance unitOfWork)
    {
        this.unitOfWork = unitOfWork;
    }

    public void registerEntityType( EntityType entityType )
    {
        entityTypes.put( entityType.type(), entityType );
    }

    public EntityType getEntityType( String aEntityType )
    {
        return entityTypes.get( aEntityType );
    }

    public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = entityTypes.get( identity.type() );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }

        UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( 0, System.currentTimeMillis(), identity, EntityStatus.NEW, entityType,
                                                                       new HashMap<QualifiedName, Object>(), new HashMap<QualifiedName, QualifiedIdentity>(), new HashMap<QualifiedName, Collection<QualifiedIdentity>>(), null );
        return entityState;
    }

    public EntityState getEntityState( QualifiedIdentity identity ) throws EntityStoreException
    {
        EntityType entityType = entityTypes.get( identity.type() );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }

        EntityState parentState = unitOfWork.getCachedState( identity );
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.version(),
                                                                                 parentState.lastModified(),
                                                                                 identity,
                                                                                 EntityStatus.LOADED,
                                                                                 entityType,
                                                                                 new HashMap<QualifiedName, Object>(),
                                                                                 new HashMap<QualifiedName, QualifiedIdentity>(),
                                                                                 new HashMap<QualifiedName, Collection<QualifiedIdentity>>(),
                                                                                 parentState );
        return unitOfWorkEntityState;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
    {
        // Unused
        return null;
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }

    public EntityStateStore getEffectiveEntityStateStore( QualifiedIdentity qi, EntityType entityType )
    {
        EntityStateStore entityStateStore = unitOfWork.getEffectiveEntityStateStore( qi, entityType );
        if( entityStateStore == null )
        {
            return null;
        }

        EntityState parentState = entityStateStore.state;
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.version(),
                                                                                 parentState.lastModified(),
                                                                                 qi,
                                                                                 EntityStatus.LOADED,
                                                                                 entityType,
                                                                                 new HashMap<QualifiedName, Object>(),
                                                                                 new HashMap<QualifiedName, QualifiedIdentity>(),
                                                                                 new HashMap<QualifiedName, Collection<QualifiedIdentity>>(),
                                                                                 parentState );
        entityStateStore = new EntityStateStore();
        entityStateStore.state = unitOfWorkEntityState;
        entityStateStore.store = this;

        return entityStateStore;
    }

    public void mergeWith( Map<QualifiedIdentity, EntityStateStore> entityStateStores )
    {
        for( Map.Entry<QualifiedIdentity, EntityStateStore> entry : entityStateStores.entrySet() )
        {
            EntityStateStore ess = unitOfWork.stateCache.get( entry.getKey() );
            if( ess == null )
            {
                unitOfWork.stateCache.put( entry.getKey(), entry.getValue() );
            }
            else
            {
                EntityState parentState = ess.state;

                if (entry.getValue().state.status() == EntityStatus.REMOVED)
                {
                    parentState.remove();
                    ess.instance.refreshState();
                } else
                {
                    UnitOfWorkEntityState state = (UnitOfWorkEntityState) entry.getValue().state;
                    state.mergeTo( parentState );
                    ess.instance.refreshState();
                }
            }
        }
    }

    public void refresh( QualifiedIdentity qid )
    {
        unitOfWork.refresh( qid );
    }
}
