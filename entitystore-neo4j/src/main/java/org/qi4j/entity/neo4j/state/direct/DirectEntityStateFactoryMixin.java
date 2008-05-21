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

import java.util.Iterator;
import org.neo4j.api.core.Node;
import org.qi4j.composite.scope.Service;
import org.qi4j.entity.neo4j.NeoCoreService;
import org.qi4j.entity.neo4j.NeoEntityStateFactory;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.entity.neo4j.state.NeoEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectEntityStateFactoryMixin implements NeoEntityStateFactory
{
    private @Service NeoCoreService neo;

    public NeoEntityState createEntityState( NeoIdentityIndex idIndex, CompositeDescriptor descriptor, QualifiedIdentity identity, EntityStatus status )
    {
        Node node = idIndex.getNode( identity.getIdentity() );
        return new DirectEntityState( neo, idIndex, node, identity, status, descriptor );
    }

    public StateCommitter prepareCommit( NeoIdentityIndex idIndex, Iterable<NeoEntityState> updated, Iterable<QualifiedIdentity> removed )
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

    public Iterator<EntityState> iterator()
    {
        return null;  // TODO: implement this
    }
}
