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

import java.io.Serializable;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.unitofwork.event.AddEntityTypeEvent;
import org.qi4j.spi.unitofwork.event.AddManyAssociationEvent;
import org.qi4j.spi.unitofwork.event.RemoveEntityTypeEvent;
import org.qi4j.spi.unitofwork.event.RemoveManyAssociationEvent;
import org.qi4j.spi.unitofwork.event.SetAssociationEvent;
import org.qi4j.spi.unitofwork.event.SetPropertyEvent;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvents;

/**
 * Tracker for changes in an EntityState
 */
public final class EntityStateChanges
    implements Serializable
{
    private UnitOfWorkEvents uow;
    private EntityReference identity;


    public EntityStateChanges( UnitOfWorkEvents uow, EntityReference identity )
    {
        this.uow = uow;
        this.identity = identity;
    }

    public void setProperty( StateName stateName, String newValue )
    {
        uow.addEvent( new SetPropertyEvent( identity, stateName, newValue ) );
    }

    public void setAssociation( StateName stateName, EntityReference entityReference )
    {
        uow.addEvent( new SetAssociationEvent( identity, stateName, entityReference ) );
    }

    public void added( StateName stateName, int index, EntityReference reference )
    {
        uow.addEvent( new AddManyAssociationEvent( identity, stateName, index, reference ) );
    }

    public void removed( StateName stateName, EntityReference reference )
    {
        uow.addEvent( new RemoveManyAssociationEvent( identity, stateName, reference ) );
    }

    public void addEntityTypeReference( EntityTypeReference entityTypeReference )
    {
        uow.addEvent( new AddEntityTypeEvent( identity, entityTypeReference ) );
    }

    public void removeEntityTypeReference( EntityTypeReference entityTypeReference )
    {
        uow.addEvent( new RemoveEntityTypeEvent( identity, entityTypeReference ) );
    }
}
