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
package org.apache.zest.spi.entitystore.helpers;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreSPI;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateCommitter;
import org.apache.zest.spi.module.ModelModule;
import org.apache.zest.spi.module.ModuleSpi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore.
 *
 * <p>Implement {@link MapEntityStore} and add as mixin to the service using this mixin.</p>
 * <p>See {@link org.apache.zest.entitystore.memory.MemoryMapEntityStoreMixin} for reference.</p>
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
    private ZestSPI spi;

    @Structure
    private Application application;

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    @Optional
    @Service
    private Migration migration;

    //    @Uses
//    private ServiceDescriptor descriptor;
//
    protected String uuid;
    private int count;

    @Override
    public void activateMapEntityStore()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
    }

    // EntityStore
    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, long currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), usecaseMetaInfo, currentTime );
    }

    // EntityStoreSPI
    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultEntityState( unitOfWork.currentTime(), identity, entityDescriptor );
    }

    @Override
    public synchronized EntityState entityStateOf( EntityStoreUnitOfWork unitofwork,
                                                   ModuleSpi module,
                                                   EntityReference identity
    )
    {
        Reader in = mapEntityStore.get( identity );
        return readEntityState( module, in );
    }

    @Override
    public synchronized String versionOf( EntityStoreUnitOfWork unitofwork,
                                                   EntityReference identity
    )
    {
        Reader in = mapEntityStore.get( identity );
        try
        {
            JSONObject jsonObject = new JSONObject( new JSONTokener( in ) );
            return jsonObject.getString( JSONKeys.VERSION );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
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
                    mapEntityStore.applyChanges( changer -> {
                        for( EntityState entityState : state )
                        {
                            DefaultEntityState state1 = (DefaultEntityState) entityState;
                            if( state1.status().equals( EntityStatus.NEW ) )
                            {
                                try (Writer writer = changer.newEntity( state1.identity(), state1.entityDescriptor() ))
                                {
                                    writeEntityState( state1, writer, unitofwork.identity(), unitofwork.currentTime() );
                                }
                            }
                            else if( state1.status().equals( EntityStatus.UPDATED ) )
                            {
                                try (Writer writer = changer.updateEntity( state1.identity(), state1.entityDescriptor() ))
                                {
                                    writeEntityState( state1, writer, unitofwork.identity(), unitofwork.currentTime() );
                                }
                            }
                            else if( state1.status().equals( EntityStatus.REMOVED ) )
                            {
                                changer.removeEntity( state1.identity(), state1.entityDescriptor() );
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
                    public <RecThrowableType extends Throwable> void sendTo( final Receiver<? super EntityState, RecThrowableType> receiver )
                        throws RecThrowableType, EntityStoreException
                    {
                        final List<EntityState> migrated = new ArrayList<>();
                        try
                        {
                            mapEntityStore.entityStates().transferTo( new Output<Reader, RecThrowableType>()
                            {
                                @Override
                                public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends Reader, SenderThrowableType> sender )
                                    throws RecThrowableType, SenderThrowableType
                                {
                                    sender.sendTo( item -> {
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
        mapEntityStore.applyChanges( changer -> {
            for( EntityState migratedEntity : migratedEntities )
            {
                DefaultEntityState state = (DefaultEntityState) migratedEntity;
                try (Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor() ))
                {
                    writeEntityState( state, writer, state.version(), state.lastModified() );
                }
            }
        } );
        migratedEntities.clear();
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
                key( JSONKeys.TYPE ).value( state.entityDescriptor().types().findFirst().get().getName() ).
                key( JSONKeys.VERSION ).value( version ).
                key( JSONKeys.MODIFIED ).value( lastModified ).
                key( JSONKeys.PROPERTIES ).object();
            EntityDescriptor entityType = state.entityDescriptor();
            entityType.state().properties().forEach( persistentProperty -> {
                Object value = state.properties().get( persistentProperty.qualifiedName() );
                try
                {
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
                    throw new ValueSerializationException( "Unable to write property " + persistentProperty, e );
                }
            } );

            JSONWriter associations = properties.endObject().key( JSONKeys.ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : state.associations()
                .entrySet() )
            {
                EntityReference value = stateNameEntityReferenceEntry.getValue();
                associations.key( stateNameEntityReferenceEntry.getKey().name() ).
                    value( value != null ? value.identity() : null );
            }

            JSONWriter manyAssociations = associations.endObject().key( JSONKeys.MANY_ASSOCIATIONS ).object();
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

            JSONWriter namedAssociations = manyAssociations.endObject().key( JSONKeys.NAMED_ASSOCIATIONS ).object();
            for( Map.Entry<QualifiedName, Map<String, EntityReference>> stateNameMapEntry : state.namedAssociations()
                .entrySet() )
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

    protected EntityState readEntityState( ModuleSpi module, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            final EntityStatus[] status = {EntityStatus.LOADED};

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
                // State changed
                status[0] = EntityStatus.UPDATED;
            }

            String type = jsonObject.getString( JSONKeys.TYPE );

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type,
                                                       module.name(),
                                                       module.findVisibleEntityTypes()
                                                           .map( ModelModule.toStringFunction )
                );
            }

            Map<QualifiedName, Object> properties = new HashMap<>();
            JSONObject props = jsonObject.getJSONObject( JSONKeys.PROPERTIES );
            entityDescriptor.state().properties().forEach( propertyDescriptor -> {
                Object jsonValue;
                try
                {
                    jsonValue = props.get( propertyDescriptor.qualifiedName().name() );
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
                catch( JSONException e )
                {
                    // Value not found, default it
                    Object initialValue = propertyDescriptor.initialValue( module );
                    properties.put( propertyDescriptor.qualifiedName(), initialValue );
                    status[0] = EntityStatus.UPDATED;
                }
            } );

            Map<QualifiedName, EntityReference> associations = new HashMap<>();
            JSONObject assocs = jsonObject.getJSONObject( JSONKeys.ASSOCIATIONS );
            entityDescriptor.state().associations().forEach( associationType -> {
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
                    status[0] = EntityStatus.UPDATED;
                }
            } );

            JSONObject manyAssocs = jsonObject.getJSONObject( JSONKeys.MANY_ASSOCIATIONS );
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<>();
            entityDescriptor.state().manyAssociations().forEach( manyAssociationType -> {
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

            JSONObject namedAssocs = jsonObject.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS );
            Map<QualifiedName, Map<String, EntityReference>> namedAssociations = new HashMap<>();
            entityDescriptor.state().namedAssociations().forEach( namedAssociationType -> {
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
            }  );

            return new DefaultEntityState( version,
                                           modified,
                                           EntityReference.parseEntityReference( identity ),
                                           status[0],
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
        try (Reader reader = mapEntityStore.get( EntityReference.parseEntityReference( id ) ))
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
