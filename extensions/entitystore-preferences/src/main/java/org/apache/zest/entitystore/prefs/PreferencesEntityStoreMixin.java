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
package org.apache.zest.entitystore.prefs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import org.apache.zest.api.cache.CacheOptions;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.IdentityGenerator;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.time.SystemTime;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.EnumType;
import org.apache.zest.api.type.MapType;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.NoSuchEntityTypeException;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.spi.PolygeneSPI;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.EntityStoreSPI;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateCommitter;
import org.apache.zest.spi.entitystore.helpers.DefaultEntityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * <p>@see Preferences</p>
 * <p>
 * Associations are stored as the reference of the referenced Entity, ManyAssociations are stored as multi-line strings
 * (one reference per line), and NamedAssociations are stored as multi-line strings (one name on a line, reference on the
 * next line).
 * </p>
 * <p>Nested ValuesComposites, Collections and Maps are stored using available ValueSerialization service.</p>
 */
public class PreferencesEntityStoreMixin
    implements ServiceActivation, EntityStore, EntityStoreSPI
{
    @Structure
    private PolygeneSPI spi;

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

    public Logger logger;

    public ScheduledThreadPoolExecutor reloadExecutor;

    @Service
    private IdentityGenerator identityGenerator;

    @Override
    public void activateService()
        throws Exception
    {
        root = getApplicationRoot();
        logger = LoggerFactory.getLogger( PreferencesEntityStoreService.class.getName() );
        logger.info( "Preferences store:" + root.absolutePath() );

        // Reload underlying store every 60 seconds
        reloadExecutor = new ScheduledThreadPoolExecutor( 1 );
        reloadExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy( false );
        reloadExecutor.scheduleAtFixedRate( () -> {
            try
            {
                //noinspection SynchronizeOnNonFinalField
                synchronized( root )
                {
                    root.sync();
                }
            }
            catch( BackingStoreException e )
            {
                logger.warn( "Could not reload preferences", e );
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
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module, entityStoreSpi, newUnitOfWorkId(), usecase, currentTime );
    }

    @Override
    public Stream<EntityState> entityStates( final ModuleDescriptor module )
    {
        UsecaseBuilder builder = UsecaseBuilder.buildUsecase( "zest.entitystore.preferences.visit" );
        Usecase visitUsecase = builder.withMetaInfo( CacheOptions.NEVER ).newUsecase();
        EntityStoreUnitOfWork uow = newUnitOfWork( module, visitUsecase, SystemTime.now() );

        try
        {
            return Stream.of( root.childrenNames() )
                         .map( EntityReference::parseEntityReference )
                         .map( ref -> uow.entityStateOf( module, ref ) )
                         .onClose( uow::discard );
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference reference,
                                       EntityDescriptor entityDescriptor
    )
    {
        return new DefaultEntityState( unitOfWork.currentTime(), reference, entityDescriptor );
    }

    @Override
    public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork,
                                      ModuleDescriptor module,
                                      EntityReference reference
    )
    {
        try
        {
            if( !root.nodeExists( reference.identity().toString() ) )
            {
                throw new NoSuchEntityException( reference, UnknownType.class, unitOfWork.usecase() );
            }

            Preferences entityPrefs = root.node( reference.identity().toString() );

            String type = entityPrefs.get( "type", null );
            EntityStatus status = EntityStatus.LOADED;

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new NoSuchEntityTypeException( type, module.name(), module.typeLookup() );
            }

            Map<QualifiedName, Object> properties = new HashMap<>();
            final Preferences propsPrefs = entityPrefs.node( "properties" );
            entityDescriptor.state().properties().forEach(
                persistentPropertyDescriptor ->
                {
                    if( persistentPropertyDescriptor.qualifiedName().name().equals( "reference" ) )
                    {
                        // Fake reference property
                        properties.put( persistentPropertyDescriptor.qualifiedName(), reference.identity().toString() );
                    }
                    else
                    {
                        ValueType propertyType = persistentPropertyDescriptor.valueType();
                        Class<?> mainType = propertyType.mainType();
                        if( Number.class.isAssignableFrom( mainType ) )
                        {
                            if( mainType.equals( Long.class ) )
                            {
                                properties.put( persistentPropertyDescriptor.qualifiedName(),
                                                this.getNumber( propsPrefs, persistentPropertyDescriptor, LONG_PARSER ) );
                            }
                            else if( mainType.equals( Integer.class ) )
                            {
                                properties.put( persistentPropertyDescriptor.qualifiedName(),
                                                this.getNumber( propsPrefs, persistentPropertyDescriptor, INT_PARSER ) );
                            }
                            else if( mainType.equals( Double.class ) )
                            {
                                properties.put( persistentPropertyDescriptor.qualifiedName(),
                                                this.getNumber( propsPrefs, persistentPropertyDescriptor, DOUBLE_PARSER ) );
                            }
                            else if( mainType.equals( Float.class ) )
                            {
                                properties.put( persistentPropertyDescriptor.qualifiedName(),
                                                this.getNumber( propsPrefs, persistentPropertyDescriptor, FLOAT_PARSER ) );
                            }
                            else
                            {
                                // Load as string even though it's a number
                                String json = propsPrefs.get( persistentPropertyDescriptor.qualifiedName()
                                                                  .name(), null );
                                Object value;
                                if( json == null )
                                {
                                    value = null;
                                }
                                else
                                {
                                    value = valueSerialization.deserialize( module, persistentPropertyDescriptor.valueType(), json );
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
                        else if( propertyType instanceof ValueCompositeType
                                 || propertyType instanceof MapType
                                 || propertyType instanceof CollectionType
                                 || propertyType instanceof EnumType )
                        {
                            String json = propsPrefs.get( persistentPropertyDescriptor.qualifiedName().name(), null );
                            Object value;
                            if( json == null )
                            {
                                value = null;
                            }
                            else
                            {
                                value = valueSerialization.deserialize( module, persistentPropertyDescriptor.valueType(), json );
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
                                    properties.put( persistentPropertyDescriptor.qualifiedName(), persistentPropertyDescriptor
                                        .initialValue( module ) );
                                }
                                else
                                {
                                    properties.put( persistentPropertyDescriptor.qualifiedName(), null );
                                }
                            }
                            else
                            {
                                Object value = valueSerialization.deserialize( module, propertyType, json );
                                properties.put( persistentPropertyDescriptor.qualifiedName(), value );
                            }
                        }
                    }
                } );

            // Associations
            Map<QualifiedName, EntityReference> associations = new HashMap<>();
            final Preferences assocs = entityPrefs.node( "associations" );
            entityDescriptor.state().associations().forEach( associationType -> {
                String associatedEntity = assocs.get( associationType.qualifiedName().name(), null );
                EntityReference value = associatedEntity == null
                                        ? null
                                        : EntityReference.parseEntityReference( associatedEntity );
                associations.put( associationType.qualifiedName(), value );
            } );

            // ManyAssociations
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<>();
            Preferences manyAssocs = entityPrefs.node( "manyassociations" );
            entityDescriptor.state().manyAssociations().forEach( manyAssociationType -> {
                List<EntityReference> references = new ArrayList<>();
                String entityReferences = manyAssocs.get( manyAssociationType
                                                              .qualifiedName()
                                                              .name(), null );
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
                        EntityReference value = ref == null
                                                ? null
                                                : EntityReference.parseEntityReference( ref );
                        references.add( value );
                    }
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
            } );

            // NamedAssociations
            Map<QualifiedName, Map<String, EntityReference>> namedAssociations = new HashMap<>();
            Preferences namedAssocs = entityPrefs.node( "namedassociations" );
            entityDescriptor.state().namedAssociations().forEach( namedAssociationType -> {
                Map<String, EntityReference> references = new LinkedHashMap<>();
                String entityReferences = namedAssocs.get( namedAssociationType.qualifiedName().name(), null );
                if( entityReferences == null )
                {
                    // NamedAssociation not found, default to empty one
                    namedAssociations.put( namedAssociationType.qualifiedName(), references );
                }
                else
                {
                    String[] namedRefs = entityReferences.split( "\n" );
                    if( namedRefs.length % 2 != 0 )
                    {
                        throw new EntityStoreException( "Invalid NamedAssociation storage format" );
                    }
                    for( int idx = 0; idx < namedRefs.length; idx += 2 )
                    {
                        String name = namedRefs[ idx ];
                        String ref = namedRefs[ idx + 1 ];
                        references.put( name, EntityReference.parseEntityReference( ref ) );
                    }
                    namedAssociations.put( namedAssociationType.qualifiedName(), references );
                }
            } );

            return new DefaultEntityState( entityPrefs.get( "version", "" ),
                                           Instant.ofEpochMilli(entityPrefs.getLong( "modified", unitOfWork.currentTime().toEpochMilli() )),
                                           reference,
                                           status,
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations,
                                           namedAssociations
            );
        }
        catch( ValueSerializationException | BackingStoreException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public String versionOf( EntityStoreUnitOfWork unitOfWork, EntityReference reference )
    {
        try
        {
            if( !root.nodeExists( reference.identity().toString() ) )
            {
                throw new NoSuchEntityException( reference, UnknownType.class, unitOfWork.usecase() );
            }

            Preferences entityPrefs = root.node( reference.identity().toString() );
            return entityPrefs.get( "version", "" );
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
            @SuppressWarnings( "SynchronizeOnNonFinalField" )
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
                                Preferences entityPrefs = root.node( state.entityReference().identity().toString() );
                                writeEntityState( state, entityPrefs, unitofwork.identity(), unitofwork.currentTime() );
                            }
                            else if( state.status().equals( EntityStatus.UPDATED ) )
                            {
                                Preferences entityPrefs = root.node( state.entityReference().identity().toString() );
                                writeEntityState( state, entityPrefs, unitofwork.identity(), unitofwork.currentTime() );
                            }
                            else if( state.status().equals( EntityStatus.REMOVED ) )
                            {
                                root.node( state.entityReference().identity().toString() ).removeNode();
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
                                     Identity identity,
                                     Instant lastModified
    )
        throws EntityStoreException
    {
        try
        {
            // Store into Preferences API
            entityPrefs.put( "type", state.entityDescriptor().types().findFirst().get().getName() );
            entityPrefs.put( "version", identity.toString() );
            entityPrefs.putLong( "modified", lastModified.toEpochMilli() );

            // Properties
            Preferences propsPrefs = entityPrefs.node( "properties" );
            state.entityDescriptor().state().properties()
                .filter( property -> !property.qualifiedName().name().equals( "reference" ) )
                .forEach( persistentProperty ->
                          {
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
                                          propsPrefs.putInt( persistentProperty.qualifiedName()
                                                                 .name(), (Integer) value );
                                      }
                                      else if( mainType.equals( Double.class ) )
                                      {
                                          propsPrefs.putDouble( persistentProperty.qualifiedName()
                                                                    .name(), (Double) value );
                                      }
                                      else if( mainType.equals( Float.class ) )
                                      {
                                          propsPrefs.putFloat( persistentProperty.qualifiedName()
                                                                   .name(), (Float) value );
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
                                      propsPrefs.putBoolean( persistentProperty.qualifiedName()
                                                                 .name(), (Boolean) value );
                                  }
                                  else if( valueType instanceof ValueCompositeType
                                           || valueType instanceof MapType
                                           || valueType instanceof CollectionType
                                           || valueType instanceof EnumType )
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
                          } );

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
                        assocsPrefs.put( association.getKey().name(), association.getValue().identity().toString() );
                    }
                }
            }

            // ManyAssociations
            if( !state.manyAssociations().isEmpty() )
            {
                Preferences manyAssocsPrefs = entityPrefs.node( "manyassociations" );
                for( Map.Entry<QualifiedName, List<EntityReference>> manyAssociation : state.manyAssociations()
                    .entrySet() )
                {
                    StringBuilder manyAssocs = new StringBuilder();
                    for( EntityReference entityReference : manyAssociation.getValue() )
                    {
                        if( manyAssocs.length() > 0 )
                        {
                            manyAssocs.append( "\n" );
                        }
                        manyAssocs.append( entityReference.identity().toString() );
                    }
                    if( manyAssocs.length() > 0 )
                    {
                        manyAssocsPrefs.put( manyAssociation.getKey().name(), manyAssocs.toString() );
                    }
                }
            }

            // NamedAssociations
            if( !state.namedAssociations().isEmpty() )
            {
                Preferences namedAssocsPrefs = entityPrefs.node( "namedassociations" );
                for( Map.Entry<QualifiedName, Map<String, EntityReference>> namedAssociation : state.namedAssociations()
                    .entrySet() )
                {
                    StringBuilder namedAssocs = new StringBuilder();
                    for( Map.Entry<String, EntityReference> namedRef : namedAssociation.getValue().entrySet() )
                    {
                        if( namedAssocs.length() > 0 )
                        {
                            namedAssocs.append( "\n" );
                        }
                        namedAssocs.append( namedRef.getKey() ).append( "\n" ).append( namedRef.getValue().identity().toString() );
                    }
                    if( namedAssocs.length() > 0 )
                    {
                        namedAssocsPrefs.put( namedAssociation.getKey().name(), namedAssocs.toString() );
                    }
                }
            }
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected Identity newUnitOfWorkId()
    {
        return identityGenerator.generate(EntityStore.class);
    }

    private interface NumberParser<T>
    {
        T parse( String str );
    }

    private static final NumberParser<Long> LONG_PARSER = Long::parseLong;

    private static final NumberParser<Integer> INT_PARSER = Integer::parseInt;

    private static final NumberParser<Double> DOUBLE_PARSER = Double::parseDouble;

    private static final NumberParser<Float> FLOAT_PARSER = Float::parseFloat;

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

    private static class UnknownType
    {
    }
}
