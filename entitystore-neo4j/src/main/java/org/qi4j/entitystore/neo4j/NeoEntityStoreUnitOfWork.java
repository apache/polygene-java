package org.qi4j.entitystore.neo4j;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.structure.ModuleSPI;

public class NeoEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork,
               StateCommitter
{
    static final String ENTITY_STATE_ID = "entity_state_id";
    static final String ENTITY_TYPE = "entity_type";

    private final EmbeddedGraphDatabase neo;
    private final IndexService indexService;
    private long currentTime;
    private final TransactionManager tm;

    private final Transaction transaction;
    private final String identity;
    private final Module module;

    NeoEntityStoreUnitOfWork( EmbeddedGraphDatabase neo, IndexService indexService,
                              String identity, Module module,
                              long currentTime )
    {
        this.neo = neo;
        this.indexService = indexService;
        this.currentTime = currentTime;
        this.tm = this.neo.getConfig().getTxModule().getTxManager();
        this.transaction = beginTransaction();
        this.identity = identity;
        this.module = module;
    }

    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return this;
    }

    public long currentTime()
    {
        return currentTime;
    }

    public void discard()
    {
        cancel();
    }

    Node getEntityStateNode( EntityReference anIdentity )
    {
        Node node = indexService.getSingleNode( ENTITY_STATE_ID,
                                                anIdentity.identity() );
        if( node == null )
        {
            throw new EntityNotFoundException( anIdentity );
        }
        return node;
    }

    public EntityState getEntityState( EntityReference anIdentity )
        throws EntityStoreException, EntityNotFoundException
    {
        return new NeoEntityState( this, getEntityStateNode( anIdentity ),
                                   EntityStatus.LOADED );
    }

    public EntityState newEntityState( EntityReference anIdentity,
                                       EntityDescriptor entityDescriptor
    )
        throws EntityStoreException
    {
        String type = entityDescriptor.entityType().type().name();
        Node typeNode = indexService.getSingleNode( ENTITY_TYPE, type );
        if( typeNode == null )
        {
            typeNode = createEntityType( type );
        }
        Node node = indexService.getSingleNode( ENTITY_STATE_ID,
                                                anIdentity.identity() );
        if( node != null )
        {
            throw new EntityAlreadyExistsException( anIdentity );
        }
        node = neo.createNode();
        node.setProperty( NeoEntityState.VERSION, 0l );
        node.setProperty( NeoEntityState.MODIFIED, currentTime );
        node.createRelationshipTo( typeNode, RelTypes.IS_OF_TYPE );
        node.setProperty( NeoEntityState.ENTITY_ID, anIdentity.identity() );
        indexService.index( node, ENTITY_STATE_ID, anIdentity.identity() );
        return new NeoEntityState( this, node, EntityStatus.NEW );
    }

    public void cancel()
    {
        try
        {
            tm.rollback();
        }
        catch( Exception e )
        {
            throw new EntityStoreException(
                "Failed to rollback transaction.", e );
        }
    }

    public void commit()
    {
        try
        {
            tm.commit();
        }
        catch( Exception e )
        {
            throw new EntityStoreException(
                "Failed to commit transaction.", e );
        }
    }

    private Transaction beginTransaction()
    {
        try
        {
            tm.begin();
            return tm.getTransaction();
        }
        catch( Exception e )
        {
            throw new EntityStoreException( "Failed to begin transaction.", e );
        }
    }

    void suspend()
    {
        try
        {
            Transaction txRunning = tm.getTransaction();
            if( txRunning != null && txRunning == transaction )
            {
                tm.suspend();
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( "Failed to suspend " +
                                            transaction, e );
        }
    }

    void resume()
    {
        try
        {
            tm.resume( transaction );
        }
        catch( Exception e )
        {
            throw new EntityStoreException( "Failed to resume " +
                                            transaction, e );
        }
    }

    EmbeddedGraphDatabase getNeo()
    {
        return neo;
    }

    IndexService getIndexService()
    {
        return indexService;
    }

    public String identity()
    {
        return identity;
    }

    private Node createEntityType( String type )
    {
        Node typeNode = neo.createNode();
        neo.getReferenceNode().createRelationshipTo( typeNode,
                                                     RelTypes.ENTITY_TYPE_REF );
        typeNode.setProperty( ENTITY_TYPE, type );
        indexService.index( typeNode, ENTITY_TYPE, type );
        return typeNode;
    }

    EntityDescriptor getEntityDescriptor( String type )
    {
        return ( (ModuleSPI) module ).entityDescriptor( type );
    }

    Module getModule()
    {
        return module;
    }
}