/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.spi.entitystore.helpers;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.polygene.api.cache.CacheOptions;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.cache.Cache;
import org.apache.polygene.spi.cache.CachePool;
import org.apache.polygene.spi.cache.NullCache;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.EntityStoreSPI;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.StateCommitter;
import org.apache.polygene.spi.serialization.JsonSerialization;

import static java.util.stream.Collectors.joining;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore.
 *
 * <p>Implement {@link MapEntityStore} and add as mixin to the service using this mixin.</p>
 * <p>See {@link org.apache.polygene.entitystore.memory.MemoryMapEntityStoreMixin} for reference.</p>
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
    private Application application;

    @Service
    private JsonSerialization serialization;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Service
    private IdentityGenerator identityGenerator;

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
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module, entityStoreSpi, newUnitOfWorkId(), usecase, currentTime );
    }

    // EntityStoreSPI

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork uow,
                                       EntityReference reference,
                                       EntityDescriptor entityDescriptor
    )
    {
        try
        {
            JsonObjectBuilder builder = jsonFactories.builderFactory().createObjectBuilder();
            builder.add( JSONKeys.IDENTITY, reference.identity().toString() );
            builder.add( JSONKeys.APPLICATION_VERSION, application.version() );
            builder.add( JSONKeys.TYPE, entityDescriptor.types().findFirst().get().getName() );
            builder.add( JSONKeys.VERSION, uow.identity().toString() );
            builder.add( JSONKeys.MODIFIED, uow.currentTime().toEpochMilli() );
            builder.add( JSONKeys.VALUE, jsonFactories.builderFactory().createObjectBuilder().build() );
            JsonObject state = builder.build();
            return new JSONEntityState( entityDescriptor.module(), serialization, jsonFactories,
                                        uow.identity().toString(), uow.currentTime(),
                                        reference,
                                        EntityStatus.NEW, entityDescriptor,
                                        state );
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public synchronized EntityState entityStateOf( EntityStoreUnitOfWork uow,
                                                   ModuleDescriptor module,
                                                   EntityReference reference )
    {
        try
        {
            EntityState state = fetchCachedState( reference, module, uow.currentTime() );
            if( state != null )
            {
                return state;
            }
            // Get state
            try( Reader in = mapEntityStore.get( reference ) )
            {
                JSONEntityState loadedState = readEntityState( module, in );
                if( loadedState.status() == EntityStatus.UPDATED )
                {
                    List<JSONEntityState> migrated = new ArrayList<>( 1 );
                    migrated.add( loadedState );
                    synchMigratedEntities( migrated );
                }
                if( doCacheOnRead( uow ) )
                {
                    cache.put( reference.identity().toString(), new CacheState( loadedState.state().toString() ) );
                }
                return loadedState;
            }
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public synchronized String versionOf( EntityStoreUnitOfWork uow, EntityReference reference )
    {
        CacheState cacheState = cache.get( reference.identity().toString() );
        if( cacheState != null )
        {
            return jsonFactories.readerFactory().createReader( new StringReader( cacheState.string ) ).readObject()
                                .getString( JSONKeys.VERSION );
        }
        // Get state
        try( JsonReader reader = jsonFactories.readerFactory().createReader( mapEntityStore.get( reference ) ) )
        {
            return reader.readObject().getString( JSONKeys.VERSION );
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public StateCommitter applyChanges( EntityStoreUnitOfWork uow, Iterable<EntityState> entityStates )
        throws EntityStoreException
    {
        return new StateCommitter()
        {
            @Override
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges(
                        changer ->
                        {
                            CacheOptions options = uow.usecase().metaInfo( CacheOptions.class );
                            if( options == null )
                            {
                                options = CacheOptions.ALWAYS;
                            }

                            for( EntityState entityState : entityStates )
                            {
                                JSONEntityState state = (JSONEntityState) entityState;
                                String newVersion = uow.identity().toString();
                                Instant lastModified = uow.currentTime();
                                if( state.status().equals( EntityStatus.NEW ) )
                                {
                                    try( Writer writer = changer.newEntity( state.entityReference(),
                                                                            state.entityDescriptor() ) )
                                    {
                                        writeEntityState( state, writer, newVersion, lastModified );
                                    }
                                    if( options.cacheOnNew() )
                                    {
                                        cache.put( state.entityReference().identity().toString(),
                                                   new CacheState( state.state().toString() ) );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.UPDATED ) )
                                {
                                    MapEntityStore.MapChange mapChange = new MapEntityStore.MapChange(
                                        state.entityReference(), state.entityDescriptor(),
                                        state.version(), newVersion, lastModified
                                    );
                                    try( Writer writer = changer.updateEntity( mapChange ) )
                                    {
                                        writeEntityState( state, writer, newVersion, lastModified );
                                    }
                                    if( options.cacheOnWrite() )
                                    {
                                        cache.put( state.entityReference().identity().toString(),
                                                   new CacheState( state.state().toString() ) );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state.entityReference(), state.entityDescriptor() );
                                    cache.remove( state.entityReference().identity().toString() );
                                }
                            }
                        } );
                }
                catch( Exception e )
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
    public Stream<EntityState> entityStates( ModuleDescriptor module )
    {
        try
        {
            Stream<Reader> stateStream = mapEntityStore.entityStates();
            List<JSONEntityState> migrated = new ArrayList<>();
            Function<Reader, EntityState> function = reader ->
            {
                JSONEntityState entity = readEntityState( module, reader );
                if( entity.status() == EntityStatus.UPDATED )
                {
                    migrated.add( entity );
                    // Synch back 100 at a time
                    if( migrated.size() > 100 )
                    {
                        synchMigratedEntities( migrated );
                    }
                }
                return entity;
            };
            Runnable closer = () ->
            {
                // Synch any remaining migrated entities
                if( !migrated.isEmpty() )
                {
                    synchMigratedEntities( migrated );
                }
            };
            return stateStream.map( function ).onClose( closer );
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    private void synchMigratedEntities( List<JSONEntityState> migratedEntities )
    {
        try
        {
            mapEntityStore.applyChanges(
                changer ->
                {
                    for( JSONEntityState state : migratedEntities )
                    {
                        Instant lastModified = state.lastModified();
                        String version = state.version();
                        MapEntityStore.MapChange changeInfo = new MapEntityStore.MapChange(
                            state.entityReference(), state.entityDescriptor(),
                            version, version, lastModified
                        );
                        try( Writer writer = changer.updateEntity( changeInfo ) )
                        {
                            writeEntityState( state, writer, version, lastModified );
                        }
                    }
                } );
            migratedEntities.clear();
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( "Synchronization of Migrated Entities failed.", ex );
        }
    }

    protected Identity newUnitOfWorkId()
    {
        return identityGenerator.generate( EntityStore.class );
    }

    protected void writeEntityState( JSONEntityState state, Writer writer, String version, Instant lastModified )
        throws EntityStoreException
    {
        try
        {
            state.stateCloneWithVersionAndModified( version, lastModified );
            writer.append( state.state().toString() );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected JSONEntityState readEntityState( ModuleDescriptor module, Reader entityState )
        throws EntityStoreException
    {
        try( JsonReader reader = jsonFactories.readerFactory().createReader( entityState ) )
        {
            JsonObject parsedState = reader.readObject();
            JsonObjectBuilder jsonStateBuilder = jsonFactories.cloneBuilder( parsedState );
            EntityStatus status = EntityStatus.LOADED;

            String version = parsedState.getString( JSONKeys.VERSION );
            Instant modified = Instant.ofEpochMilli( parsedState.getJsonNumber( JSONKeys.MODIFIED ).longValueExact() );
            Identity identity = StringIdentity.fromString( parsedState.getString( JSONKeys.IDENTITY ) );

            // Check if version is correct
            JsonObject state;
            String currentAppVersion = parsedState.getString( JSONKeys.APPLICATION_VERSION, "0.0" );
            if( currentAppVersion.equals( application.version() ) )
            {
                state = jsonStateBuilder.build();
            }
            else
            {
                if( migration != null )
                {
                    state = migration.migrate( jsonStateBuilder.build(), application.version(), this );
                }
                else
                {
                    // Do nothing - set version to be correct
                    jsonStateBuilder.add( JSONKeys.APPLICATION_VERSION, application.version() );
                    state = jsonStateBuilder.build();
                }
                // State changed
                status = EntityStatus.UPDATED;
            }

            String type = state.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new NoSuchEntityTypeException( type, module.name(), module.typeLookup() );
            }

            return new JSONEntityState( entityDescriptor.module(), serialization, jsonFactories,
                                        version, modified,
                                        EntityReference.create( identity ),
                                        status, entityDescriptor,
                                        state
            );
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public JsonObject jsonStateOf( String id )
    {
        try( JsonReader jsonReader = jsonFactories
            .readerFactory().createReader( mapEntityStore.get( EntityReference.parseEntityReference( id ) ) ) )
        {
            return jsonReader.readObject();
        }
        catch( EntityStoreException ex )
        {
            throw ex;
        }
        catch( Exception ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    private EntityState fetchCachedState( EntityReference reference, ModuleDescriptor module, Instant currentTime )
    {
        CacheState cacheState = cache.get( reference.identity().toString() );
        if( cacheState != null )
        {
            JsonObject state = jsonFactories.readerFactory().createReader( new StringReader( cacheState.string ) )
                                            .readObject();
            try
            {
                String type = state.getString( JSONKeys.TYPE );
                EntityDescriptor entityDescriptor = module.entityDescriptor( type );
                String version = state.getString( JSONKeys.VERSION );
                Instant modified = Instant.ofEpochMilli( state.getJsonNumber( JSONKeys.MODIFIED ).longValueExact() );
                return new JSONEntityState( entityDescriptor.module(), serialization, jsonFactories,
                                            version, modified,
                                            reference,
                                            EntityStatus.LOADED, entityDescriptor,
                                            state );
            }
            catch( Exception e )
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
        public String string;

        public CacheState()
        {
        }

        private CacheState( String string )
        {
            this.string = string;
        }

        @Override
        public void writeExternal( ObjectOutput out )
            throws IOException
        {
            out.writeUTF( string );
        }

        @Override
        public void readExternal( ObjectInput in )
            throws IOException, ClassNotFoundException
        {
            try( BufferedReader reader = new BufferedReader( new StringReader( in.readUTF() ) ) )
            {
                string = reader.lines().collect( joining( "\n" ) );
            }
        }
    }
}
