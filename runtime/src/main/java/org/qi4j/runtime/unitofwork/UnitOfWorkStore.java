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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.*;
import org.qi4j.runtime.entity.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entity.EntityStoreEvents;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

import java.util.Iterator;
import java.util.UUID;

/**
 * JAVADOC
 */
public class UnitOfWorkStore
    implements EntityStore, EntityStoreEvents
{
    private UnitOfWorkInstance unitOfWork;
    private String uuid;
    private int count;

    public UnitOfWorkStore( UnitOfWorkInstance unitOfWork )
    {
        this.unitOfWork = unitOfWork;
        uuid = UUID.randomUUID().toString() + "-";

    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, MetaInfo unitOfWorkMetaInfo )
    {
        return new DefaultEntityStoreUnitOfWork( this, newUnitOfWorkId(), usecase, unitOfWorkMetaInfo );
    }

    public StateCommitter apply( String unitOfWorkIdentity, Iterable<UnitOfWorkEvent> events, Usecase usecase, MetaInfo metaInfo ) throws EntityStoreException
    {
        // TODO
        return null;
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( unitOfWork, "",
                                                                       System.currentTimeMillis(),
                                                                       identity,
                                                                       EntityStatus.NEW );
        return entityState;
    }

    public EntityState getEntityState( EntityStoreUnitOfWork uow, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        EntityState parentState = unitOfWork.getCachedState( identity );
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( uow, parentState );
        return unitOfWorkEntityState;
    }

    public Iterator<EntityState> iterator()
    {
        return null;
    }

    public EntityState getParentEntityState( EntityReference identity )
    {
        EntityState parentState = unitOfWork.getParentEntityState( identity );
        if( parentState == null )
        {
            return null;
        }

        // TODO Needs to be fixed
        UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( null, parentState );

        return unitOfWorkEntityState;
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor )
    {
        // ???
        return null;
    }


    public void refresh( EntityReference identity )
    {
        unitOfWork.refresh( identity );
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

}
