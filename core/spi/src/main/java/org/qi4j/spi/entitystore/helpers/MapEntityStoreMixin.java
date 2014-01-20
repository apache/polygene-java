/*
 * Copyright (c) 2009-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.spi.entitystore.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.functional.Iterables.first;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore.
 *
 * <p>Implement {@link MapEntityStore} and add as mixin to the service using this mixin.</p>
 * <p>See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.</p>
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
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    @Optional
    @Service
    private Migration migration;

    @Uses
    private ServiceDescriptor descriptor;

    protected String uuid;
    private int count;

    private Logger logger;

    @Override
    public void activateMapEntityStore()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";

        logger = LoggerFactory.getLogger( descriptor.identity() );
    }

    // EntityStore
    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, Module module, long currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module, usecaseMetaInfo, currentTime );
    }

    // EntityStoreSPI
    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor );
    }

    @Override
    public synchronized EntityState entityStateOf( EntityStoreUnitOfWork unitofwork, EntityReference identity )
    {
        DefaultEntityStoreUnitOfWork unitOfWork = (DefaultEntityStoreUnitOfWork) unitofwork;
        Reader in = mapEntityStore.get( identity );
        return readEntityState( unitOfWork, in );
    }

    @Override
    public StateCommitter applyChanges( final EntityStoreUnitOfWork unitofwork, final Iterable<EntityState> state
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
                            for( EntityState entityState : state )
                            {
                                DefaultEntityState state = (DefaultEntityState) entityState;
                                if( state.status().equals( EntityStatus.NEW ) )
                                {
                                    try( Writer writer = changer.newEntity( state.identity(), state.entityDescriptor() ) )
                                    {
                                        writeEntityState( state, writer, unitofwork.identity(), unitofwork.currentTime() );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.UPDATED ) )
                                {
                                    try( Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor() ) )
                                    {
                                        writeEntityState( state, writer, unitofwork.identity(), unitofwork.currentTime() );
                                    }
                                }
                                else if( state.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state.identity(), state.entityDescriptor() );
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
                        Usecase usecase = UsecaseBuilder
                            .buildUsecase( "qi4j.entitystore.entitystates" )
                            .withMetaInfo( CacheOptions.NEVER )
                            .newUsecase();

                        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork(
                            entityStoreSpi,
                            newUnitOfWorkId(),
                            module,
                            usecase,
                            System.currentTimeMillis() );

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
                                            final EntityState entity = readEntityState( uow, item );
                                            if( entity.status() == EntityStatus.UPDATED )
                                            {
                                                migrated.add( entity );

                                                // Synch back 100 at a time
                                                if( migrated.size() > 100 )
                                                {
                                                    synchMigratedEntities( migrated );
                                                }
                                            }
                                            receiver.receive( entity );
                                        }
                                    } );

                                    // Synch any remaining migrated entities
                                    if( !migrated.isEmpty() )
                                    {
                                        synchMigratedEntities( migrated );
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
    {
        try
        {
            mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
            {
                @Override
                public void visitMap( MapEntityStore.MapChanger changer )
                    throws IOException
                {
                    for( EntityState migratedEntity : migratedEntities )
                    {
                        DefaultEntityState state = (DefaultEntityState) migratedEntity;
                        try( Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor() ) )
                        {
                            writeEntityState( state, writer, state.version(), state.lastModified() );
                        }
                    }
                }
            } );
            migratedEntities.clear();
        }
        catch( IOException e )
        {
            logger.warn( "Could not store migrated entites", e );
        }
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    protected void writeEntityState( DefaultEntityState state, Writer writer, String version, long lastModified )
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
                key( JSONKeys.MODIFIED ).value( lastModified ).
                key( JSONKeys.PROPERTIES ).object();
            EntityDescriptor entityType = state.entityDescriptor();
            for( PropertyDescriptor persistentProperty : entityType.state().properties() )
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
                for( Map.Entry<String, EntityReference> namedRef : stateNameMapEntry.getValue().entrySet() )
                {
                    assocs.key( namedRef.getKey() ).value( namedRef.getValue().identity() );
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

    protected EntityState readEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
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

                LoggerFactory.getLogger( MapEntityStoreMixin.class )
                    .debug(
                        "Updated version nr on " + identity + " from " + currentAppVersion + " to " + application.version() );

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
                    EntityReference value = jsonValue == JSONObject.NULL
                                            ? null
                                            : EntityReference.parseEntityReference( (String) jsonValue );
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
            }

            JSONObject namedAssocs = jsonObject.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS );
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
                            Object value = jsonValues.get( name );
                            EntityReference ref = value == JSONObject.NULL
                                                  ? null
                                                  : EntityReference.parseEntityReference( (String) value );
                            references.put( name, ref );
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

            return new DefaultEntityState( unitOfWork,
                                           version,
                                           modified,
                                           EntityReference.parseEntityReference( identity ),
                                           status,
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations,
                                           namedAssociations
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
        JSONObject jsonObject;
        try( Reader reader = mapEntityStore.get( EntityReference.parseEntityReference( id ) ) )
        {
            jsonObject = new JSONObject( new JSONTokener( reader ) );
        }
        catch( JSONException e )
        {
            throw new IOException( e );
        }
        return jsonObject;
    }
}
