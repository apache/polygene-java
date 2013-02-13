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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
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
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.functional.Iterables.first;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * <p>@see Preferences</p>
 * <p>ManyAssociation is stored as string, one identity string per line</p>
 * <p>Nested ValuesComposites, Collections and Maps are stored using available ValueSerialization service.</p>
 */
public class PreferencesEntityStoreMixin
    implements ServiceActivation, EntityStore, EntityStoreSPI
{
    @This
    private EntityStoreSPI entityStoreSpi;

    @Uses
    private ServiceDescriptor descriptor;

    @Structure
    private Application application;

    @Service
    @Tagged( ValueSerialization.Formats.JSON )
    private ValueSerialization valueSerialization;

    private Preferences root;
    protected String uuid;
    private int count;
    public Logger logger;
    public ScheduledThreadPoolExecutor reloadExecutor;

    @Override
    public void activateService()
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
            @Override
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
            preferences = storeInfo.rootNode();
        }

        return preferences;
    }

    @Override
    public void passivateService()
        throws Exception
    {
        reloadExecutor.shutdown();
        reloadExecutor.awaitTermination( 10, TimeUnit.SECONDS );
    }

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module, long currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module, usecase, currentTime );
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
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super EntityState, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, EntityStoreException
                    {
                        UsecaseBuilder builder = UsecaseBuilder.buildUsecase( "qi4j.entitystore.preferences.visit" );
                        Usecase visitUsecase = builder.withMetaInfo( CacheOptions.NEVER ).newUsecase();
                        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(),
                                                                                                   module, visitUsecase, System
                            .currentTimeMillis() );

                        try
                        {
                            String[] identities = root.childrenNames();
                            for( String identity : identities )
                            {
                                EntityState entityState = uow.entityStateOf( EntityReference.parseEntityReference( identity ) );
                                receiver.receive( entityState );
                            }
                        }
                        catch( BackingStoreException e )
                        {
                            throw new EntityStoreException( e );
                        }
                    }
                } );
            }
        };
    }

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor );
    }

    @Override
    public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        try
        {
            DefaultEntityStoreUnitOfWork desuw = (DefaultEntityStoreUnitOfWork) unitOfWork;

            Module module = desuw.module();

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
            Preferences propsPrefs = null;
            for( PropertyDescriptor persistentPropertyDescriptor : entityDescriptor.state().properties() )
            {
                if( persistentPropertyDescriptor.qualifiedName().name().equals( "identity" ) )
                {
                    // Fake identity property
                    properties.put( persistentPropertyDescriptor.qualifiedName(), identity.identity() );
                    continue;
                }

                if( propsPrefs == null )
                {
                    propsPrefs = entityPrefs.node( "properties" );
                }

                ValueType propertyType = persistentPropertyDescriptor.valueType();
                Class<?> mainType = propertyType.mainType();
                if( Number.class.isAssignableFrom( mainType ) )
                {
                    if( mainType.equals( Long.class ) )
                    {
                        properties.put( persistentPropertyDescriptor.qualifiedName(),
                                        this.getNumber( propsPrefs, persistentPropertyDescriptor, new NumberParser<Long>()
                                        {
                                            @Override
                                            public Long parse( String str )
                                            {
                                                return Long.parseLong( str );
                                            }
                                        } ) );
                    }
                    else if( mainType.equals( Integer.class ) )
                    {
                        properties.put( persistentPropertyDescriptor.qualifiedName(),
                                        this.getNumber( propsPrefs, persistentPropertyDescriptor, new NumberParser<Integer>()
                                        {
                                            @Override
                                            public Integer parse( String str )
                                            {
                                                return Integer.parseInt( str );
                                            }
                                        } ) );
                    }
                    else if( mainType.equals( Double.class ) )
                    {
                        properties.put( persistentPropertyDescriptor.qualifiedName(),
                                        this.getNumber( propsPrefs, persistentPropertyDescriptor, new NumberParser<Double>()
                                        {
                                            @Override
                                            public Double parse( String str )
                                            {
                                                return Double.parseDouble( str );
                                            }
                                        } ) );
                    }
                    else if( mainType.equals( Float.class ) )
                    {
                        properties.put( persistentPropertyDescriptor.qualifiedName(),
                                        this.getNumber( propsPrefs, persistentPropertyDescriptor, new NumberParser<Float>()
                                        {
                                            @Override
                                            public Float parse( String str )
                                            {
                                                return Float.parseFloat( str );
                                            }
                                        } ) );
                    }
                    else
                    {
                        // Load as string even though it's a number
                        String json = propsPrefs.get( persistentPropertyDescriptor.qualifiedName().name(), null );
                        Object value;
                        if( json == null )
                        {
                            value = null;
                        }
                        else
                        {
                            value = valueSerialization.deserialize( persistentPropertyDescriptor.valueType(), json );
                        }
                        properties.put( persistentPropertyDescriptor.qualifiedName(), value );
                    }
                }
                else if( mainType.equals( Boolean.class ) )
                {
                    Boolean initialValue = (Boolean) persistentPropertyDescriptor.initialValue( module );
                    properties.put( persistentPropertyDescriptor.qualifiedName(),
                                    propsPrefs.getBoolean( persistentPropertyDescriptor.qualifiedName().name(),
                                                           initialValue == null ? false : initialValue ) );
                }
                else if( propertyType instanceof ValueCompositeType ||
                         propertyType instanceof MapType ||
                         propertyType instanceof CollectionType ||
                         propertyType instanceof EnumType )
                {
                    String json = propsPrefs.get( persistentPropertyDescriptor.qualifiedName().name(), null );
                    Object value;
                    if( json == null )
                    {
                        value = null;
                    }
                    else
                    {
                        value = valueSerialization.deserialize( persistentPropertyDescriptor.valueType(), json );
                    }
                    properties.put( persistentPropertyDescriptor.qualifiedName(), value );
                }
                else
                {
                    String json = propsPrefs.get( persistentPropertyDescriptor.qualifiedName().name(), null );
                    if( json == null )
                    {
                        if( persistentPropertyDescriptor.initialValue( module ) != null )
                        {
                            properties.put( persistentPropertyDescriptor.qualifiedName(), persistentPropertyDescriptor.initialValue( module ) );
                        }
                        else
                        {
                            properties.put( persistentPropertyDescriptor.qualifiedName(), null );
                        }
                    }
                    else
                    {
                        Object value = valueSerialization.deserialize( propertyType, json );
                        properties.put( persistentPropertyDescriptor.qualifiedName(), value );
                    }
                }
            }

            // Associations
            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            Preferences assocs = null;
            for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
            {
                if( assocs == null )
                {
                    assocs = entityPrefs.node( "associations" );
                }

                String associatedEntity = assocs.get( associationType.qualifiedName().name(), null );
                EntityReference value = associatedEntity == null ? null : EntityReference.parseEntityReference(
                    associatedEntity );
                associations.put( associationType.qualifiedName(), value );
            }

            // ManyAssociations
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            Preferences manyAssocs = null;
            for( AssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
            {
                if( manyAssocs == null )
                {
                    manyAssocs = entityPrefs.node( "manyassociations" );
                }

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

            return new DefaultEntityState( desuw,
                                           entityPrefs.get( "version", "" ),
                                           entityPrefs.getLong( "modified", unitOfWork.currentTime() ),
                                           identity,
                                           status,
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations
            );
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( e );
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public StateCommitter applyChanges( final EntityStoreUnitOfWork unitofwork, final Iterable<EntityState> state )
    {
        return new StateCommitter()
        {
            @Override
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
                                writeEntityState( state, entityPrefs, unitofwork.identity(), unitofwork.currentTime() );
                            }
                            else if( state.status().equals( EntityStatus.UPDATED ) )
                            {
                                Preferences entityPrefs = root.node( state.identity().identity() );
                                writeEntityState( state, entityPrefs, unitofwork.identity(), unitofwork.currentTime() );
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

            @Override
            public void cancel()
            {
            }
        };
    }

    protected void writeEntityState( DefaultEntityState state,
                                     Preferences entityPrefs,
                                     String identity,
                                     long lastModified
    )
        throws EntityStoreException
    {
        try
        {
            // Store into Preferences API
            entityPrefs.put( "type", first( state.entityDescriptor().types() ).getName() );
            entityPrefs.put( "version", identity );
            entityPrefs.putLong( "modified", lastModified );

            // Properties
            Preferences propsPrefs = entityPrefs.node( "properties" );
            for( PropertyDescriptor persistentProperty : state.entityDescriptor().state().properties() )
            {
                if( persistentProperty.qualifiedName().name().equals( "identity" ) )
                {
                    continue; // Skip Identity.identity()
                }

                Object value = state.properties().get( persistentProperty.qualifiedName() );

                if( value == null )
                {
                    propsPrefs.remove( persistentProperty.qualifiedName().name() );
                }
                else
                {
                    ValueType valueType = persistentProperty.valueType();
                    Class<?> mainType = valueType.mainType();
                    if( Number.class.isAssignableFrom( mainType ) )
                    {
                        if( mainType.equals( Long.class ) )
                        {
                            propsPrefs.putLong( persistentProperty.qualifiedName().name(), (Long) value );
                        }
                        else if( mainType.equals( Integer.class ) )
                        {
                            propsPrefs.putInt( persistentProperty.qualifiedName().name(), (Integer) value );
                        }
                        else if( mainType.equals( Double.class ) )
                        {
                            propsPrefs.putDouble( persistentProperty.qualifiedName().name(), (Double) value );
                        }
                        else if( mainType.equals( Float.class ) )
                        {
                            propsPrefs.putFloat( persistentProperty.qualifiedName().name(), (Float) value );
                        }
                        else
                        {
                            // Store as string even though it's a number
                            String jsonString = valueSerialization.serialize( value );
                            propsPrefs.put( persistentProperty.qualifiedName().name(), jsonString );
                        }
                    }
                    else if( mainType.equals( Boolean.class ) )
                    {
                        propsPrefs.putBoolean( persistentProperty.qualifiedName().name(), (Boolean) value );
                    }
                    else if( valueType instanceof ValueCompositeType ||
                             valueType instanceof MapType ||
                             valueType instanceof CollectionType ||
                             valueType instanceof EnumType )
                    {
                        String jsonString = valueSerialization.serialize( value );
                        propsPrefs.put( persistentProperty.qualifiedName().name(), jsonString );
                    }
                    else
                    {
                        String jsonString = valueSerialization.serialize( value );
                        propsPrefs.put( persistentProperty.qualifiedName().name(), jsonString );
                    }
                }
            }

            // Associations
            if( !state.associations().isEmpty() )
            {
                Preferences assocsPrefs = entityPrefs.node( "associations" );
                for( Map.Entry<QualifiedName, EntityReference> association : state.associations().entrySet() )
                {
                    if( association.getValue() == null )
                    {
                        assocsPrefs.remove( association.getKey().name() );
                    }
                    else
                    {
                        assocsPrefs.put( association.getKey().name(), association.getValue().identity() );
                    }
                }
            }

            // ManyAssociations
            if( !state.manyAssociations().isEmpty() )
            {
                Preferences manyAssocsPrefs = entityPrefs.node( "manyassociations" );
                for( Map.Entry<QualifiedName, List<EntityReference>> manyAssociations : state.manyAssociations()
                    .entrySet() )
                {
                    StringBuilder manyAssocs = new StringBuilder();
                    for( EntityReference entityReference : manyAssociations.getValue() )
                    {
                        if( manyAssocs.length() > 0 )
                        {
                            manyAssocs.append( "\n" );
                        }
                        manyAssocs.append( entityReference.identity() );
                    }
                    if( manyAssocs.length() > 0 )
                    {
                        manyAssocsPrefs.put( manyAssociations.getKey().name(), manyAssocs.toString() );
                    }
                }
            }
        }
        catch( ValueSerializationException e )
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

    private <T> T getNumber( Preferences prefs, PropertyDescriptor pDesc, NumberParser<T> parser )
    {
        Object initialValue = pDesc.initialValue( null );
        String str = prefs.get( pDesc.qualifiedName().name(), initialValue == null ? null : initialValue.toString() );
        T result = null;
        if( str != null )
        {
            result = parser.parse( str );
        }
        return result;
    }
}
