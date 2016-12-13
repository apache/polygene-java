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
 *
 *
 */
package org.apache.zest.entitystore.sql;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.IdentityGenerator;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.unitofwork.NoSuchEntityTypeException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.entitystore.sql.internal.DatabaseSQLService;
import org.apache.zest.entitystore.sql.internal.DatabaseSQLService.EntityValueResult;
import org.apache.zest.entitystore.sql.internal.SQLEntityState;
import org.apache.zest.entitystore.sql.internal.SQLEntityState.DefaultSQLEntityState;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreSPI;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateCommitter;
import org.apache.zest.spi.entitystore.helpers.DefaultEntityState;
import org.apache.zest.spi.entitystore.helpers.JSONKeys;
import org.apache.zest.spi.entitystore.helpers.Migration;
import org.apache.zest.spi.entitystore.helpers.StateStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL EntityStore core Mixin.
 */
// TODO Rewrite reusing JSONMapEntityStoreMixin
// Old notes:
//      Most of this code is copy-paste from {@link org.apache.zest.spi.entitystore.helpers.MapEntityStoreMixin}.
//      Refactor stuff that has to do with general things than actual MapEntityStore from MapEntityStoreMixin
//      so that this class could extend some "AbstractJSONEntityStoreMixin".
public class SQLEntityStoreMixin
    implements EntityStore, EntityStoreSPI, StateStore, ServiceActivation
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SQLEntityStoreMixin.class );

    @Service
    private DatabaseSQLService database;

    @This
    private EntityStoreSPI entityStoreSPI;

    @Structure
    private ZestSPI spi;

    @Structure
    private Application application;

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    @Optional
    @Service
    private Migration migration;

    private String uuid;

    private final AtomicInteger count = new AtomicInteger();

    @Service
    private IdentityGenerator identityGenerator;

    @Override
    public void activateService()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
        count.set( 0 );
        database.startDatabase();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        database.stopDatabase();
    }

    @Override
    public StateCommitter applyChanges( final EntityStoreUnitOfWork unitofwork, final Iterable<EntityState> states )
    {
        return new StateCommitter()
        {
            @Override
            public void commit()
            {
                Connection connection = null;
                PreparedStatement insertPS = null;
                PreparedStatement updatePS = null;
                PreparedStatement removePS = null;
                try
                {
                    connection = database.getConnection();
                    connection.setAutoCommit( false );
                    insertPS = database.prepareInsertEntityStatement( connection );
                    updatePS = database.prepareUpdateEntityStatement( connection );
                    removePS = database.prepareRemoveEntityStatement( connection );
                    for( EntityState state : states )
                    {
                        EntityStatus status = state.status();
                        DefaultEntityState defState = ( (SQLEntityState) state ).getDefaultEntityState();
                        Long entityPK = ( (SQLEntityState) state ).getEntityPK();
                        if( EntityStatus.REMOVED.equals( status ) )
                        {
                            database.populateRemoveEntityStatement( removePS, entityPK, state.entityReference() );
                            removePS.addBatch();
                        }
                        else
                        {
                            StringWriter writer = new StringWriter();
                            writeEntityState( defState, writer, unitofwork.identity().toString() );
                            writer.flush();
                            if( EntityStatus.UPDATED.equals( status ) )
                            {
                                Long entityOptimisticLock = ( (SQLEntityState) state ).getEntityOptimisticLock();
                                database.populateUpdateEntityStatement( updatePS, entityPK, entityOptimisticLock,
                                                                        defState.entityReference(), writer.toString(),
                                                                        unitofwork.currentTime() );
                                updatePS.addBatch();
                            }
                            else if( EntityStatus.NEW.equals( status ) )
                            {
                                database.populateInsertEntityStatement( insertPS, defState.entityReference(),
                                                                        writer.toString(), unitofwork.currentTime() );
                                insertPS.addBatch();
                            }
                        }
                    }

                    removePS.executeBatch();
                    insertPS.executeBatch();
                    updatePS.executeBatch();

                    connection.commit();
                }
                catch( SQLException sqle )
                {
                    SQLUtil.rollbackQuietly( connection );
                    throw new EntityStoreException( "Unable to apply state changes",
                                                    SQLUtil.withAllSQLExceptions( sqle ) );
                }
                catch( RuntimeException re )
                {
                    SQLUtil.rollbackQuietly( connection );
                    throw new EntityStoreException( re );
                }
                finally
                {
                    SQLUtil.closeQuietly( insertPS );
                    SQLUtil.closeQuietly( updatePS );
                    SQLUtil.closeQuietly( removePS );
                    SQLUtil.closeQuietly( connection );
                }
            }

            @Override
            public void cancel()
            {
            }
        };
    }

    @Override
    public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork,
                                      ModuleDescriptor module,
                                      EntityReference entityRef
    )
    {
        EntityValueResult valueResult = getValue( entityRef );
        DefaultEntityState state = readEntityState( module, valueResult.getReader() );
        return new DefaultSQLEntityState( state, valueResult.getEntityPK(), valueResult.getEntityOptimisticLock() );
    }

    @Override
    public String versionOf( EntityStoreUnitOfWork unitOfWork, EntityReference entityRef )
    {
        EntityValueResult valueResult = getValue( entityRef );
        Reader entityState = valueResult.getReader();
        try
        {
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            final String version = jsonObject.getString( JSONKeys.VERSION );
            return version;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference entityRef,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultSQLEntityState(
            new DefaultEntityState( unitOfWork.currentTime(), entityRef, entityDescriptor ) );
    }

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module, entityStoreSPI, newUnitOfWorkId(), usecase, currentTime );
    }

    @Override
    public Stream<EntityState> entityStates( final ModuleDescriptor module )
    {
        try
        {
            Connection connection = database.getConnection();
            PreparedStatement ps = database.prepareGetAllEntitiesStatement( connection );
            database.populateGetAllEntitiesStatement( ps );
            ResultSet rs = ps.executeQuery();
            return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<EntityState>( Long.MAX_VALUE, Spliterator.ORDERED )
                {
                    @Override
                    public boolean tryAdvance( final Consumer<? super EntityState> action )
                    {
                        try
                        {
                            if( !rs.next() ) { return false; }
                            EntityState entityState = readEntityState( module,
                                                                       database.getEntityValue( rs ).getReader() );
                            action.accept( entityState );
                            return true;
                        }
                        catch( SQLException ex )
                        {
                            SQLUtil.closeQuietly( rs, ex );
                            SQLUtil.closeQuietly( ps, ex );
                            SQLUtil.closeQuietly( connection, ex );
                            throw new EntityStoreException( "Unable to get next entity state",
                                                            SQLUtil.withAllSQLExceptions( ex ) );
                        }
                    }
                },
                false
            ).onClose(
                () ->
                {
                    SQLUtil.closeQuietly( rs );
                    SQLUtil.closeQuietly( ps );
                    SQLUtil.closeQuietly( connection );
                }
            );
        }
        catch( SQLException ex )
        {
            throw new EntityStoreException( "Unable to get entity states", SQLUtil.withAllSQLExceptions( ex ) );
        }
    }

    protected Identity newUnitOfWorkId()
    {
        return identityGenerator.generate( EntityStore.class );
    }

    protected DefaultEntityState readEntityState( ModuleDescriptor module, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            final EntityStatus[] status = { EntityStatus.LOADED };

            String version = jsonObject.getString( JSONKeys.VERSION );
            Instant modified = Instant.ofEpochMilli( jsonObject.getLong( JSONKeys.MODIFIED ) );
            String identity = jsonObject.getString( JSONKeys.IDENTITY );

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

                LOGGER.trace( "Updated version nr on {} from {} to {}",
                              identity, currentAppVersion, application.version() );

                // State changed
                status[ 0 ] = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new NoSuchEntityTypeException( type, module.name(), module.typeLookup() );
            }

            Map<QualifiedName, Object> properties = new HashMap<>();
            JSONObject props = jsonObject.getJSONObject( JSONKeys.PROPERTIES );
            entityDescriptor.state().properties().forEach(
                propertyDescriptor ->
                {
                    Object jsonValue;
                    try
                    {
                        jsonValue = props.get(
                            propertyDescriptor.qualifiedName().name() );
                        if( JSONObject.NULL.equals( jsonValue ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(), null );
                        }
                        else
                        {
                            Object value = valueSerialization.deserialize( module,
                                                                           propertyDescriptor.valueType(),
                                                                           jsonValue.toString() );
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
                    catch( JSONException e )
                    {
                        // Value not found, default it
                        Object initialValue = propertyDescriptor.initialValue( module );
                        properties.put( propertyDescriptor.qualifiedName(), initialValue );
                        status[ 0 ] = EntityStatus.UPDATED;
                    }
                }
            );

            Map<QualifiedName, EntityReference> associations = new HashMap<>();
            JSONObject assocs = jsonObject.getJSONObject( JSONKeys.ASSOCIATIONS );
            entityDescriptor.state().associations().forEach(
                associationType ->
                {
                    try
                    {
                        Object jsonValue = assocs.get( associationType.qualifiedName().name() );
                        EntityReference value = jsonValue == JSONObject.NULL
                                                ? null
                                                : EntityReference.parseEntityReference( (String) jsonValue );
                        associations.put( associationType.qualifiedName(), value );
                    }
                    catch( JSONException e )
                    {
                        // Association not found, default it to null
                        associations.put( associationType.qualifiedName(), null );
                        status[ 0 ] = EntityStatus.UPDATED;
                    }
                }
            );

            JSONObject manyAssocs = jsonObject.getJSONObject( JSONKeys.MANY_ASSOCIATIONS );
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<>();
            entityDescriptor.state().manyAssociations().forEach(
                manyAssociationType ->
                {
                    List<EntityReference> references = new ArrayList<>();
                    try
                    {
                        JSONArray jsonValues = manyAssocs.getJSONArray( manyAssociationType.qualifiedName().name() );
                        for( int i = 0; i < jsonValues.length(); i++ )
                        {
                            Object jsonValue = jsonValues.getString( i );
                            EntityReference value = jsonValue == JSONObject.NULL
                                                    ? null
                                                    : EntityReference.parseEntityReference( (String) jsonValue );
                            references.add( value );
                        }
                        manyAssociations.put( manyAssociationType.qualifiedName(), references );
                    }
                    catch( JSONException e )
                    {
                        // ManyAssociation not found, default to empty one
                        manyAssociations.put( manyAssociationType.qualifiedName(), references );
                    }
                } );

            JSONObject namedAssocs = jsonObject.has( JSONKeys.NAMED_ASSOCIATIONS )
                                     ? jsonObject.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS )
                                     : new JSONObject();
            Map<QualifiedName, Map<String, EntityReference>> namedAssociations = new HashMap<>();
            entityDescriptor.state().namedAssociations().forEach(
                namedAssociationType ->
                {
                    Map<String, EntityReference> references = new LinkedHashMap<>();
                    try
                    {
                        JSONObject jsonValues = namedAssocs.getJSONObject( namedAssociationType.qualifiedName().name() );
                        JSONArray names = jsonValues.names();
                        if( names != null )
                        {
                            for( int idx = 0; idx < names.length(); idx++ )
                            {
                                String name = names.getString( idx );
                                String jsonValue = jsonValues.getString( name );
                                references.put( name, EntityReference.parseEntityReference( jsonValue ) );
                            }
                        }
                        namedAssociations.put( namedAssociationType.qualifiedName(), references );
                    }
                    catch( JSONException e )
                    {
                        // NamedAssociation not found, default to empty one
                        namedAssociations.put( namedAssociationType.qualifiedName(), references );
                    }
                } );

            return new DefaultEntityState( version, modified,
                                           EntityReference.parseEntityReference( identity ), status[ 0 ],
                                           entityDescriptor,
                                           properties, associations, manyAssociations, namedAssociations );
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
        JSONObject jsonObject;
        try( Reader reader = getValue( EntityReference.parseEntityReference( id ) ).getReader() )
        {
            jsonObject = new JSONObject( new JSONTokener( reader ) );
        }
        catch( JSONException e )
        {
            throw new IOException( e );
        }
        return jsonObject;
    }

    protected EntityValueResult getValue( EntityReference ref )
    {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = database.getConnection();
            ps = database.prepareGetEntityStatement( connection );
            database.populateGetEntityStatement( ps, ref );
            rs = ps.executeQuery();
            if( !rs.next() )
            {
                throw new EntityNotFoundException( ref );
            }
            return database.getEntityValue( rs );
        }
        catch( SQLException sqle )
        {
            throw new EntityStoreException( "Unable to get Entity " + ref, SQLUtil.withAllSQLExceptions( sqle ) );
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( ps );
            SQLUtil.closeQuietly( connection );
        }
    }

    protected void writeEntityState( DefaultEntityState state, Writer writer, String version )
        throws EntityStoreException
    {
        try
        {
            JSONWriter json = new JSONWriter( writer );
            JSONWriter properties = json.object()
                                        .key( JSONKeys.IDENTITY )
                                        .value( state.entityReference().identity().toString() )
                                        .key( JSONKeys.APPLICATION_VERSION )
                                        .value( application.version() )
                                        .key( JSONKeys.TYPE )
                                        .value( state.entityDescriptor().types().findFirst().get().getName() )
                                        .key( JSONKeys.VERSION )
                                        .value( version )
                                        .key( JSONKeys.MODIFIED )
                                        .value( state.lastModified().toEpochMilli() )
                                        .key( JSONKeys.PROPERTIES )
                                        .object();

            state.entityDescriptor().state().properties().forEach(
                persistentProperty ->
                {
                    try
                    {
                        Object value = state.properties().get( persistentProperty.qualifiedName() );
                        json.key( persistentProperty.qualifiedName().name() );
                        if( value == null || ValueType.isPrimitiveValue( value ) )
                        {
                            json.value( value );
                        }
                        else
                        {
                            String serialized = valueSerialization.serialize( value );
                            if( serialized.startsWith( "{" ) )
                            {
                                json.value( new JSONObject( serialized ) );
                            }
                            else if( serialized.startsWith( "[" ) )
                            {
                                json.value( new JSONArray( serialized ) );
                            }
                            else
                            {
                                json.value( serialized );
                            }
                        }
                    }
                    catch( JSONException e )
                    {
                        throw new EntityStoreException(
                            "Could not store EntityState", e );
                    }
                } );

            JSONWriter associations = properties.endObject().key( JSONKeys.ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, EntityReference> stateNameEntityRefEntry : state.associations().entrySet() )
            {
                EntityReference value = stateNameEntityRefEntry.getValue();
                associations.key( stateNameEntityRefEntry.getKey().name() )
                            .value( value != null ? value.identity().toString() : null );
            }

            JSONWriter manyAssociations = associations.endObject().key( JSONKeys.MANY_ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, List<EntityReference>> stateNameListEntry : state.manyAssociations().entrySet() )
            {
                JSONWriter assocs = manyAssociations.key( stateNameListEntry.getKey().name() ).array();
                for( EntityReference entityReference : stateNameListEntry.getValue() )
                {
                    assocs.value( entityReference.identity().toString() );
                }
                assocs.endArray();
            }

            JSONWriter namedAssociations = manyAssociations.endObject().key( JSONKeys.NAMED_ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, Map<String, EntityReference>> stateNameMapEntry : state.namedAssociations().entrySet() )
            {
                JSONWriter assocs = namedAssociations.key( stateNameMapEntry.getKey().name() ).object();
                for( Map.Entry<String, EntityReference> entry : stateNameMapEntry.getValue().entrySet() )
                {
                    assocs.key( entry.getKey() ).value( entry.getValue().identity().toString() );
                }
                assocs.endObject();
            }
            namedAssociations.endObject().endObject();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }
}
