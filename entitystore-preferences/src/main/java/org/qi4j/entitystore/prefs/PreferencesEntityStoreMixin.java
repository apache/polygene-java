/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entitystore.prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * @see Preferences
 */
public class PreferencesEntityStoreMixin
    implements Activatable, EntityStore, EntityStoreSPI
{
    @This
    private EntityStoreSPI entityStoreSpi;

    @Uses
    private ServiceDescriptor descriptor;

    @Structure
    private Application application;

    private Preferences root;
    protected String uuid;
    private int count;
    public Logger logger;
    public ScheduledThreadPoolExecutor reloadExecutor;

    public void activate()
        throws Exception
    {
        root = getApplicationRoot();
        logger = LoggerFactory.getLogger( PreferencesEntityStoreService.class.getName() );
        logger.info( "Preferences store:" + root.absolutePath() );
        uuid = UUID.randomUUID().toString() + "-";

        // Reload underlying store every 60 seconds
        reloadExecutor = new ScheduledThreadPoolExecutor( 1 );
        reloadExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy( false );
        reloadExecutor.scheduleAtFixedRate( new Runnable()
        {
            public void run()
            {
                try
                {
                    synchronized( root )
                    {
                        root.sync();
                    }
                }
                catch( BackingStoreException e )
                {
                    logger.warn( "Could not reload preferences", e );
                }
            }
        }, 0, 60, TimeUnit.SECONDS );
    }

    private Preferences getApplicationRoot()
    {
        PreferencesEntityStoreInfo storeInfo = descriptor.metaInfo( PreferencesEntityStoreInfo.class );

        Preferences preferences;
        if( storeInfo == null )
        {
            // Default to use system root + application name
            preferences = Preferences.systemRoot();
            String name = application.name();
            preferences = preferences.node( name );
        }
        else
        {
            preferences = storeInfo.getRootNode();
        }

        return preferences;
    }

    public void passivate()
        throws Exception
    {
        reloadExecutor.shutdown();
        reloadExecutor.awaitTermination( 10, TimeUnit.SECONDS );
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, ModuleSPI module )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module, usecase );
    }

    public Input<EntityState, EntityStoreException> entityStates( final ModuleSPI module )
    {
        return new Input<EntityState, EntityStoreException>()
        {
           @Override
           public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super EntityState, ReceiverThrowableType> output) throws EntityStoreException, ReceiverThrowableType
           {
                output.receiveFrom( new Sender<EntityState, EntityStoreException>()
                {
                   @Override
                   public <ReceiverThrowableType extends Throwable> void sendTo(Receiver<? super EntityState, ReceiverThrowableType> receiver) throws ReceiverThrowableType, EntityStoreException
                   {
                        UsecaseBuilder builder = UsecaseBuilder.buildUsecase( "qi4j.entitystore.preferences.visit" );
                        Usecase visitUsecase = builder.with( CacheOptions.NEVER ).newUsecase();
                        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(),
                                                                                                   module, visitUsecase
                        );

                        try
                        {
                            String[] identities = root.childrenNames();
                            for( String identity : identities )
                            {
                                EntityState entityState = uow.getEntityState( EntityReference.parseEntityReference( identity ) );
                                receiver.receive( entityState );
                            }
                        }
                        catch( BackingStoreException e )
                        {
                            throw new EntityStoreException( e );
                        }
                    }
                });
            }
        };
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor );
    }

    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        try
        {
            DefaultEntityStoreUnitOfWork desuw = (DefaultEntityStoreUnitOfWork) unitOfWork;

            ModuleSPI module = desuw.module();

            if( !root.nodeExists( identity.identity() ) )
            {
                throw new NoSuchEntityException( identity );
            }

            Preferences entityPrefs = root.node( identity.identity() );

            String type = entityPrefs.get( "type", null );
            EntityStatus status = EntityStatus.LOADED;

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type );
            }

            Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();
            if( !entityDescriptor.state().properties().isEmpty() )
            {
                Preferences propsPrefs = entityPrefs.node( "properties" );
                for( PropertyTypeDescriptor propertyDescriptor : entityDescriptor.state()
                    .<PropertyTypeDescriptor>properties() )
                {
                    if( propertyDescriptor.qualifiedName().name().equals( "identity" ) )
                    {
                        // Fake identity property
                        properties.put( propertyDescriptor.qualifiedName(), identity.identity() );
                        continue;
                    }

                    ValueType propertyType = propertyDescriptor.propertyType().type();
                    if( propertyType.isNumber() )
                    {
                        if( propertyType.type().name().equals( "java.lang.Long" ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(),
                                            this.getNumber( propsPrefs, propertyDescriptor, new NumberParser<Long>()
                                            {
                                                public Long parse( String str )
                                                {
                                                    return Long.parseLong( str );
                                                }
                                            } ) );
                        }
                        else if( propertyType.type().name().equals( "java.lang.Integer" ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(),
                                            this.getNumber( propsPrefs, propertyDescriptor, new NumberParser<Integer>()
                                            {
                                                public Integer parse( String str )
                                                {
                                                    return Integer.parseInt( str );
                                                }
                                            } ) );
                        }
                        else if( propertyType.type().name().equals( "java.lang.Double" ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(),
                                            this.getNumber( propsPrefs, propertyDescriptor, new NumberParser<Double>()
                                            {
                                                public Double parse( String str )
                                                {
                                                    return Double.parseDouble( str );
                                                }
                                            } ) );
                        }
                        else if( propertyType.type().name().equals( "java.lang.Float" ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(),
                                            this.getNumber( propsPrefs, propertyDescriptor, new NumberParser<Float>()
                                            {
                                                public Float parse( String str )
                                                {
                                                    return Float.parseFloat( str );
                                                }
                                            } ) );
                        }
                        else
                        {
                            // Load as string even though it's a number
                            String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(), "null" );
                            json = "[" + json + "]";
                            JSONTokener tokener = new JSONTokener( json );
                            JSONArray array = (JSONArray) tokener.nextValue();
                            Object jsonValue = array.get( 0 );
                            Object value;
                            if( jsonValue == JSONObject.NULL )
                            {
                                value = null;
                            }
                            else
                            {
                                value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
                            }
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
                    else if( propertyType.isBoolean() )
                    {
                       Boolean initialValue = (Boolean) propertyDescriptor.initialValue();
                       properties.put( propertyDescriptor.qualifiedName(),
                                        propsPrefs.getBoolean( propertyDescriptor.qualifiedName().name(),
                                              initialValue == null ? false : initialValue) );
                    }
                    else if( propertyType.isValue() )
                    {
                        String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(), "null" );
                        JSONTokener tokener = new JSONTokener( json );
                        Object composite = tokener.nextValue();
                        if( composite.equals( JSONObject.NULL ) )
                        {
                            properties.put( propertyDescriptor.qualifiedName(), null );
                        }
                        else
                        {
                            Object value = propertyType.fromJSON( composite, module );
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
                    else if( propertyType.isString() )
                    {
                        String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(),
                                                      (String) propertyDescriptor
                                                          .initialValue() );
                        if( json == null )
                        {
                            properties.put( propertyDescriptor.qualifiedName(), null );
                        }
                        else
                        {
                            Object value = propertyType.fromJSON( json, module );
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
                    else
                    {
                        String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(), "null" );
                        json = "[" + json + "]";
                        JSONTokener tokener = new JSONTokener( json );
                        JSONArray array = (JSONArray) tokener.nextValue();
                        Object jsonValue = array.get( 0 );
                        Object value;
                        if( jsonValue == JSONObject.NULL )
                        {
                            value = null;
                        }
                        else
                        {
                            value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
                        }
                        properties.put( propertyDescriptor.qualifiedName(), value );
                    }
                }
            }

            // Associations
            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            if( !entityDescriptor.state().associations().isEmpty() )
            {
                Preferences assocs = entityPrefs.node( "associations" );
                for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
                {
                    String associatedEntity = assocs.get( associationType.qualifiedName().name(), null );
                    EntityReference value = associatedEntity == null ? null : EntityReference.parseEntityReference(
                        associatedEntity );
                    associations.put( associationType.qualifiedName(), value );
                }
            }

            // ManyAssociations
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            if( !entityDescriptor.state().manyAssociations().isEmpty() )
            {
                Preferences manyAssocs = entityPrefs.node( "manyassociations" );
                for( ManyAssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
                {
                    List<EntityReference> references = new ArrayList<EntityReference>();
                    String entityReferences = manyAssocs.get( manyAssociationType.qualifiedName().name(), null );
                    if( entityReferences == null )
                    {
                        // ManyAssociation not found, default to empty one
                        manyAssociations.put( manyAssociationType.qualifiedName(), references );
                    }
                    else
                    {
                        String[] refs = entityReferences.split( "\n" );
                        for( String ref : refs )
                        {
                            EntityReference value = ref == null ? null : EntityReference.parseEntityReference( ref );
                            references.add( value );
                        }
                        manyAssociations.put( manyAssociationType.qualifiedName(), references );
                    }
                }
            }

            return new DefaultEntityState( desuw,
                                           entityPrefs.get( "version", "" ),
                                           entityPrefs.getLong( "modified", System.currentTimeMillis() ),
                                           identity,
                                           status,
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations
            );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public StateCommitter applyChanges( EntityStoreUnitOfWork unitofwork, final Iterable<EntityState> state, final String version, final long lastModified )
    {
        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    synchronized( root )
                    {
                        for( EntityState entityState : state )
                        {
                            DefaultEntityState state = (DefaultEntityState) entityState;
                            if( state.status().equals( EntityStatus.NEW ) )
                            {
                                Preferences entityPrefs = root.node( state.identity().identity() );
                                writeEntityState( state, entityPrefs, version, lastModified );
                            }
                            else if( state.status().equals( EntityStatus.UPDATED ) )
                            {
                                Preferences entityPrefs = root.node( state.identity().identity() );
                                writeEntityState( state, entityPrefs, version, lastModified );
                            }
                            else if( state.status().equals( EntityStatus.REMOVED ) )
                            {
                                root.node( state.identity().identity() ).removeNode();
                            }
                        }
                        root.flush();
                    }
                }
                catch( BackingStoreException e )
                {
                    throw new EntityStoreException( e );
                }
            }

            public void cancel()
            {
            }
        };
    }

    protected void writeEntityState( DefaultEntityState state, Preferences entityPrefs, String identity, long lastModified )
        throws EntityStoreException
    {
        try
        {
            // Store into Preferences API
            EntityType entityType = state.entityDescriptor().entityType();
            entityPrefs.put( "type", state.entityDescriptor().entityType().type().name() );
            entityPrefs.put( "version", identity );
            entityPrefs.putLong( "modified", lastModified );

            // Properties
            Preferences propsPrefs = entityPrefs.node( "properties" );
            for( PropertyType propertyType : entityType.properties() )
            {
                if( propertyType.qualifiedName().name().equals( "identity" ) )
                {
                    continue; // Skip Identity.identity()
                }

                Object value = state.properties().get( propertyType.qualifiedName() );

                if( value == null )
                {
                    propsPrefs.remove( propertyType.qualifiedName().name() );
                }
                else
                {
                    if( propertyType.type().isNumber() )
                    {
                        if( propertyType.type().type().name().equals( "java.lang.Long" ) )
                        {
                            propsPrefs.putLong( propertyType.qualifiedName().name(), (Long) value );
                        }
                        else if( propertyType.type().type().name().equals( "java.lang.Integer" ) )
                        {
                            propsPrefs.putInt( propertyType.qualifiedName().name(), (Integer) value );
                        }
                        else if( propertyType.type().type().name().equals( "java.lang.Double" ) )
                        {
                            propsPrefs.putDouble( propertyType.qualifiedName().name(), (Double) value );
                        }
                        else if( propertyType.type().type().name().equals( "java.lang.Float" ) )
                        {
                            propsPrefs.putFloat( propertyType.qualifiedName().name(), (Float) value );
                        }
                        else
                        {
                            // Store as string even though it's a number
                            JSONStringer json = new JSONStringer();
                            json.array();
                            propertyType.type().toJSON( value, json );
                            json.endArray();
                            String jsonString = json.toString();
                            jsonString = jsonString.substring( 1, jsonString.length() - 1 );
                            propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                        }
                    }
                    else if( propertyType.type().isBoolean() )
                    {
                        propsPrefs.putBoolean( propertyType.qualifiedName().name(), (Boolean) value );
                    }
                    else if( propertyType.type().isValue() )
                    {
                        JSONStringer json = new JSONStringer();
                        propertyType.type().toJSON( value, json );
                        String jsonString = json.toString();
                        propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                    }
                    else if( propertyType.type().isString() )
                    {
                        JSONStringer json = new JSONStringer();
                        json.array();
                        propertyType.type().toJSON( value, json );
                        json.endArray();
                        String jsonString = json.toString();
                        jsonString = jsonString.substring( 2, jsonString.length() - 2 );
                        propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                    }
                    else
                    {
                        JSONStringer json = new JSONStringer();
                        json.array();
                        propertyType.type().toJSON( value, json );
                        json.endArray();
                        String jsonString = json.toString();
                        jsonString = jsonString.substring( 1, jsonString.length() - 1 );
                        propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                    }
                }
            }

            // Associations
            if( !entityType.associations().isEmpty() )
            {
                Preferences assocsPrefs = entityPrefs.node( "associations" );
                for( AssociationType associationType : entityType.associations() )
                {
                    EntityReference ref = state.getAssociation( associationType.qualifiedName() );
                    if( ref == null )
                    {
                        assocsPrefs.remove( associationType.qualifiedName().name() );
                    }
                    else
                    {
                        assocsPrefs.put( associationType.qualifiedName().name(), ref.identity() );
                    }
                }
            }

            // ManyAssociations
            if( !entityType.manyAssociations().isEmpty() )
            {
                Preferences manyAssocsPrefs = entityPrefs.node( "manyassociations" );
                for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
                {
                    String manyAssocs = "";
                    ManyAssociationState manyAssoc = state.getManyAssociation( manyAssociationType.qualifiedName() );
                    for( EntityReference entityReference : manyAssoc )
                    {
                        if( manyAssocs.length() > 0 )
                        {
                            manyAssocs += "\n";
                        }
                        manyAssocs += entityReference.identity();
                    }
                    manyAssocsPrefs.put( manyAssociationType.qualifiedName().name(), manyAssocs );
                }
            }
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    private interface NumberParser<T>
    {
        T parse( String str );
    }

    private <T> T getNumber( Preferences prefs, PropertyTypeDescriptor pDesc, NumberParser<T> parser )
    {
        Object initialValue = pDesc.initialValue();
        String str = prefs.get( pDesc.qualifiedName().name(), initialValue == null ? null : initialValue.toString() );
        T result = null;
        if( str != null )
        {
            result = parser.parse( str );
        }
        return result;
    }

}
