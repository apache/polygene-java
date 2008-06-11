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
package org.qi4j.entity.neo4j;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.qi4j.entity.Identity;
import org.qi4j.injection.scope.Service;
import org.qi4j.service.Activatable;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoIdentityGeneratorMixin implements NeoIdentityGenerator, Activatable, PassivationListener
{
    // Dependancies
    private @Service NeoCoreService neo;
    private @Service NeoTransactionService txService;

    // Internal state
    private boolean garbageCollected = false;

    // NeoIdentityGenerator implementation

    public Node getNode( String identity )
    {
        if( isNeoId( identity ) )
        {
            return neo.getNodeById( toNeoId( identity ) );
        }
        else
        {
            return null;
        }
    }

    public boolean index( String identity, Node node )
    {
        if( isNeoId( identity ) )
        {
            Relationship newNodeRel = node.getSingleRelationship( newNodeType, Direction.INCOMING );
            if( newNodeRel != null )
            {
                newNodeRel.delete();
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    // IdentityGenerator implementation

    public String generate( Class<? extends Identity> compositeType )
    {
        Transaction tx = txService.beginTx();
        try
        {
            Node referenceNode = neo.getReferenceNode();
            Node node = neo.createNode();
            referenceNode.createRelationshipTo( node, newNodeType );
            long id = node.getId();
            tx.success();
            return toIdentity( id );
        }
        finally
        {
            tx.finish();
        }
    }

    // Activatable implementation

    public void activate() throws Exception
    {
        garbageCollected = false;
        neo.addPassivationListener( this );
    }

    public void passivate() throws Exception
    {
        handlePassivation();
    }

    // PassivationListener implementation

    public void handlePassivation()
    {
        synchronized( this )
        {
            if( !garbageCollected )
            {
                garbageCollectIndices();
                garbageCollected = true;
            }
        }
    }

    // Implementation internals

    private void garbageCollectIndices()
    {
        Transaction tx = txService.beginTx();
        try
        {
            for( Relationship newNodeRel : neo.getReferenceNode().getRelationships( newNodeType, Direction.OUTGOING ) )
            {
                Node garbageNode = newNodeRel.getEndNode();
                newNodeRel.delete();
                garbageNode.delete();
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private static final String ID_FORMAT_ERROR_MESSAGE = "The supplied identity was not a valid Neo Identity. Identity = ";
    private static final String ID_PREFIX = "NeoNodeId:";
    private static final String RELATIONSHIP_TYPE_NAME = NeoIdentityGenerator.class.getName() + "::newNodeType";
    private static final RelationshipType newNodeType = new RelationshipType()
    {
        public String name()
        {
            return RELATIONSHIP_TYPE_NAME;
        }
    };

    private boolean isNeoId( String identity )
    {
        return identity.startsWith( ID_PREFIX );
    }

    private long toNeoId( String identity )
    {
        long id;
        try
        {
            id = Long.parseLong( identity.substring( ID_PREFIX.length() ) );
        }
        catch( NumberFormatException ex )
        {
            throw new IllegalArgumentException( ID_FORMAT_ERROR_MESSAGE + identity, ex );
        }
        return id;
    }

    private String toIdentity( long id )
    {
        return ID_PREFIX + id;
    }
}
