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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateName;

/**
 * Default implementation of ManyAssociationState that also
 * keeps a list of changes that can be extracted at any time.
 */
public class DefaultDiffManyAssociationState
    implements ManyAssociationState
{
    private List<EntityReference> references;
    private StateName name;
    private EntityStateChanges changes;

    public DefaultDiffManyAssociationState( StateName name, ManyAssociationState state, EntityStateChanges changes )
    {
        this.name = name;
        this.changes = changes;

        // Copy
        for( int i = 0; i < state.count(); i++ )
        {
            references.add( state.get( i ) );
        }
    }

    public DefaultDiffManyAssociationState( StateName name, EntityStateChanges changes )
    {
        this.name = name;
        this.changes = changes;
        references = new ArrayList<EntityReference>();
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
        changes.added( name, i, entityReference );
        return true;
    }

    public boolean remove( EntityReference entityReference )
    {
        boolean removed = references.remove( entityReference );
        if( removed )
        {
            changes.removed( name, entityReference );
        }
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