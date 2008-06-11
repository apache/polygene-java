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
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.injection.scope.Service;
import org.qi4j.service.Activatable;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoIdentityMixin
    implements IdentityGenerator, NeoIdentityIndex, Activatable, PassivationListener
{
    private static final String TYPE_INDEX = "<ENTITY TYPES>";
    private static final String FOREIGN_ENTITY_INDEX = "<FOREIGN ENTITIES>";

    // Dependancies
    private @Service NeoCoreService neo;
    private @Service NeoTransactionService txService;

    // Internal state
    private boolean garbageCollected = false;

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

    // NeoIdentityIndex implementation

    public Node getNode( String identity )
    {
        // TODO: use the foreign entity index to lookup foreign entities
        Transaction tx = txService.beginTx();
        try
        {
            Node node = neo.getNodeById( toNeoId( identity ) );
            Relationship newNodeRel = node.getSingleRelationship( newNodeType, Direction.INCOMING );
            if( newNodeRel != null )
            {
                newNodeRel.delete();
            }
            tx.success();
            return node;
        }
        finally
        {
            tx.finish();
        }
    }

    public Node getOrCreateNode( String identity )
    {
        //TODO: Auto-generated, need attention.
        return null;
    }

    public void putNode( String identity, Node node )
    {
        //TODO: Auto-generated, need attention.

    }

    public Node getTypeNode( String type )
    {
        Transaction tx = txService.beginTx();
        try
        {
            Node typeNode = neo.getSingleNode( TYPE_INDEX, type );
            if( null == typeNode )
            {
                // NOTE: we might need some kind of thread safety here, double checking or something like that.
                typeNode = neo.createNode();

// TODO: Compile Error, so take away this line for now
//                typeNode.setProperty( NeoEntityState.TYPE_PROPERTY, type );
                neo.index( typeNode, TYPE_INDEX, type );
            }
            tx.success();
            return typeNode;
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
    private static final String RELATIONSHIP_TYPE_NAME = NeoIdentityMixin.class.getName() + "::newNodeType";
    private static final RelationshipType newNodeType = new RelationshipType()
    {
        public String name()
        {
            return RELATIONSHIP_TYPE_NAME;
        }
    };

    private long toNeoId( String identity )
    {
        long id;
        if( identity.startsWith( ID_PREFIX ) )
        {
            try
            {
                id = Long.parseLong( identity.substring( ID_PREFIX.length() ) );
            }
            catch( NumberFormatException ex )
            {
                throw new IllegalArgumentException( ID_FORMAT_ERROR_MESSAGE + identity, ex );
            }
        }
        else
        {
            throw new IllegalArgumentException( ID_FORMAT_ERROR_MESSAGE + identity );
        }
        return id;
    }

    private String toIdentity( long id )
    {
        return ID_PREFIX + id;
    }
}
