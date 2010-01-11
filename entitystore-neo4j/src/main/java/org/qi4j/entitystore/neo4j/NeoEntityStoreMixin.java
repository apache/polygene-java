package org.qi4j.entitystore.neo4j;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.util.index.IndexService;
import org.neo4j.util.index.LuceneIndexService;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;

public class NeoEntityStoreMixin
    implements Activatable, EntityStore, EntityStoreSPI
{
    @This
    private Configuration<NeoConfiguration> config;

    private EmbeddedNeo neo;
    private IndexService indexService;

    private AtomicInteger count = new AtomicInteger( 0 );
    private String uuid;

    public void activate()
        throws Exception
    {
        String path = config.configuration().path().get();
        if( path == null )
        {
            path = "target/neodb";
        }
        neo = new EmbeddedNeo( path );
        indexService = new LuceneIndexService( neo );
        uuid = UUID.randomUUID().toString() + "-";
    }

    public void passivate()
        throws Exception
    {
        indexService.shutdown();
        neo.shutdown();
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module )
    {
        return new NeoEntityStoreUnitOfWork( neo, indexService, newUnitOfWorkId(), module );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, Module module )
    {
        NeoEntityStoreUnitOfWork uow = new NeoEntityStoreUnitOfWork( neo, indexService, newUnitOfWorkId(), module );

        Iterable<Relationship> relationships =
            neo.getReferenceNode().getRelationships( RelTypes.ENTITY_TYPE_REF, Direction.OUTGOING );

        for( Relationship entityTypeRel : relationships )
        {
            Node entityType = entityTypeRel.getEndNode();
            for( Relationship entityRel : entityType.getRelationships( RelTypes.IS_OF_TYPE, Direction.INCOMING ) )
            {
                Node entityNode = entityRel.getStartNode();
                NeoEntityState entityState = new NeoEntityState( uow, entityNode, EntityStatus.LOADED );
                visitor.visitEntityState( entityState );
            }
        }
        return uow;
    }

    public StateCommitter apply( Iterable<EntityState> state, String version )
    {
        for( EntityState firstState : state )
        {
            if( firstState instanceof NeoEntityState )
            {
                return ( (NeoEntityState) firstState ).getUnitOfWork().apply();
            }
        }
        return null;
    }

    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        return unitOfWork.getEntityState( identity );
    }

    public EntityState newEntityState( EntityStoreUnitOfWork uow, EntityReference ref, EntityDescriptor descriptor )
    {
        return uow.newEntityState( ref, descriptor );
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count.incrementAndGet() );
    }
}