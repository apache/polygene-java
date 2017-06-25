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

import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.EntityStoreSPI;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.StateCommitter;
import org.apache.polygene.spi.serialization.JsonSerialization;

import static org.apache.polygene.api.entity.EntityReference.parseEntityReference;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore.
 *
 * <p>Implement {@link MapEntityStore} and add as mixin to the service using this mixin.</p>
 * <p>See {@link org.apache.polygene.entitystore.memory.MemoryMapEntityStoreMixin} for reference.</p>
 * <p>EntityStores based on this mixin gets support for the <b>Migration</b> extension.</p>
 * <p>MapEntityStore implementations will get their values as JSON.</p>
 */
public class MapEntityStoreMixin
    implements EntityStore, EntityStoreSPI, StateStore, MapEntityStoreActivation
{
    @This
    private MapEntityStore mapEntityStore;

    @This
    private EntityStoreSPI entityStoreSpi;

    @Structure
    private Application application;

    @Service
    private JsonSerialization jsonSerialization;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Optional
    @Service
    private Migration migration;

    @Service
    private IdentityGenerator identityGenerator;

    @Override
    public void activateMapEntityStore() {}

    // EntityStore
    @Override
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module, entityStoreSpi, newUnitOfWorkId(),
                                                 usecase, currentTime );
    }

    // EntityStoreSPI
    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork uow,
                                       EntityReference reference, EntityDescriptor entityDescriptor )
    {
        return new DefaultEntityState( uow.currentTime(), reference, entityDescriptor );
    }

    @Override
    public synchronized EntityState entityStateOf( EntityStoreUnitOfWork uow,
                                                   ModuleDescriptor module, EntityReference reference )
    {
        try
        {
            Reader in = mapEntityStore.get( reference );
            EntityState loadedState = readEntityState( module, in );
            if( loadedState.status() == EntityStatus.UPDATED )
            {
                List<EntityState> migrated = new ArrayList<>( 1 );
                migrated.add( loadedState );
                synchMigratedEntities( migrated );
            }
            return loadedState;
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
        try( JsonReader jsonReader = jsonFactories.readerFactory().createReader( mapEntityStore.get( reference ) ) )
        {
            return jsonReader.readObject().getString( JSONKeys.VERSION );
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
    public StateCommitter applyChanges( EntityStoreUnitOfWork uow, Iterable<EntityState> state )
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
                            for( EntityState entityState : state )
                            {
                                DefaultEntityState state1 = (DefaultEntityState) entityState;
                                String newVersion = uow.identity().toString();
                                Instant lastModified = uow.currentTime();
                                if( state1.status().equals( EntityStatus.NEW ) )
                                {
                                    try( Writer writer = changer.newEntity( state1.entityReference(),
                                                                            state1.entityDescriptor() ) )
                                    {
                                        writeEntityState( state1, writer, newVersion, lastModified );
                                    }
                                }
                                else if( state1.status().equals( EntityStatus.UPDATED ) )
                                {
                                    MapEntityStore.MapChange mapChange = new MapEntityStore.MapChange(
                                        state1.entityReference(), state1.entityDescriptor(),
                                        state1.version(), newVersion, lastModified
                                    );
                                    try( Writer writer = changer.updateEntity( mapChange ) )
                                    {
                                        writeEntityState( state1, writer, newVersion, lastModified );
                                    }
                                }
                                else if( state1.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state1.entityReference(), state1.entityDescriptor() );
                                }
                            }
                        } );
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
            List<EntityState> migrated = new ArrayList<>();
            String migrationErrorMsg = "Synchronization of Migrated Entities failed.";
            Function<Reader, EntityState> function = reader ->
            {
                EntityState entity = readEntityState( module, reader );
                if( entity.status() == EntityStatus.UPDATED )
                {
                    migrated.add( entity );
                    // Sync back 100 at a time
                    if( migrated.size() > 100 )
                    {
                        try
                        {
                            synchMigratedEntities( migrated );
                        }
                        catch( EntityStoreException ex )
                        {
                            throw ex;
                        }
                        catch( Exception ex )
                        {
                            throw new EntityStoreException( migrationErrorMsg, ex );
                        }
                    }
                }
                return entity;
            };
            Runnable closer = () ->
            {
                // Sync any remaining migrated entities
                if( !migrated.isEmpty() )
                {
                    try
                    {
                        synchMigratedEntities( migrated );
                    }
                    catch( EntityStoreException ex )
                    {
                        throw ex;
                    }
                    catch( Exception ex )
                    {
                        throw new EntityStoreException( migrationErrorMsg, ex );
                    }
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

    private void synchMigratedEntities( final List<EntityState> migratedEntities )
        throws Exception
    {
        mapEntityStore.applyChanges(
            changer ->
            {
                for( EntityState migratedEntity : migratedEntities )
                {
                    DefaultEntityState state = (DefaultEntityState) migratedEntity;
                    String version = state.version();
                    Instant lastModified = state.lastModified();
                    MapEntityStore.MapChange mapChange = new MapEntityStore.MapChange(
                        state.entityReference(), state.entityDescriptor(),
                        version, version, lastModified
                    );
                    try( Writer writer = changer.updateEntity( mapChange ) )
                    {
                        writeEntityState( state, writer, version, lastModified );
                    }
                }
            } );
        migratedEntities.clear();
    }

    protected Identity newUnitOfWorkId()
    {
        return identityGenerator.generate( EntityStore.class );
    }

    protected void writeEntityState( DefaultEntityState state, Writer writer, String version, Instant lastModified )
        throws EntityStoreException
    {
        try
        {
            JsonObjectBuilder json = jsonFactories.builderFactory().createObjectBuilder();
            json.add( JSONKeys.IDENTITY, state.entityReference().identity().toString() );
            json.add( JSONKeys.APPLICATION_VERSION, application.version() );
            json.add( JSONKeys.TYPE, state.entityDescriptor().primaryType().getName() );
            json.add( JSONKeys.VERSION, version );
            json.add( JSONKeys.MODIFIED, lastModified.toEpochMilli() );
            JsonObjectBuilder valueBuilder = jsonFactories.builderFactory().createObjectBuilder();
            EntityDescriptor entityType = state.entityDescriptor();
            entityType.state().properties().forEach(
                persistentProperty ->
                {
                    Object value = state.properties().get( persistentProperty.qualifiedName() );
                    JsonValue jsonValue = jsonSerialization.toJson( value );
                    valueBuilder.add( persistentProperty.qualifiedName().name(), jsonValue );
                } );

            for( Map.Entry<QualifiedName, EntityReference> entry : state.associations().entrySet() )
            {
                EntityReference value = entry.getValue();
                valueBuilder.add( entry.getKey().name(), value == null ? null : value.identity().toString() );
            }

            for( Map.Entry<QualifiedName, List<EntityReference>> entry : state.manyAssociations().entrySet() )
            {
                JsonArrayBuilder arrayBuilder = jsonFactories.builderFactory().createArrayBuilder();
                for( EntityReference entityReference : entry.getValue() )
                {
                    arrayBuilder.add( entityReference.identity().toString() );
                }
                valueBuilder.add( entry.getKey().name(), arrayBuilder.build() );
            }

            for( Map.Entry<QualifiedName, Map<String, EntityReference>> entry : state.namedAssociations().entrySet() )
            {
                JsonObjectBuilder objectBuilder = jsonFactories.builderFactory().createObjectBuilder();
                for( Map.Entry<String, EntityReference> namedRef : entry.getValue().entrySet() )
                {
                    objectBuilder.add( namedRef.getKey(), namedRef.getValue().identity().toString() );
                }
                valueBuilder.add( entry.getKey().name(), objectBuilder.build() );
            }
            json.add( JSONKeys.VALUE, valueBuilder.build() );
            JsonObject jsonState = json.build();
            try( JsonWriter jsonWriter = jsonFactories.writerFactory().createWriter( writer ) )
            {
                jsonWriter.write( jsonState );
            }
        }
        catch( Exception e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected EntityState readEntityState( ModuleDescriptor module, Reader entityState )
        throws EntityStoreException
    {
        try( JsonReader jsonReader = jsonFactories.readerFactory().createReader( entityState ) )
        {
            JsonObject parsedState = jsonReader.readObject();
            JsonObjectBuilder jsonStateBuilder = jsonFactories.cloneBuilder( parsedState );
            final EntityStatus[] status = { EntityStatus.LOADED };

            String version = parsedState.getString( JSONKeys.VERSION );
            Instant modified = Instant.ofEpochMilli( parsedState.getJsonNumber( JSONKeys.MODIFIED ).longValueExact() );
            Identity identity = StringIdentity.identityOf( parsedState.getString( JSONKeys.IDENTITY ) );

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
                status[ 0 ] = EntityStatus.UPDATED;
            }

            String type = state.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new NoSuchEntityTypeException( type, module );
            }

            Map<QualifiedName, Object> properties = new HashMap<>();
            JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
            entityDescriptor.state().properties().forEach(
                property ->
                {
                    try
                    {
                        JsonValue jsonValue = valueState.get( property.qualifiedName().name() );
                        Object value = jsonSerialization.fromJson( module, property.valueType(), jsonValue );
                        properties.put( property.qualifiedName(), value );
                    }
                    catch( JsonException e )
                    {
                        // Value not found, default it
                        Object initialValue = property.resolveInitialValue( module );
                        properties.put( property.qualifiedName(), initialValue );
                        status[ 0 ] = EntityStatus.UPDATED;
                    }
                } );

            Map<QualifiedName, EntityReference> associations = new HashMap<>();
            entityDescriptor.state().associations().forEach(
                association ->
                {
                    try
                    {
                        String jsonValue = valueState.getString( association.qualifiedName().name(), null );
                        EntityReference value = jsonValue == null
                                                ? null
                                                : parseEntityReference( jsonValue );
                        associations.put( association.qualifiedName(), value );
                    }
                    catch( JsonException e )
                    {
                        // Association not found, default it to null
                        associations.put( association.qualifiedName(), null );
                        status[ 0 ] = EntityStatus.UPDATED;
                    }
                } );

            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<>();
            entityDescriptor.state().manyAssociations().forEach(
                association ->
                {
                    List<EntityReference> references = new ArrayList<>();
                    try
                    {
                        JsonArray jsonValues = valueState.getJsonArray( association.qualifiedName().name() );
                        for( int i = 0; i < jsonValues.size(); i++ )
                        {
                            String jsonValue = jsonValues.getString( i, null );
                            EntityReference value = jsonValue == null
                                                    ? null
                                                    : parseEntityReference( jsonValue );
                            references.add( value );
                        }
                        manyAssociations.put( association.qualifiedName(), references );
                    }
                    catch( JsonException e )
                    {
                        // ManyAssociation not found, default to empty one
                        manyAssociations.put( association.qualifiedName(), references );
                    }
                } );

            Map<QualifiedName, Map<String, EntityReference>> namedAssociations = new HashMap<>();
            entityDescriptor.state().namedAssociations().forEach(
                association ->
                {
                    Map<String, EntityReference> references = new LinkedHashMap<>();
                    try
                    {
                        JsonObject jsonValues = valueState.getJsonObject( association.qualifiedName().name() );
                        for( String name : jsonValues.keySet() )
                        {
                            String value = jsonValues.getString( name, null );
                            EntityReference ref = value == null
                                                  ? null
                                                  : parseEntityReference( value );
                            references.put( name, ref );
                        }
                        namedAssociations.put( association.qualifiedName(), references );
                    }
                    catch( JsonException e )
                    {
                        // NamedAssociation not found, default to empty one
                        namedAssociations.put( association.qualifiedName(), references );
                    }
                } );

            return new DefaultEntityState( version,
                                           modified,
                                           EntityReference.create( identity ),
                                           status[ 0 ],
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations,
                                           namedAssociations
            );
        }
        catch( Exception e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public JsonObject jsonStateOf( String id )
    {
        try( JsonReader reader = jsonFactories.readerFactory()
                                              .createReader( mapEntityStore.get( parseEntityReference( id ) ) ) )
        {
            return reader.readObject();
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
}
