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

package org.qi4j.spi.association;

import java.lang.reflect.Type;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.qi4j.association.AssociationInfo;
import org.qi4j.association.AssociationVetoException;
import org.qi4j.association.SetAssociation;
import org.qi4j.entity.EntityComposite;

/**
 * Implementation of SetAssociation, which delegates to an
 * ordinary Set.
 */
public final class SetAssociationInstance<T> extends AbstractSet<T>
    implements SetAssociation<T>
{
    private AssociationInfo associationInfo;
    private Set<T> associated;

    public SetAssociationInstance( Set<T> associated, AssociationInfo associationInfo )
    {
        this.associationInfo = associationInfo;
        this.associated = associated;
    }

    public Set<T> getAssociatedSet()
    {
        return associated;
    }

    public <T> T getAssociationInfo( Class<T> infoType )
    {
        return associationInfo.getAssociationInfo( infoType );
    }

    public String getName()
    {
        return associationInfo.getName();
    }

    public String getQualifiedName()
    {
        return associationInfo.getQualifiedName();
    }

    public Type getAssociationType()
    {
        return associationInfo.getAssociationType();
    }

    @Override public boolean removeAll( Collection<?> objects )
    {
        return associated.removeAll( objects );
    }

    @Override public boolean isEmpty()
    {
        return associated.isEmpty();
    }

    @Override public boolean contains( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new AssociationVetoException( "Object must be an EntityComposite" );
        }

        return associated.contains( o );
    }

    @Override public Object[] toArray()
    {
        return associated.toArray();
    }

    @Override public <T> T[] toArray( T[] ts )
    {
        return associated.toArray( ts );
    }

    @Override public boolean add( T t )
    {
        if( !( t instanceof EntityComposite ) )
        {
            throw new AssociationVetoException( "Associated object must be an EntityComposite" );
        }

        return associated.add( t );
    }

    @Override public boolean remove( Object o )
    {
        if( !( o instanceof EntityComposite ) )
        {
            throw new AssociationVetoException( "Associated object must be an EntityComposite" );
        }

        return associated.remove( o );
    }

    @Override public boolean containsAll( Collection<?> objects )
    {
        return associated.containsAll( objects );
    }

    @Override public boolean addAll( Collection<? extends T> ts )
    {
        return associated.addAll( ts );
    }

    @Override public boolean retainAll( Collection<?> objects )
    {
        return associated.retainAll( objects );
    }

    @Override public void clear()
    {
        associated.clear();
    }

    @Override public String toString()
    {
        return associated.toString();
    }

    public Iterator<T> iterator()
    {
        return associated.iterator();
    }

    public int size()
    {
        return associated.size();
    }
}