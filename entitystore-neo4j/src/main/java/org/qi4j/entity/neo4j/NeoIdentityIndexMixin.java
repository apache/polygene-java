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

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.qi4j.composite.scope.Service;
import org.qi4j.entity.neo4j.state.DirectEntityState;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoIdentityIndexMixin implements NeoIdentityIndex
{
    private static final String TYPE_INDEX = "<ENTITY TYPES>";
    private static final String ENTITY_INDEX = "<QI4J ENTITIES>";

    // Dependancies
    private @Service NeoCoreService neo;
    private @Service NeoTransactionService txService;
    private @Service( optional = true ) NeoIdentityGeneratorService idGenerator;

    // NeoIdentityIndex implementation

    public Node getNode( String identity )
    {
        return getNode( identity, false );
    }

    public Node getOrCreateNode( String identity )
    {
        return getNode( identity, true );
    }

    public void putNode( String identity, Node node )
    {
        if( !indexWithIdGenerator( identity, node ) )
        {
            neo.index( node, ENTITY_INDEX, identity );
        }
    }

    private boolean indexWithIdGenerator( String identity, Node node )
    {
        return idGenerator != null && idGenerator.index( identity, node );
    }

    private Node getNode( String identity, boolean create )
    {
        Transaction tx = txService.beginTx();
        try
        {
            Node node = getNodeFromIdGenerator( identity );
            if( node == null )
            {
                node = neo.getSingleNode( ENTITY_INDEX, identity );
                if( node == null && create )
                {
                    node = neo.createNode();
                }
            }
            tx.success();
            return node;
        }
        finally
        {
            tx.finish();
        }
    }

    private Node getNodeFromIdGenerator( String identity )
    {
        if( idGenerator != null )
        {
            return idGenerator.getNode( identity );
        }
        else
        {
            return null;
        }
    }

    public Node getTypeNode( String type )
    {
        Transaction tx = txService.beginTx();
        try
        {
            Node typeNode = neo.getSingleNode( TYPE_INDEX, type );
            if( null == typeNode )
            {
                synchronized( this )
                {
                    typeNode = neo.getSingleNode( TYPE_INDEX, type );
                    if( null == typeNode )
                    {
                        typeNode = neo.createNode();
                        typeNode.setProperty( DirectEntityState.TYPE_PROPERTY_KEY, type );
                        neo.index( typeNode, TYPE_INDEX, type );
                    }
                }
            }
            tx.success();
            return typeNode;
        }
        finally
        {
            tx.finish();
        }
    }

}
