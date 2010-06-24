/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.map.MapEntityStoreMixin;
import org.qi4j.entitystore.map.Migration;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.entitystore.sql.database.DatabaseSQLService;
import org.qi4j.entitystore.sql.database.DatabaseSQLService.EntityValueResult;
import org.qi4j.library.sql.api.SQLEntityState;
import org.qi4j.library.sql.api.SQLEntityState.DefaultSQLEntityState;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.structure.ModuleSPI;


/**
 * Most of this code is copy-paste from {@link MapEntityStoreMixin}. TODO refactor stuff that has to do with general
 * things than actual MapEntityStore from {@link MapEntityStoreMixin} so that this class could extend some
 * "AbstractJSONEntityStoreMixin".
 *
 * @author Stanislav Muhametsin
 */
public abstract class SQLEntityStoreMixin
    implements EntityStore, EntityStoreSPI, StateStore, Activatable
{
    @Service
    private DatabaseSQLService _database;

    @This
    private EntityStoreSPI _entityStoreSPI;

    @Structure
    private Application _application;

    @Optional
    @Service
    private Migration _migration;

    private String _uuid;
    private Integer _count;

    public void activate()
        throws Exception
    {
        this._uuid = UUID.randomUUID().toString() + "-";
        this._count = 0;

        this._database.startDatabase();
    }

    public void passivate()
        throws Exception
    {
        // NOOP (maybe add closing connection if application is in RELEASE mode ? )
        this._database.stopDatabase();
    }

    public StateCommitter applyChanges( final Iterable<EntityState> states, final String version )
    {
        return new StateCommitter()
        {

            public void commit()
            {
                Connection connection = null;
                PreparedStatement insertPS = null;
                PreparedStatement updatePS = null;
                PreparedStatement removePS = null;
                try
                {
                    connection = _database.getConnection();
                    insertPS = _database.prepareInsertEntityStatement( connection );
                    updatePS = _database.prepareUpdateEntityStatement( connection );
                    removePS = _database.prepareRemoveEntityStatement( connection );
                    for (EntityState state : states)
                    {
                        EntityStatus status = state.status();
                        DefaultEntityState defState = ((SQLEntityState)state).getDefaultEntityState();
                        Long entityPK = ((SQLEntityState)state).getEntityPK();
                        if (EntityStatus.REMOVED.equals( status ))
                        {
                            _database.populateRemoveEntityStatement( removePS, entityPK, state.identity() );
                            removePS.addBatch();
                        } else
                        {
                            StringWriter writer = new StringWriter();
                            writeEntityState( defState, writer, version );
                            writer.flush();
                            if (EntityStatus.UPDATED.equals( status ))
                            {
                                _database.populateUpdateEntityStatement( updatePS, entityPK, defState.identity(), writer.toString() );
                                updatePS.addBatch();
                            } else if (EntityStatus.NEW.equals( status ))
                            {
                                _database.populateInsertEntityStatement( insertPS, entityPK, defState.identity(), writer.toString() );
                                insertPS.addBatch();
                            }
                        }
                    }

                    removePS.executeBatch();
                    insertPS.executeBatch();
                    updatePS.executeBatch();

                    connection.commit();

                } catch (SQLException sqle)
                {
                    SQLUtil.rollbackQuietly( connection );
//                    SQLException e = sqle;
//                    while (e != null)
//                    {
//                        e.printStackTrace();
//                        e = e.getNextException();
//                    }
                    throw new EntityStoreException( sqle );
                } catch (RuntimeException re)
                {
                    SQLUtil.rollbackQuietly( connection );
                    throw new EntityStoreException( re );
                } finally
                {
                    SQLUtil.closeQuietly( insertPS );
                    SQLUtil.closeQuietly( updatePS );
                    SQLUtil.closeQuietly( removePS );
                }
            }

            public void cancel()
            {
            }
        };
    }

    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        EntityValueResult valueResult = this.getValue( identity );
        return new DefaultSQLEntityState( this.readEntityState( (DefaultEntityStoreUnitOfWork)unitOfWork, valueResult.getReader() ), valueResult.getEntityPK() );
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity,
        EntityDescriptor entityDescriptor )
    {
        return new DefaultSQLEntityState( new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity,
            entityDescriptor ), this._database.newPKForEntity() );
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, ModuleSPI module )
    {
        return new DefaultEntityStoreUnitOfWork( this._entityStoreSPI, this.newUnitOfWorkId(), module );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, ModuleSPI module )
    {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final DefaultEntityStoreUnitOfWork uow =
            new DefaultEntityStoreUnitOfWork( this._entityStoreSPI, newUnitOfWorkId(), module );
        try
        {
            connection = this._database.getConnection();
            ps = this._database.prepareGetAllEntitiesStatement( connection );
            this._database.populateGetAllEntitiesStatement( ps );
            rs = ps.executeQuery();
            while (rs.next())
            {
                visitor.visitEntityState( this.readEntityState( uow, this._database.getEntityValue( rs ).getReader() ) );
            }
        } catch (SQLException sqle)
        {
            throw new EntityStoreException( sqle );
        } finally
        {
            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( ps );
        }

        return uow;
    }

    protected String newUnitOfWorkId()
    {
        return this._uuid + Integer.toHexString( this._count++ );
    }

    protected DefaultEntityState readEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            ModuleSPI module = unitOfWork.module();
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            EntityStatus status = EntityStatus.LOADED;

            String version = jsonObject.getString( "version" );
            long modified = jsonObject.getLong( "modified" );
            String identity = jsonObject.getString( "identity" );

            // Check if version is correct
            String currentAppVersion = jsonObject.optString( MapEntityStore.JSONKeys.application_version.name(), "0.0" );
            if( !currentAppVersion.equals( this._application.version() ) )
            {
                if( this._migration != null )
                {
                    this._migration.migrate( jsonObject, this._application.version(), this );
                }
                else
                {
                    // Do nothing - set version to be correct
                    jsonObject.put( MapEntityStore.JSONKeys.application_version.name(), this._application.version() );
                }

                Logger.getLogger( MapEntityStoreMixin.class.getName() ).info(
                    "Updated version nr on " + identity + " from " + currentAppVersion + " to "
                        + this._application.version() );

                // State changed
                status = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( "type" );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type );
            }

            Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();
            JSONObject props = jsonObject.getJSONObject( "properties" );
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
                    Object initialValue = propertyDescriptor.initialValue();
                    properties.put( propertyDescriptor.qualifiedName(), initialValue );
                    status = EntityStatus.UPDATED;
                    continue;
                }
                if( jsonValue == JSONObject.NULL )
                {
                    properties.put( propertyDescriptor.qualifiedName(), null );
                }
                else
                {
                    Object value = ((PropertyTypeDescriptor) propertyDescriptor).propertyType().type().fromJSON(
                        jsonValue, module );
                    properties.put( propertyDescriptor.qualifiedName(), value );
                }
            }

            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            JSONObject assocs = jsonObject.getJSONObject( "associations" );
            for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
            {
                try
                {
                    Object jsonValue = assocs.get( associationType.qualifiedName().name() );
                    EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference
                        .parseEntityReference( (String) jsonValue );
                    associations.put( associationType.qualifiedName(), value );
                }
                catch( JSONException e )
                {
                    // Association not found, default it to null
                    associations.put( associationType.qualifiedName(), null );
                    status = EntityStatus.UPDATED;
                }
            }

            JSONObject manyAssocs = jsonObject.getJSONObject( "manyassociations" );
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            for( ManyAssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
            {
                List<EntityReference> references = new ArrayList<EntityReference>();
                try
                {
                    JSONArray jsonValues = manyAssocs.getJSONArray( manyAssociationType.qualifiedName().name() );
                    for( int i = 0; i < jsonValues.length(); i++ )
                    {
                        Object jsonValue = jsonValues.getString( i );
                        EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference
                            .parseEntityReference( (String) jsonValue );
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

            return new DefaultEntityState( unitOfWork, version, modified, EntityReference
                .parseEntityReference( identity ), status, entityDescriptor, properties, associations, manyAssociations );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public JSONObject getState( String id )
        throws IOException
    {
        Reader reader = this.getValue(  EntityReference.parseEntityReference( id ) ).getReader();
        JSONObject jsonObject;
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

    protected EntityValueResult getValue(EntityReference ref)
    {
        try
        {
            Connection connection = this._database.getConnection();
            PreparedStatement ps = this._database.prepareGetEntityStatement( connection );
            this._database.populateGetEntityStatement( ps, ref );
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
            {
                throw new EntityNotFoundException( ref );
            }

            EntityValueResult result = this._database.getEntityValue( rs );

            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( ps );

            return result;
        } catch (SQLException sqle)
        {
            throw new EntityStoreException( "Unable to get Entity " + ref, sqle );
        }
    }

    protected void writeEntityState( DefaultEntityState state, Writer writer, String version )
    throws EntityStoreException
{
    try
    {
        JSONWriter json = new JSONWriter( writer );
        JSONWriter properties = json.object().
            key( "identity" ).value( state.identity().identity() ).
            key( "application_version" ).value( this._application.version() ).
            key( "type" ).value( state.entityDescriptor().entityType().type().name() ).
            key( "version" ).value( version ).
            key( "modified" ).value( state.lastModified() ).
            key( "properties" ).object();
        EntityType entityType = state.entityDescriptor().entityType();
        for( PropertyType propertyType : entityType.properties() )
        {
            Object value = state.properties().get( propertyType.qualifiedName() );
            json.key( propertyType.qualifiedName().name() );
            if( value == null )
            {
                json.value( null );
            }
            else
            {
                propertyType.type().toJSON( value, json );
            }
        }

        JSONWriter associations = properties.endObject().key( "associations" ).object();
        for( Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : state.associations()
            .entrySet() )
        {
            EntityReference value = stateNameEntityReferenceEntry.getValue();
            associations.key( stateNameEntityReferenceEntry.getKey().name() ).
                value( value != null ? value.identity() : null );
        }

        JSONWriter manyAssociations = associations.endObject().key( "manyassociations" ).object();
        for( Map.Entry<QualifiedName, List<EntityReference>> stateNameListEntry : state.manyAssociations()
            .entrySet() )
        {
            JSONWriter assocs = manyAssociations.key( stateNameListEntry.getKey().name() ).array();
            for( EntityReference entityReference : stateNameListEntry.getValue() )
            {
                assocs.value( entityReference.identity() );
            }
            assocs.endArray();
        }
        manyAssociations.endObject().endObject();
    }
    catch( JSONException e )
    {
        throw new EntityStoreException( "Could not store EntityState", e );
    }
}


}
