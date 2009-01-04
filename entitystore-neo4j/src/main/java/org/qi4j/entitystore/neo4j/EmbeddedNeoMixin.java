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
package org.qi4j.entitystore.neo4j;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.index.IndexService;
import org.neo4j.util.index.Isolation;
import org.neo4j.util.index.LuceneIndexService;
import org.neo4j.util.index.NeoIndexService;
import org.qi4j.api.service.Activatable;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class EmbeddedNeoMixin implements Activatable, NeoService, IndexService, PassivationBroadcaster, NeoTransactionService
{
    // Internal state
    private NeoService neoImpl;
    private IndexService indexImpl;
    private List<WeakReference<PassivationListener>> passivationListeners = new LinkedList<WeakReference<PassivationListener>>();

    // Activatable implementation

    public void activate() throws Exception
    {
        // TODO: use a Qi4j specific way of passing configuration parameters.
        //neoImpl = new EmbeddedNeo(config.configuration().path());
        neoImpl = new EmbeddedNeo( System.getProperty( "neo.nodestore.path", "target/neo" ) );
        if(/*config.configuration().useLuceneIndex()*/ false )
        {
            indexImpl = new LuceneIndexService( neoImpl );
        }
        else
        {
            indexImpl = new NeoIndexService( neoImpl );
        }
    }

    public void passivate() throws Exception
    {
        for( WeakReference<PassivationListener> reference : passivationListeners )
        {
            PassivationListener listener = reference.get();
            if( listener != null )
            {
                listener.handlePassivation();
            }
        }
        shutdown();
        indexImpl = null;
        neoImpl = null;
    }

    // NeoService implementation

    public Node createNode()
    {
        return neoImpl.createNode();
    }

    public Node getNodeById( long id )
    {
        return neoImpl.getNodeById( id );
    }

    public Node getReferenceNode()
    {
        return neoImpl.getReferenceNode();
    }

    public boolean enableRemoteShell()
    {
        return neoImpl.enableRemoteShell();
    }

    public boolean enableRemoteShell( Map<String, Serializable> config )
    {
        return neoImpl.enableRemoteShell( config );
    }

    public Transaction beginTx()
    {
        return neoImpl.beginTx();
    }

    public boolean inTransaction()
    {
        TransactionManager manager = ( (EmbeddedNeo) neoImpl ).getConfig().getTxModule().getTxManager();
        try
        {
            return manager.getTransaction() != null;
        }
        catch( SystemException e )
        {
            return false;
        }
    }

    public void shutdown()
    {
        // Shared method between NeoService and IndexService
        indexImpl.shutdown();
        neoImpl.shutdown();
    }

    // IndexService implementation

    public void index( Node node, String s, Object o )
    {
        indexImpl.index( node, s, o );
    }

    public Node getSingleNode( String s, Object o )
    {
        return indexImpl.getSingleNode( s, o );
    }

    public Iterable<Node> getNodes( String s, Object o )
    {
        return indexImpl.getNodes( s, o );
    }

    public void removeIndex( Node node, String s, Object o )
    {
        indexImpl.removeIndex( node, s, o );
    }

    public void setIsolation( Isolation isolation )
    {
        indexImpl.setIsolation( isolation );
    }

    // PassivationBroadcaster implementation

    public void addPassivationListener( PassivationListener listener )
    {
        passivationListeners.add( new WeakReference<PassivationListener>( listener ) );
    }

	public Relationship getRelationshipById(long arg0) {
		return neoImpl.getRelationshipById(arg0);
	}
}
