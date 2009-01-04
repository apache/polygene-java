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
package org.qi4j.entitystore.neo4j.state;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectUnorderedCollection extends AbstractCollection<QualifiedIdentity> implements Set<QualifiedIdentity>, IndirectCollection
{
    private final Collection<QualifiedIdentity> backend;
    private final Collection<QualifiedIdentity> added;
    private final List<QualifiedIdentity> removed = new LinkedList<QualifiedIdentity>();
    private final Collection<QualifiedIdentity> underlyingCollection;

    public IndirectUnorderedCollection( BackendFactory factory, Collection<QualifiedIdentity> underlyingCollection )
    {
        backend = factory.createBackend( QualifiedIdentity.class );
        added = factory.createBackend( QualifiedIdentity.class );
        this.underlyingCollection = underlyingCollection;
        for( QualifiedIdentity id : underlyingCollection )
        {
            backend.add( id );
        }
    }

    public Iterator<QualifiedIdentity> iterator()
    {
        return backend.iterator();
    }

    public int size()
    {
        return backend.size();
    }

    @Override
    public boolean add( QualifiedIdentity qualifiedIdentity )
    {
        boolean result = backend.add( qualifiedIdentity );
        if( result )
        {
            added.add( qualifiedIdentity );
        }
        return result;
    }

    @Override
    public boolean remove( Object o )
    {
        if( o instanceof QualifiedIdentity )
        {
            QualifiedIdentity identity = (QualifiedIdentity) o;
            if( backend.remove( identity ) )
            {
                if( !added.remove( identity ) )
                {
                    removed.add( identity );
                }
                return true;
            }
        }
        return false;
    }

    public void prepareCommit()
    {
        for( QualifiedIdentity id : removed )
        {
            underlyingCollection.remove( id );
        }
        for( QualifiedIdentity id : added )
        {
            underlyingCollection.add( id );
        }
    }
}
