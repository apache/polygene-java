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

import java.util.Iterator;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.entitystore.neo4j.NeoCoreService;
import org.qi4j.entitystore.neo4j.NeoIdentityIndex;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.StateCommitter;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectEntityStateFactoryMixin implements NodeEntityStateFactory
{
    private
    @Service
    NeoCoreService neo;

    public CommittableEntityState createEntityState(NeoIdentityIndex idIndex, LoadedDescriptor descriptor, EntityReference reference, EntityStatus status)
    {
        Node node = idIndex.getOrCreateNode(reference.identity());
        return createEntityState(idIndex, node, descriptor, reference, status);
    }

    public CommittableEntityState createEntityState(NeoIdentityIndex idIndex, Node node, LoadedDescriptor descriptor, EntityReference reference, EntityStatus status)
    {
        return null; // new DirectEntityState(neo, idIndex, node, reference, status, descriptor);
    }

    public CommittableEntityState loadEntityStateFromNode(NeoIdentityIndex idIndex, Node node)
    {
        EntityReference reference = DirectEntityState.getIdentityFromNode(node);
/* TODO
        LoadedDescriptor descriptor = LoadedDescriptor.loadDescriptor( idIndex.getTypeNode( reference.type() ) );
        return createEntityState( idIndex, node, descriptor, reference, EntityStatus.LOADED );
*/
        return null;
    }

    public StateCommitter prepareCommit(NeoIdentityIndex idIndex, Iterable<CommittableEntityState> updated, Iterable<EntityReference> removed)
    {
        return new StateCommitter()
        {
            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    public Iterator<CommittableEntityState> iterator(final NeoIdentityIndex idIndex)
    {
        final Iterator<Node> nodes = null; // TODO: get all nodes from Neo
        return new Iterator<CommittableEntityState>()
        {
            Node next = null;
            CommittableEntityState previous = null;

            public boolean hasNext()
            {
                if (next != null)
                {
                    return true;
                }
                while (nodes.hasNext())
                {
                    Node node = nodes.next();
                    if (node.hasRelationship(DirectEntityState.PROXY_FOR, Direction.OUTGOING))
                    {
                        next = node;
                        return true;
                    }
                }
                return false;
            }

            public CommittableEntityState next()
            {
                if (hasNext())
                {
                    previous = loadEntityStateFromNode(idIndex, next);
                }
                return previous;
            }

            public void remove()
            {
                if (previous != null)
                {
                    previous.remove();
                } else
                {
                    throw new IllegalStateException();
                }
            }
        };
    }
}
