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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateName;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of ManyAssociationState. Backed by ArrayList.
 */
public class DefaultManyAssociationState
    implements ManyAssociationState, Serializable
{
    private List<EntityReference> references;
    private EntityReference identity;
    private StateName stateName;
    private DefaultEntityStoreUnitOfWork unitOfWork;

    public DefaultManyAssociationState( List<EntityReference> references, EntityReference identity, StateName stateName, DefaultEntityStoreUnitOfWork unitOfWork )
    {
        this.references = references;
        this.identity = identity;
        this.stateName = stateName;
        this.unitOfWork = unitOfWork;
    }

    public int count()
    {
        return references.size();
    }

    public boolean contains( EntityReference entityReference )
    {
        return references.contains( entityReference );
    }

    public boolean add( int i, EntityReference entityReference )
    {
        if( references.contains( entityReference ) )
        {
            return false;
        }

        references.add( i, entityReference );
        unitOfWork.addManyAssociation( identity, stateName, i, entityReference );
        return true;
    }

    public boolean remove( EntityReference entity )
    {
        boolean removed = references.remove( entity );
        unitOfWork.removeManyAssociation( identity, stateName, entity );
        return removed;
    }

    public EntityReference get( int i )
    {
        return references.get( i );
    }

    public Iterator<EntityReference> iterator()
    {
        return references.iterator();
    }
}
