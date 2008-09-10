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
import java.util.List;
import java.util.ListIterator;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Implementation of ListAssociation, which delegates to a
 * List provided by the EntityStore.
 */
public final class ListAssociationInstance<T>
    extends ManyAssociationInstance<T>
    implements ListAssociation<T>
{
    private List<QualifiedIdentity> associated;

    public ListAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, List<QualifiedIdentity> associated )
    {
        super( associationInfo, unitOfWork, associated );
        this.associated = associated;
    }

    public T get( int i )
    {
        return getEntity( associated.get( i ) );
    }

    public T set( int i, T t )
    {
        checkType( t );

        return getEntity( associated.set( i, getEntityId( t ) ) );
    }

    public void add( int i, T t )
    {
        checkType( t );

        associated.add( i, getEntityId( t ) );
    }

    public T remove( int i )
    {
        return getEntity( associated.remove( i ) );
    }

    public int indexOf( Object o )
    {
        checkType( o );

        return associated.indexOf( getEntityId( o ) );
    }

    public int lastIndexOf( Object o )
    {
        checkType( o );

        return associated.lastIndexOf( o );
    }

    public boolean addAll( int i, Collection<? extends T> ts )
    {
        Collection<QualifiedIdentity> list = getEntityIdCollection( ts );

        return associated.addAll( i, list );
    }

    public ListIterator<T> listIterator()
    {
        return new ListAssociationListIterator( associated.listIterator() );
    }

    public ListIterator<T> listIterator( int i )
    {
        return new ListAssociationListIterator( associated.listIterator( i ) );
    }

    public List<T> subList( int i, int i1 )
    {
        List<QualifiedIdentity> subList = associated.subList( i, i1 );
        return new ListAssociationInstance<T>( associationInfo, unitOfWork, subList );
    }

    public void refresh( List<QualifiedIdentity> newList )
    {
        associated = newList;
    }

    private class ListAssociationListIterator
        implements ListIterator<T>
    {
        private final ListIterator<QualifiedIdentity> idIterator;

        public ListAssociationListIterator( ListIterator<QualifiedIdentity> idIterator )
        {
            this.idIterator = idIterator;
        }

        public boolean hasNext()
        {
            return idIterator.hasNext();
        }

        public T next()
        {
            return getEntity( idIterator.next() );
        }

        public boolean hasPrevious()
        {
            return idIterator.hasPrevious();
        }

        public T previous()
        {
            return getEntity( idIterator.previous() );
        }

        public int nextIndex()
        {
            return idIterator.nextIndex();
        }

        public int previousIndex()
        {
            return idIterator.previousIndex();
        }

        public void remove()
        {
            idIterator.remove();
        }

        public void set( Object o )
        {
            idIterator.set( getEntityId( o ) );
        }

        public void add( Object o )
        {
            idIterator.add( getEntityId( o ) );
        }
    }
}
