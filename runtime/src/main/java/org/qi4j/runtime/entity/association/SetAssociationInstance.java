/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity.association;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.serialization.EntityId;

/**
 * Implementation of SetAssociation, which delegates to a
 * Set provided by the EntityStore.
 */
public final class SetAssociationInstance<T>
    extends AbstractManyAssociationInstance<T>
    implements SetAssociation<T>
{
    private Set<EntityId> associated;

    public SetAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, Set<EntityId> associated )
    {
        super( associationInfo, unitOfWork );
        this.associated = associated;
    }

    public Set<EntityId> getAssociatedSet()
    {
        return associated;
    }

    public boolean removeAll( Collection<?> objects )
    {
        return associated.removeAll( getEntityIdCollection( objects ) );
    }

    public boolean isEmpty()
    {
        return associated.isEmpty();
    }

    public boolean contains( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Object must be an EntityComposite" );
        }

        return associated.contains( getEntityId( o ) );
    }


    public Object[] toArray()
    {
        Object[] ids = associated.toArray();
        for( int i = 0; i < ids.length; i++ )
        {
            ids[ i ] = getEntity( (EntityId) ids[ i ] );
        }

        return ids;
    }

    public <T> T[] toArray( T[] ts )
    {
        EntityId[] ids = new EntityId[ts.length];
        associated.toArray( ids );
        for( int i = 0; i < ids.length; i++ )
        {
            EntityId id = ids[ i ];
            ts[ i ] = (T) getEntity( id );
        }
        return ts;
    }

    public boolean add( T t )
    {
        if( !( t instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Associated object must be an EntityComposite" );
        }

        return associated.add( getEntityId( t ) );
    }

    public boolean remove( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Associated object must be an EntityComposite" );
        }

        return associated.remove( getEntityId( o ) );
    }

    public boolean containsAll( Collection<?> objects )
    {
        return associated.containsAll( getEntityIdCollection( objects ) );
    }

    public boolean addAll( Collection<? extends T> ts )
    {
        return associated.addAll( getEntityIdCollection( ts ) );
    }

    public boolean retainAll( Collection<?> objects )
    {
        return associated.retainAll( getEntityIdCollection( objects ) );
    }

    public void clear()
    {
        associated.clear();
    }

    public String toString()
    {
        return associated.toString();
    }

    public Iterator<T> iterator()
    {
        return new ManyAssociationIterator( associated.iterator() );
    }

    public int size()
    {
        return associated.size();
    }

    public void refresh( Set<EntityId> newSet )
    {
        associated = newSet;
    }
}