/*
 * Copyright 2007-2011, Niclas Hedhman. All Rights Reserved.
 * Copyright 2009-2013, Rickard Öberg. All Rights Reserved.
 * Copyright 2012-2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.spi.entitystore.helpers;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.apache.zest.api.cache.CacheOptions;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.spi.Qi4jSPI;
import org.apache.zest.spi.cache.Cache;
import org.apache.zest.spi.cache.CachePool;
import org.apache.zest.spi.cache.NullCache;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreSPI;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.ModuleEntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateCommitter;
import org.apache.zest.spi.module.ModelModule;
import org.apache.zest.spi.module.ModuleSpi;

import static org.apache.zest.functional.Iterables.first;
import static org.apache.zest.functional.Iterables.map;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore.
 *
 * <p>Implement {@link MapEntityStore} and add as mixin to the service using this mixin.</p>
 * <p>See {@link org.apache.zest.entitystore.memory.MemoryMapEntityStoreMixin} for reference.</p>
 * <p>EntityStores based on this mixin gets support for the <b>Migration</b> and <b>Cache</b> extensions.</p>
 * <p>MapEntityStore implementations will get their values as JSON.</p>
 */
public class JSONMapEntityStoreMixin
    implements EntityStore, EntityStoreSPI, StateStore, JSONMapEntityStoreActivation
{
    @This
    private MapEntityStore mapEntityStore;

    @This
    private EntityStoreSPI entityStoreSpi;

    @Structure
    private Qi4jSPI spi;

    @Structure
    private Application application;

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    @Optional
    @Service
    private Migration migration;

    @Uses
    private ServiceDescriptor descriptor;

    @Optional
    @Service
    private CachePool caching;
    private Cache<CacheState> cache;

    protected String uuid;
    private int count;

    public JSONMapEntityStoreMixin()
    {
    }

    @Override
    public void setUpJSONMapES()
        throws Exception
    {
        uuid = descriptor.identity() + "-" + UUID.randomUUID().toString();
        if( caching != null )
        {
            cache = caching.fetchCache( uuid, CacheState.class );
        }
        else
        {
            cache = new NullCache<>();
        }
    }

    @Override
    public void tearDownJSONMapES()
        throws Exception
    {
        if( caching != null )
        {
            caching.returnCache( cache );
            cache = null;
        }
    }

    // EntityStore

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, ModuleSpi module, long currentTime )
    {
        EntityStoreUnitOfWork storeUnitOfWork = new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), usecaseMetaInfo, currentTime );
        storeUnitOfWork = new ModuleEntityStoreUnitOfWork( module, storeUnitOfWork );
        return storeUnitOfWork;
    }

    // EntityStoreSPI

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       ModuleSpi module,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        try
        {
            JSONObject state = new JSONObject();
            state.put( JSONKeys.IDENTITY, identity.identity() );
            state.put( JSONKeys.APPLICATION_VERSION, application.version() );
            state.put( JSONKeys.TYPE, first( entityDescriptor.types() ).getName() );
            state.put( JSONKeys.VERSION, unitOfWork.identity() );
            state.put( JSONKeys.MODIFIED, unitOfWork.currentTime() );
            state.put( JSONKeys.PROPERTIES, new JSONObject() );
            state.put( JSONKeys.ASSOCIATIONS, new JSONObject() );
            state.put( JSONKeys.MANY_ASSOCIATIONS, new JSONObject() );
            state.put( JSONKeys.NAMED_ASSOCIATIONS, new JSONObject() );
            return new JSONEntityState( unitOfWork.currentTime(), valueSerialization,
                                        identity, entityDescriptor, state );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public synchronized EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork,
                                                   ModuleSpi module,
                                                   EntityReference identity
    )
    {
        EntityState state = fetchCachedState( identity, module, unitOfWork.currentTime() );
        if( state != null )
        {
            return state;
        }
        // Get state
        Reader in = mapEntityStore.get( identity );
        JSONEntityState loadedState = readEntityState( module, in );
        if( doCacheOnRead( unitOfWork ) )
        {
            cache.put( identity.identity(), new CacheState( loadedState.state() ) );
        }
        return loadedState;
    }

    @Override
    public StateCommitter applyChanges( final EntityStoreUnitOfWork unitOfWork,
                                        final Iterable<EntityState> state
    )
        throws EntityStoreException
    {
        return new StateCommitter()
        {
            @Override
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
                    {
                        @Override
                        public void visitMap( MapEntityStore.MapChanger changer )
                            throws IOException
                        {
                            CacheOptions options = unitOfWork.usecase().metaInfo( CacheOptions.class );
                            if( options == null )
                            {
                                options = CacheOptions.ALWAYS;
                            }

                            for( EntityState entityState : state )
                            {
                                JSONEntityState state = (JSONEntityState) entityState;
                                if( state.status().equals( EntityStatus.NEW ) )
                                {
                                    try (Writer writer = changer.newEntity( state.identity(), state.entityDescriptor() ))
                                    {
                                        writeEntityState( state, writer, unitOfWork.identity(), unitOfWork.currentTime() );
                                    }
                                    if( options.cacheOnNew() )
                                    {
                                        cache.put( state.identity().identity(), new CacheState( state.state() ) );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.UPDATED ) )
                                {
                                    try (Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor() ))
                                    {
                                        writeEntityState( state, writer, unitOfWork.identity(), unitOfWork.currentTime() );
                                    }
                                    if( options.cacheOnWrite() )
                                    {
                                        cache.put( state.identity().identity(), new CacheState( state.state() ) );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state.identity(), state.entityDescriptor() );
                                    cache.remove( state.identity().identity() );
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

            @Override
            public void cancel()
            {
            }
        };
    }

    @Override
    public Input<EntityState, EntityStoreException> entityStates( final ModuleSpi module )
    {
        return new Input<EntityState, EntityStoreException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super EntityState, ReceiverThrowableType> output )
                throws EntityStoreException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<EntityState, EntityStoreException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super EntityState, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, EntityStoreException
                    {
                        final List<EntityState> migrated = new ArrayList<>();
                        try
                        {
                            mapEntityStore.entityStates().transferTo( new Output<Reader, ReceiverThrowableType>()
                            {
                                @Override
                                public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Reader, SenderThrowableType> sender )
                                    throws ReceiverThrowableType, SenderThrowableType
                                {
                                    sender.sendTo( new Receiver<Reader, ReceiverThrowableType>()
                                    {
                                        @Override
                                        public void receive( Reader item )
                                            throws ReceiverThrowableType
                                        {
                                            final EntityState entity = readEntityState( module, item );
                                            if( entity.status() == EntityStatus.UPDATED )
                                            {
                                                migrated.add( entity );

                                                // Synch back 100 at a time
                                                if( migrated.size() > 100 )
                                                {
                                                    try
                                                    {
                                                        synchMigratedEntities( migrated );
                                                    }
                                                    catch( IOException e )
                                                    {
                                                        throw new EntityStoreException( "Synchronization of Migrated Entities failed.", e );
                                                    }
                                                }
                                            }
                                            receiver.receive( entity );
                                        }
                                    } );

                                    // Synch any remaining migrated entities
                                    if( !migrated.isEmpty() )
                                    {
                                        try
                                        {
                                            synchMigratedEntities( migrated );
                                        }
                                        catch( IOException e )
                                        {
                                            throw new EntityStoreException( "Synchronization of Migrated Entities failed.", e );
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
                } );
            }
        };
    }

    private void synchMigratedEntities( final List<EntityState> migratedEntities )
        throws IOException
    {
        mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
        {
            @Override
            public void visitMap( MapEntityStore.MapChanger changer )
                throws IOException
            {
                for( EntityState migratedEntity : migratedEntities )
                {
                    JSONEntityState state = (JSONEntityState) migratedEntity;
                    try (Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor() ))
                    {
                        writeEntityState( state, writer, state.version(), state.lastModified() );
                    }
                }
            }
        } );
        migratedEntities.clear();
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    protected void writeEntityState( JSONEntityState state, Writer writer, String identity, long lastModified )
        throws EntityStoreException
    {
        try
        {
            JSONObject jsonState = state.state();
            jsonState.put( JSONKeys.VERSION, identity );
            jsonState.put( JSONKeys.MODIFIED, lastModified );
            writer.append( jsonState.toString() );
        }
        catch( JSONException | IOException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected JSONEntityState readEntityState( ModuleSpi module, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            EntityStatus status = EntityStatus.LOADED;

            String version = jsonObject.getString( JSONKeys.VERSION );
            long modified = jsonObject.getLong( JSONKeys.MODIFIED );
            String identity = jsonObject.getString( JSONKeys.IDENTITY );

            // Check if NamedAssociation is supported
            if( !jsonObject.has( JSONKeys.NAMED_ASSOCIATIONS ) )
            {
                jsonObject.put( JSONKeys.NAMED_ASSOCIATIONS, new JSONObject() );
            }

            // Check if version is correct
            String currentAppVersion = jsonObject.optString( JSONKeys.APPLICATION_VERSION, "0.0" );
            if( !currentAppVersion.equals( application.version() ) )
            {
                if( migration != null )
                {
                    migration.migrate( jsonObject, application.version(), this );
                }
                else
                {
                    // Do nothing - set version to be correct
                    jsonObject.put( JSONKeys.APPLICATION_VERSION, application.version() );
                }
                // State changed
                status = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type,
                                                       module.name(),
                                                       map( ModelModule.toStringFunction,
                                                            module.findVisibleEntityTypes()
                                                       ) );
            }

            return new JSONEntityState( valueSerialization,
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

    @Override
    public JSONObject jsonStateOf( String id )
        throws IOException
    {
        try (Reader reader = mapEntityStore.get( EntityReference.parseEntityReference( id ) ))
        {
            return new JSONObject( new JSONTokener( reader ) );
        }
        catch( JSONException e )
        {
            throw new IOException( e );
        }
    }

    private EntityState fetchCachedState( EntityReference identity, ModuleSpi module, long currentTime )
    {
        CacheState cacheState = cache.get( identity.identity() );
        if( cacheState != null )
        {
            JSONObject data = cacheState.json;
            try
            {
                String type = data.getString( JSONKeys.TYPE );
                EntityDescriptor entityDescriptor = module.entityDescriptor( type );
//                return new JSONEntityState( currentTime, valueSerialization, identity, entityDescriptor, data );
                return new JSONEntityState( valueSerialization, data.getString( JSONKeys.VERSION ), data.getLong( JSONKeys.MODIFIED ), identity, EntityStatus.LOADED, entityDescriptor, data );
            }
            catch( JSONException e )
            {
                // Should not be able to happen, unless internal error in the cache system.
                throw new EntityStoreException( e );
            }
        }
        return null;
    }

    private boolean doCacheOnRead( EntityStoreUnitOfWork unitOfWork )
    {
        CacheOptions cacheOptions = unitOfWork.usecase().metaInfo( CacheOptions.class );
        return cacheOptions == null || cacheOptions.cacheOnRead();
    }

    public static class CacheState
        implements Externalizable
    {
        public JSONObject json;

        public CacheState()
        {
        }

        private CacheState( JSONObject state )
        {
            json = state;
        }

        @Override
        public void writeExternal( ObjectOutput out )
            throws IOException
        {
            out.writeUTF( json.toString() );
        }

        @Override
        public void readExternal( ObjectInput in )
            throws IOException, ClassNotFoundException
        {
            try
            {
                json = new JSONObject( in.readUTF() );
            }
            catch( JSONException e )
            {
                throw new IOException( e );
            }
        }
    }
}
