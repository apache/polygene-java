/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.runtime.value;

import java.util.Iterator;
import java.util.List;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.spi.entity.ManyAssociationState;

/**
 * ManyAssociationState implementation for Value composites.
 */
public class ManyAssociationValueState
    implements ManyAssociationState
{
    private List<EntityReference> references;

    public ManyAssociationValueState( List<EntityReference> references )
    {
        this.references = references;
    }

    @Override
    public int count()
    {
        return references.size();
    }

    @Override
    public boolean contains( EntityReference entityReference )
    {
        return references.contains( entityReference );
    }

    @Override
    public boolean add( int i, EntityReference entityReference )
    {
        if( references.contains( entityReference ) )
        {
            return false;
        }

        references.add( i, entityReference );
        return true;
    }

    @Override
    public boolean remove( EntityReference entity )
    {
        boolean removed = references.remove( entity );
        return removed;
    }

    @Override
    public EntityReference get( int i )
    {
        return references.get( i );
    }

    @Override
    public Iterator<EntityReference> iterator()
    {
        final Iterator<EntityReference> iter = references.iterator();

        return new Iterator<EntityReference>()
        {
            EntityReference current;

            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public EntityReference next()
            {
                current = iter.next();
                return current;
            }

            @Override
            public void remove()
            {
                iter.remove();
            }
        };
    }
}
