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

package org.qi4j.spi.entitystore.helpers;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;

/**
 * Default implementation of ManyAssociationState. Backed by ArrayList.
 */
public final class DefaultManyAssociationState
    implements ManyAssociationState, Serializable
{
    private DefaultEntityState entityState;
    private List<EntityReference> references;

    public DefaultManyAssociationState( DefaultEntityState entityState, List<EntityReference> references )
    {
        this.entityState = entityState;
        this.references = references;
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
        entityState.markUpdated();
        return true;
    }

    public boolean remove( EntityReference entity )
    {
        boolean removed = references.remove( entity );
        entityState.markUpdated();
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
