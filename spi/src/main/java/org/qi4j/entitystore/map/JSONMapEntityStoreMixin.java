package org.qi4j.entitystore.map;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.JSONEntityState;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore. Implement
 * MapEntityStore and add as mixin to the service using this mixin.
 * See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.
 */
public class JSONMapEntityStoreMixin
    implements EntityStore, EntityStoreSPI, StateStore, Activatable
{
    @This
    private MapEntityStore mapEntityStore;
    @This
    private EntityStoreSPI entityStoreSpi;

    @Structure
    private Application application;

    @Optional
    @Service
    private Migration migration;

    protected String uuid;
    private int count;

    public void activate()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
    }

    public void passivate()
        throws Exception
    {
    }

    // EntityStore

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, Module module )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }

    // EntityStoreSPI

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        try
        {
            JSONObject state = new JSONObject();
            state.put( "identity", identity.identity() );
            state.put( "application_version", application.version() );
            state.put( "type", entityDescriptor.entityType().type().name() );
            state.put( "version", unitOfWork.identity() );
            state.put( "modified", System.currentTimeMillis() );
            state.put( "properties", new JSONObject() );
            state.put( "associations", new JSONObject() );
            state.put( "manyassociations", new JSONObject() );

            return new JSONEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor, state );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public synchronized EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        // Get state
        Reader in = mapEntityStore.get( identity );

        return readEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, in );
    }

    public StateCommitter apply( final Iterable<EntityState> state, final String version )
        throws EntityStoreException
    {
        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
                    {
                        public void visitMap( MapEntityStore.MapChanger changer )
                            throws IOException
                        {
                            for( EntityState entityState : state )
                            {
                                JSONEntityState state = (JSONEntityState) entityState;
                                if( state.status().equals( EntityStatus.NEW ) )
                                {
                                    Writer writer = changer.newEntity( state.identity(), state.entityDescriptor().entityType() );
                                    writeEntityState( state, writer, version );
                                    writer.close();
                                }
                                else if( state.status().equals( EntityStatus.UPDATED ) )
                                {
                                    Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor().entityType() );
                                    writeEntityState( state, writer, version );
                                    writer.close();
                                }
                                else if( state.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state.identity(), state.entityDescriptor().entityType() );
                                }
                            }
                        }
                    } );
                }
                catch( IOException e )
                {
                    throw new EntityStoreException( e );
                }
            }

            public void cancel()
            {
            }
        };
    }

    public EntityStoreUnitOfWork visitEntityStates( final EntityStateVisitor visitor, Module moduleInstance )
    {
        // TODO This can be used for reading state, but not for modifying (e.g. removing all entities)
        final DefaultEntityStoreUnitOfWork uow =
            new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), moduleInstance );

        mapEntityStore.visitMap( new MapEntityStore.MapEntityStoreVisitor()
        {
            public void visitEntity( Reader entityState )
            {
                try
                {
                    EntityState entity = readEntityState( uow, entityState );
                    visitor.visitEntityState( entity );
                    uow.registerEntityState( entity );
                }
                catch( Exception e )
                {
                    Logger.getLogger( getClass().getName() ).log( Level.SEVERE, "visitEntityStates", e );
                }
            }
        } );

        return uow;
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    protected void writeEntityState( JSONEntityState state, Writer writer, String identity )
        throws EntityStoreException
    {
        try
        {
            JSONObject jsonState = state.state();
            jsonState.put( "version", identity );
            writer.append( jsonState.toString() );
        }
        catch( Exception e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected EntityState readEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            ModuleSPI module = (ModuleSPI) unitOfWork.module();
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            EntityStatus status = EntityStatus.LOADED;

            String version = jsonObject.getString( "version" );
            long modified = jsonObject.getLong( "modified" );
            String identity = jsonObject.getString( "identity" );

            // Check if version is correct
            String currentAppVersion = jsonObject.optString( MapEntityStore.JSONKeys.application_version.name(), "0.0" );
            if( !currentAppVersion.equals( application.version() ) )
            {
                if( migration != null )
                {
                    migration.migrate( jsonObject, application.version(), this );
                }
                else
                {
                    // Do nothing - set version to be correct
                    jsonObject.put( MapEntityStore.JSONKeys.application_version.name(), application.version() );
                }

                Logger.getLogger( getClass().getName() )
                    .info( "Updated version nr on " + identity + " from " + currentAppVersion + " to " + application.version() );

                // State changed
                status = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( "type" );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type );
            }

            return new JSONEntityState( unitOfWork,
                                        version,
                                        modified,
                                        EntityReference.parseEntityReference( identity ),
                                        status,
                                        entityDescriptor,
                                        jsonObject
            );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public JSONObject getState( String id )
        throws IOException
    {
        Reader reader = mapEntityStore.get( EntityReference.parseEntityReference( id ) );
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject( new JSONTokener( reader ) );
        }
        catch( JSONException e )
        {
            throw (IOException) new IOException().initCause( e );
        }
        reader.close();
        return jsonObject;
    }
}