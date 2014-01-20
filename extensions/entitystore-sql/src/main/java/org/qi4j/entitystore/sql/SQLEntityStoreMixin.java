/*
 * Copyright (c) 2010-2012, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010-2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.sql;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.entitystore.sql.internal.DatabaseSQLService;
import org.qi4j.entitystore.sql.internal.DatabaseSQLService.EntityValueResult;
import org.qi4j.entitystore.sql.internal.SQLEntityState;
import org.qi4j.entitystore.sql.internal.SQLEntityState.DefaultSQLEntityState;
import org.qi4j.functional.Visitor;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.qi4j.spi.entitystore.helpers.JSONKeys;
import org.qi4j.spi.entitystore.helpers.Migration;
import org.qi4j.spi.entitystore.helpers.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.functional.Iterables.first;

/**
 * SQL EntityStore core Mixin.
 */
// TODO Rewrite reusing JSONMapEntityStoreMixin
// Old notes:
//      Most of this code is copy-paste from {@link org.qi4j.spi.entitystore.helpers.MapEntityStoreMixin}.
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
    private Application application;

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    @Optional
    @Service
    private Migration migration;

    private String uuid;

    private final AtomicInteger count = new AtomicInteger();

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
                            database.populateRemoveEntityStatement( removePS, entityPK, state.identity() );
                            removePS.addBatch();
                        }
                        else
                        {
                            StringWriter writer = new StringWriter();
                            writeEntityState( defState, writer, unitofwork.identity() );
                            writer.flush();
                            if( EntityStatus.UPDATED.equals( status ) )
                            {
                                Long entityOptimisticLock = ( (SQLEntityState) state ).getEntityOptimisticLock();
                                database.populateUpdateEntityStatement( updatePS, entityPK, entityOptimisticLock,
                                                                        defState.identity(), writer.toString(),
                                                                        unitofwork.currentTime() );
                                updatePS.addBatch();
                            }
                            else if( EntityStatus.NEW.equals( status ) )
                            {
                                database.populateInsertEntityStatement( insertPS, defState.identity(),
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
                    if( LOGGER.isDebugEnabled() )
                    {
                        StringWriter sb = new StringWriter();
                        sb.append(
                            "SQLException during commit, logging nested exceptions before throwing EntityStoreException:\n" );
                        SQLException e = sqle;
                        while( e != null )
                        {
                            e.printStackTrace( new PrintWriter( sb, true ) );
                            e = e.getNextException();
                        }
                        LOGGER.debug( sb.toString() );
                    }
                    throw new EntityStoreException( sqle );
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
    public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork, EntityReference entityRef )
    {
        EntityValueResult valueResult = getValue( entityRef );
        return new DefaultSQLEntityState( readEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork,
                                                           valueResult.getReader() ),
                                          valueResult.getEntityPK(),
                                          valueResult.getEntityOptimisticLock() );
    }

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference entityRef, EntityDescriptor entityDescriptor )
    {
        return new DefaultSQLEntityState( new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork,
                                                                  entityRef,
                                                                  entityDescriptor ) );
    }

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module, long currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSPI, newUnitOfWorkId(), module, usecase, currentTime );
    }

    @Override
    public Input<EntityState, EntityStoreException> entityStates( final Module module )
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
                        queryAllEntities( module, new EntityStatesVisitor()
                        {
                            @Override
                            public boolean visit( EntityState visited )
                                throws SQLException
                            {
                                try
                                {
                                    receiver.receive( visited );
                                }
                                catch( Throwable receiverThrowableType )
                                {
                                    throw new SQLException( receiverThrowableType );
                                }
                                return true;
                            }
                        } );
                    }
                } );
            }
        };
    }

    private void queryAllEntities( Module module, EntityStatesVisitor entityStatesVisitor )
    {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        UsecaseBuilder builder = UsecaseBuilder.buildUsecase( "qi4j.entitystore.sql.visit" );
        Usecase usecase = builder.withMetaInfo( CacheOptions.NEVER ).newUsecase();
        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSPI,
                                                                                   newUnitOfWorkId(), module, usecase,
                                                                                   System.currentTimeMillis() );
        try
        {
            connection = database.getConnection();
            ps = database.prepareGetAllEntitiesStatement( connection );
            database.populateGetAllEntitiesStatement( ps );
            rs = ps.executeQuery();
            while( rs.next() )
            {
                DefaultEntityState entityState = readEntityState( uow, database.getEntityValue( rs ).getReader() );
                if( !entityStatesVisitor.visit( entityState ) )
                {
                    return;
                }
            }
        }
        catch( SQLException ex )
        {
            throw new EntityStoreException( ex );
        }
        finally
        {
            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( ps );
            SQLUtil.closeQuietly( connection );
        }
    }

    private interface EntityStatesVisitor
        extends Visitor<EntityState, SQLException>
    {
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count.incrementAndGet() );
    }

    protected DefaultEntityState readEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            Module module = unitOfWork.module();
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            EntityStatus status = EntityStatus.LOADED;

            String version = jsonObject.getString( JSONKeys.VERSION );
            long modified = jsonObject.getLong( JSONKeys.MODIFIED );
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
                status = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type );
            }

            Map<QualifiedName, Object> properties = new HashMap<>();
            JSONObject props = jsonObject.getJSONObject( JSONKeys.PROPERTIES );
            for( PropertyDescriptor propertyDescriptor : entityDescriptor.state().properties() )
            {
                Object jsonValue;
                try
                {
                    jsonValue = props.get( propertyDescriptor.qualifiedName().name() );
                }
                catch( JSONException e )
                {
                    // Value not found, default it
                    Object initialValue = propertyDescriptor.initialValue( module );
                    properties.put( propertyDescriptor.qualifiedName(), initialValue );
                    status = EntityStatus.UPDATED;
                    continue;
                }
                if( JSONObject.NULL.equals( jsonValue ) )
                {
                    properties.put( propertyDescriptor.qualifiedName(), null );
                }
                else
                {
                    Object value = valueSerialization.deserialize( propertyDescriptor.valueType(), jsonValue.toString() );
                    properties.put( propertyDescriptor.qualifiedName(), value );
                }
            }

            Map<QualifiedName, EntityReference> associations = new HashMap<>();
            JSONObject assocs = jsonObject.getJSONObject( JSONKeys.ASSOCIATIONS );
            for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
            {
                try
                {
                    Object jsonValue = assocs.get( associationType.qualifiedName().name() );
                    EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference(
                        (String) jsonValue );
                    associations.put( associationType.qualifiedName(), value );
                }
                catch( JSONException e )
                {
                    // Association not found, default it to null
                    associations.put( associationType.qualifiedName(), null );
                    status = EntityStatus.UPDATED;
                }
            }

            JSONObject manyAssocs = jsonObject.getJSONObject( JSONKeys.MANY_ASSOCIATIONS );
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<>();
            for( AssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
            {
                List<EntityReference> references = new ArrayList<>();
                try
                {
                    JSONArray jsonValues = manyAssocs.getJSONArray( manyAssociationType.qualifiedName().name() );
                    for( int i = 0; i < jsonValues.length(); i++ )
                    {
                        Object jsonValue = jsonValues.getString( i );
                        EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference(
                            (String) jsonValue );
                        references.add( value );
                    }
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
                catch( JSONException e )
                {
                    // ManyAssociation not found, default to empty one
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
            }

            JSONObject namedAssocs = jsonObject.has( JSONKeys.NAMED_ASSOCIATIONS )
                                     ? jsonObject.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS )
                                     : new JSONObject();
            Map<QualifiedName, Map<String, EntityReference>> namedAssociations = new HashMap<>();
            for( AssociationDescriptor namedAssociationType : entityDescriptor.state().namedAssociations() )
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
            }

            return new DefaultEntityState( unitOfWork, version, modified,
                                           EntityReference.parseEntityReference( identity ), status, entityDescriptor,
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

            EntityValueResult result = database.getEntityValue( rs );

            return result;
        }
        catch( SQLException sqle )
        {
            throw new EntityStoreException( "Unable to get Entity " + ref, sqle );
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
            JSONWriter properties = json.object().
                key( JSONKeys.IDENTITY ).value( state.identity().identity() ).
                key( JSONKeys.APPLICATION_VERSION ).value( application.version() ).
                key( JSONKeys.TYPE ).value( first( state.entityDescriptor().types() ).getName() ).
                key( JSONKeys.VERSION ).value( version ).
                key( JSONKeys.MODIFIED ).value( state.lastModified() ).
                key( JSONKeys.PROPERTIES ).object();

            for( PropertyDescriptor persistentProperty : state.entityDescriptor().state().properties() )
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

            JSONWriter associations = properties.endObject().key( JSONKeys.ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : state.associations().entrySet() )
            {
                EntityReference value = stateNameEntityReferenceEntry.getValue();
                associations.key( stateNameEntityReferenceEntry.getKey().name() ).
                    value( value != null ? value.identity() : null );
            }

            JSONWriter manyAssociations = associations.endObject().key( JSONKeys.MANY_ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, List<EntityReference>> stateNameListEntry : state.manyAssociations().entrySet() )
            {
                JSONWriter assocs = manyAssociations.key( stateNameListEntry.getKey().name() ).array();
                for( EntityReference entityReference : stateNameListEntry.getValue() )
                {
                    assocs.value( entityReference.identity() );
                }
                assocs.endArray();
            }

            JSONWriter namedAssociations = manyAssociations.endObject().key( JSONKeys.NAMED_ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, Map<String, EntityReference>> stateNameMapEntry : state.namedAssociations().entrySet() )
            {
                JSONWriter assocs = namedAssociations.key( stateNameMapEntry.getKey().name() ).object();
                for( Map.Entry<String, EntityReference> entry : stateNameMapEntry.getValue().entrySet() )
                {
                    assocs.key( entry.getKey() ).value( entry.getValue().identity() );
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
