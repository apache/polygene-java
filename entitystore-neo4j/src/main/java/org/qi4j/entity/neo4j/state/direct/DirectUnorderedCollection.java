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
package org.qi4j.entity.neo4j.state.direct;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.neo4j.api.core.Relationship;
import org.qi4j.entity.neo4j.state.DuplicationChecker;
import org.qi4j.entity.neo4j.state.NeoEntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationModel;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectUnorderedCollection extends AbstractCollection<QualifiedIdentity> implements Set<QualifiedIdentity>
{
    private final NeoEntityState state;
    private final AssociationModel model;
    private final DuplicationChecker checker;

    public DirectUnorderedCollection( DuplicationChecker checker, final NeoEntityState state, final AssociationModel model )
    {
        this.state = state;
        this.model = model;
        this.checker = checker;
    }

    public Iterator<QualifiedIdentity> iterator()
    {
        final Iterator<Relationship> relations = state.getRelationships( model ).iterator();
        return new Iterator<QualifiedIdentity>()
        {
            Relationship last = null;

            public boolean hasNext()
            {
                return relations.hasNext();
            }

            public QualifiedIdentity next()
            {
                if( relations.hasNext() )
                {
                    last = relations.next();
                    return NeoEntityState.getIdentityFromNode( last.getEndNode() );
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }

            public void remove()
            {
                if( last != null )
                {
                    last.delete();
                    changeSize( -1 );
                    last = null;
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
        };
    }

    public int size()
    {
        return state.getSizeOfCollection( model );
    }

    private void changeSize( int delta )
    {
        state.setSizeOfCollection( model, size() + delta );
    }

    public boolean add( QualifiedIdentity qualifiedIdentity )
    {
        if( checker.goodToAdd( this, qualifiedIdentity ) )
        {
            state.createLink( qualifiedIdentity, model );
            changeSize( 1 );
            return true;
        }
        else
        {
            return false;
        }
    }
}
