/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.neo4j.state.indirect;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectIdentityList extends AbstractList<QualifiedIdentity> implements IndirectCollection
{
    private final List<ListElement> backend;
    private List<QualifiedIdentity> underlyingList;

    public IndirectIdentityList( List<QualifiedIdentity> underlyingList )
    {
        this.underlyingList = underlyingList;
        backend = new ArrayList<ListElement>( underlyingList.size() );
        Iterator<QualifiedIdentity> iter = underlyingList.iterator();
        for( int i = 0; iter.hasNext(); i++ )
        {
            backend.add( new ListElement( iter.next(), i ) );
        }
    }

    public QualifiedIdentity get( int i )
    {
        return backend.get( i ).value;
    }

    public QualifiedIdentity set( int i, QualifiedIdentity qualifiedIdentity )
    {
        ListElement old = backend.get( i );
        if( old.value.equals( qualifiedIdentity ) )
        {
            return qualifiedIdentity;
        }
        return backend.set( i, new ListElement( qualifiedIdentity ) ).value;
    }

    public void add( int i, QualifiedIdentity qualifiedIdentity )
    {
        backend.add( i, new ListElement( qualifiedIdentity ) );
    }

    public QualifiedIdentity remove( int i )
    {
        return backend.remove( i ).value;
    }

    public int size()
    {
        return backend.size();
    }

    public void prepareCommit()
    {
        int next = 0;
        ListIterator<QualifiedIdentity> iter = underlyingList.listIterator();
        for( ListElement element : backend )
        {
            if( element.index < 0 )
            {
                iter.add( element.value );
            }
            else
            {
                iter.next();
                while( element.index > next )
                {
                    iter.remove();
                    iter.next();
                    next++;
                }
                next++;
            }
        }
        while( iter.hasNext() )
        {
            iter.next();
            iter.remove();
        }
    }

    private static class ListElement
    {
        private final QualifiedIdentity value;
        private final int index;

        ListElement( QualifiedIdentity id, int index )
        {
            this.value = id;
            this.index = index;
        }

        ListElement( QualifiedIdentity id )
        {
            this( id, -1 );
        }
    }

}
