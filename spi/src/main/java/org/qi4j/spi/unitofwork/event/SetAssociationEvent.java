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

package org.qi4j.spi.unitofwork.event;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

/**
 * JAVADOC
 */
public class SetAssociationEvent extends AssociationEvent
{
    public SetAssociationEvent( EntityReference identity, StateName stateName, EntityReference associatedEntity )
    {
        super( identity, stateName, associatedEntity );
    }

    public void applyTo( EntityStoreUnitOfWork uow )
    {
        applyTo( uow.getEntityState( identity() ) );
    }

    public void applyTo( EntityState entityState )
    {
        entityState.setAssociation( stateName(), associatedEntity() );
    }
}
