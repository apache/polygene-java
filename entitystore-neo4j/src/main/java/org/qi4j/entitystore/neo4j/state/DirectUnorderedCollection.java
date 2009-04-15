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

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateName;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectUnorderedCollection extends AbstractCollection<EntityReference> implements ManyAssociationState
{
    private final DirectEntityState state;
    private final RelationshipType associationType;
    private final DuplicationChecker checker;
    private final StateName stateName;
    private final NeoIdentityIndex idIndex;

    public DirectUnorderedCollection(NeoIdentityIndex idIndex, DuplicationChecker checker, final DirectEntityState state, final StateName stateName)
    {
        this.idIndex = idIndex;
        this.state = state;
        this.associationType = LinkType.UNQUALIFIED.getRelationshipType(stateName.qualifiedName().name());
        this.stateName = stateName;
        this.checker = checker;
    }

    public int count()
    {
        return state.getSizeOfCollection(stateName);
    }

    public boolean contains(EntityReference entityReference)
    {
        // TODO NYI
        return false;
    }

    public boolean add(int index, EntityReference entityReference)
    {
        // TODO NYI
        return false;
    }

    public boolean remove(EntityReference entityReference)
    {
        // TODO NYI
        return false;
    }

    public EntityReference get(int index)
    {
        // TODO NYI
        return null;
    }

    public Iterator<EntityReference> iterator()
    {
        final Iterator<Relationship> relations = state.underlyingNode.getRelationships(associationType, Direction.OUTGOING).iterator();
        return new Iterator<EntityReference>()
        {
            Relationship last = null;

            public boolean hasNext()
            {
                return relations.hasNext();
            }

            public EntityReference next()
            {
                if (relations.hasNext())
                {
                    last = relations.next();
                    return DirectEntityState.getIdentityFromNode(DirectEntityState.unproxy(last.getEndNode()));
                } else
                {
                    throw new NoSuchElementException();
                }
            }

            public void remove()
            {
                if (last != null)
                {
                    DirectEntityState.removeProxy(last.getEndNode());
                    last.delete();
                    changeSize(-1);
                    last = null;
                } else
                {
                    throw new IllegalStateException();
                }
            }
        };
    }

    public int size()
    {
        return state.getSizeOfCollection(stateName);
    }

    private void changeSize(int delta)
    {
        state.setSizeOfCollection(stateName.qualifiedName(), size() + delta);
    }

    public boolean add(EntityReference entityReference)
    {
        if (checker.goodToAdd(this, entityReference))
        {
            Node node = idIndex.getNode(entityReference.identity());
            if (state.underlyingNode.equals(node))
            {
                node = DirectEntityState.proxy(state.neo, node);
            }
            state.underlyingNode.createRelationshipTo(node, associationType);
            changeSize(1);
            return true;
        } else
        {
            return false;
        }
    }
}
