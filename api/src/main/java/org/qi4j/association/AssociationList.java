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

package org.qi4j.association;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 */
public final class AssociationList<T> extends AbstractList<T>
{
    ListAssociation<T> association;

    public AssociationList( ListAssociation<T> association )
    {
        this.association = association;

    }

    public T get( int i )
    {
        return association.get( i );
    }


    @Override public boolean add( T o )
    {
        association.add( o );
        return true;
    }


    @Override public T set( int i, T o )
    {
        T old = association.remove( i );
        association.add( i, o );

        return old;
    }

    @Override public void add( int i, T o )
    {
        association.add( i, o );
    }


    @Override public T remove( int i )
    {
        return association.remove( i );
    }

    @Override public int indexOf( Object o )
    {
        int idx = 0;
        for( T object : association )
        {
            if( o.equals( object ) )
            {
                return idx;
            }
            idx++;
        }

        return -1;
    }

    @Override public int lastIndexOf( Object o )
    {
        int idx = 0;
        int foundIdx = -1;
        for( T object : association )
        {
            if( o.equals( object ) )
            {
                foundIdx = idx;
            }
            idx++;
        }

        return foundIdx;
    }


    @Override public void clear()
    {
        int size = association.size();
        for( int i = 0; i < size; i++ )
        {
            association.remove( 0 );
        }
    }


    @Override public boolean addAll( int i, Collection<? extends T> collection )
    {
        for( T object : collection )
        {
            association.add( i, object );
        }
        return true;
    }


    @Override public Iterator<T> iterator()
    {
        return association.iterator();
    }

    @Override public ListIterator<T> listIterator()
    {
        return null; // TODO
    }

    @Override public ListIterator<T> listIterator( int i )
    {
        return null; // TODO
    }

    @Override public List<T> subList( int from, int to )
    {
        List<T> subList = new ArrayList<T>();
        for( int i = from; i < to; i++ )
        {
            subList.add( association.get( i ) );
        }

        return subList;
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        if( !super.equals( o ) )
        {
            return false;
        }

        AssociationList that = (AssociationList) o;

        if( !association.equals( that.association ) )
        {
            return false;
        }

        return true;
    }

    @Override public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + association.hashCode();
        return result;
    }

    @Override public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override public boolean contains( Object o )
    {
        return indexOf( o ) != -1;
    }

    @Override public T[] toArray()
    {
        T[] array = (T[]) new Object[size()];
        int idx = 0;
        for( T object : association )
        {
            array[ idx ] = object;
        }

        return array;
    }

    @Override public <T> T[] toArray( T[] array )
    {
        int idx = 0;
        for( Object object : association )
        {
            array[ idx ] = (T) object;
        }

        return array;
    }

    @Override public boolean remove( Object o )
    {
        return association.remove( (T) o );
    }

    @Override public boolean containsAll( Collection<?> objects )
    {
        return super.containsAll( objects );
    }

    @Override public boolean addAll( Collection<? extends T> ts )
    {
        for( T t : ts )
        {
            association.add( t );
        }
        return true;
    }

    @Override public boolean removeAll( Collection objects )
    {
        for( Object object : objects )
        {
            association.remove( (T) object );
        }
        return super.removeAll( objects );
    }

    @Override public boolean retainAll( Collection<?> objects )
    {
        for( T t : association )
        {
            if( !objects.contains( t ) )
            {
                association.remove( t );
            }
        }

        return true;
    }

    public int size()
    {
        return association.size();
    }
}
