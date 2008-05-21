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

import java.util.Iterator;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.qi4j.composite.scope.Service;
import org.qi4j.entity.neo4j.NeoEntityStateFactory;
import org.qi4j.entity.neo4j.NeoIdentityIndex;
import org.qi4j.entity.neo4j.NeoTransactionService;
import org.qi4j.entity.neo4j.state.NeoEntityState;
import org.qi4j.entity.neo4j.state.direct.DirectEntityState;
import org.qi4j.entity.neo4j.state.direct.DirectEntityStateFactory;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityStateFactoryMixin implements NeoEntityStateFactory
{
    private @Service DirectEntityStateFactory directFactory;
    private @Service NeoTransactionService txFactory;

    public EntityState createEntityState( NeoIdentityIndex idIndex, CompositeDescriptor descriptor, QualifiedIdentity identity, EntityStatus status )
    {
        Transaction tx = txFactory.beginTx();
        try
        {
            return new IndirectEntityState( (DirectEntityState) directFactory.createEntityState( idIndex, descriptor, identity, status ), descriptor );
        }
        finally
        {
            tx.finish();
        }
    }

    public StateCommitter prepareCommit( NeoIdentityIndex idIndex, Iterable<NeoEntityState> updated, Iterable<QualifiedIdentity> removed ) throws EntityStoreException
    {
        final Transaction tx = txFactory.beginTx();
        try
        {
            for( NeoEntityState state : updated )
            {
                state.prepareCommit();
            }
            for( QualifiedIdentity removedId : removed )
            {
                Node removedNode = idIndex.getNode( removedId.getIdentity() );
                for( Relationship relation : removedNode.getRelationships( Direction.OUTGOING ) )
                {
                    relation.delete();
                }
            }
            for( QualifiedIdentity removedId : removed )
            {
                Node removedNode = idIndex.getNode( removedId.getIdentity() );
                for( Relationship relation : removedNode.getRelationships( Direction.INCOMING ) )
                {
                    throw new EntityStoreException( "Cannot remove " + removedId + ", it has incoming references." );
                }
                removedNode.delete();
            }
            return new StateCommitter()
            {
                public void commit()
                {
                    tx.success();
                    tx.finish();
                }

                public void cancel()
                {
                    tx.failure();
                    tx.finish();
                }
            };
        }
        catch( EntityStoreException ex )
        {
            tx.failure();
            tx.finish();
            throw ex;
        }
        catch( Exception ex )
        {
            tx.failure();
            tx.finish();
            throw new EntityStoreException( ex );
        }
    }

    public Iterator<EntityState> iterator()
    {
        return null;  // TODO: implement this
    }
}
