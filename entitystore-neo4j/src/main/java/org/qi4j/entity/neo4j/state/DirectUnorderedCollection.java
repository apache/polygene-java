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
package org.qi4j.entity.neo4j.state;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectUnorderedCollection extends AbstractCollection<QualifiedIdentity> implements Set<QualifiedIdentity>
{
    private final DirectEntityState state;
    private final RelationshipType associationType;
    private final DuplicationChecker checker;
    private final String qualifiedName;
    private final NeoIdentityIndex idIndex;

    public DirectUnorderedCollection( NeoIdentityIndex idIndex, DuplicationChecker checker, final DirectEntityState state, final String qualifiedName )
    {
        this.idIndex = idIndex;
        this.state = state;
        this.associationType = LinkType.UNQUALIFIED.getRelationshipType( qualifiedName );
        this.qualifiedName = qualifiedName;
        this.checker = checker;
    }

    public Iterator<QualifiedIdentity> iterator()
    {
        final Iterator<Relationship> relations = state.underlyingNode.getRelationships( associationType, Direction.OUTGOING ).iterator();
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
                    return DirectEntityState.getIdentityFromNode( last.getEndNode() );
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
        return state.getSizeOfCollection( qualifiedName );
    }

    private void changeSize( int delta )
    {
        state.setSizeOfCollection( qualifiedName, size() + delta );
    }

    public boolean add( QualifiedIdentity qualifiedIdentity )
    {
        if( checker.goodToAdd( this, qualifiedIdentity ) )
        {
            Node node = idIndex.getNode( qualifiedIdentity.getIdentity() );
            state.underlyingNode.createRelationshipTo( node, associationType );
            changeSize( 1 );
            return true;
        }
        else
        {
            return false;
        }
    }
}
