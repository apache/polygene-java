package org.qi4j.entitystore.qrm;

import java.util.UUID;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * User: alex
 */
public class QrmEntityStoreServiceMixin
    implements Activatable, EntityStore, EntityStoreSPI, IdentityGenerator
{

    @Uses
    ServiceDescriptor descriptor;

    @Service
    QrmMapper mapper;

    @This
    EntityStoreSPI entityStoreSpi;

    @Structure
    ModuleSPI module;

    protected String uowUUID;

    private int uowCount;

    public void activate()
        throws Exception
    {
        QrmEntityStoreDescriptor cfg = descriptor.metaInfo( QrmEntityStoreDescriptor.class );

        if( cfg == null )
        {
            throw new RuntimeException( "QRM EntityStore expects configuration provided to it." );
        }

        mapper.bootstrap( cfg );

        uowUUID = UUID.randomUUID().toString() + "-";

        uowCount = 1;
    }

    public void passivate()
        throws Exception
    {
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        System.err.println( "EntityState newEntityState ... was called." );

        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor );
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module )
    {
        System.err.println( "EntityStoreUnitOfWork newUnitOfWork ... was called." );

        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, Module module )
    {
        System.err.println( "visit entity states called." );

        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );

        return uow;
    }

    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        String strIdentity = identity.identity();

        Class mappedClazz = null;
        try
        {
            mappedClazz = Class.forName( strIdentity.substring( 0, strIdentity.indexOf( ":" ) ) );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityTypeNotFoundException( identity.identity() );
        }

        EntityState result = mapper.get( (DefaultEntityStoreUnitOfWork) unitOfWork, mappedClazz, identity );

        return result;
    }

    public StateCommitter apply( final Iterable<EntityState> entityStates, final String version )
    {
        return new StateCommitter()
        {
            public void commit()
            {
                for( EntityState entityState : entityStates )
                {
                    DefaultEntityState state = (DefaultEntityState) entityState;

                    String strIdentity = state.identity().identity();

                    Class mappedClazz = null;
                    try
                    {
                        mappedClazz = Class.forName( strIdentity.substring( 0, strIdentity.indexOf( ":" ) ) );
                    }
                    catch( ClassNotFoundException e )
                    {
                        throw new EntityTypeNotFoundException( strIdentity );
                    }

                    if( state.status() == EntityStatus.NEW )
                    {
                        mapper.newEntity( mappedClazz, state, version );
                    }
                    else if( state.status() == EntityStatus.REMOVED )
                    {
                        mapper.delEntity( mappedClazz, state, version );
                    }
                    else if( state.status() == EntityStatus.UPDATED )
                    {
                        mapper.updEntity( mappedClazz, state, version );
                    }
                }
            }

            public void cancel()
            {
            }
        };
    }

    public String generate( Class<? extends Identity> compositeType )
    {
        System.err.println( "generating for: " + compositeType );

        EntityDescriptor eDesc = module.entityDescriptor( compositeType.getName() );

        Class mappedClass = mapper.findMappedMixin( eDesc );

        String mapperNextId = mapper.fetchNextId( mappedClass );

        return mappedClass.getName() + ":" + mapperNextId;
    }

    protected String newUnitOfWorkId()
    {
        return uowUUID + Integer.toHexString( uowCount++ );
    }
}
