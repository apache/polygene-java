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
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entity.helpers.EntityStoreSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.util.json.JSONArray;
import org.qi4j.spi.util.json.JSONException;
import org.qi4j.spi.util.json.JSONObject;
import org.qi4j.spi.util.json.JSONTokener;
import org.qi4j.spi.util.json.JSONStringer;
import org.qi4j.spi.value.NumberType;
import org.qi4j.spi.value.BooleanType;
import org.qi4j.spi.value.StringType;
import org.qi4j.spi.value.ValueType;

/**
 * Implementation of EntityStore that is backed by the Preferences API.
 *
 * @see Preferences
 */
public class PreferencesEntityStoreMixin
    implements Activatable, EntityStore, EntityStoreSPI
{
    @This EntityStoreSPI entityStoreSpi;

    private @Uses ServiceDescriptor descriptor;
    private @Structure Application application;

    private Preferences root;
    protected String uuid;
    private int count;

    public void activate()
        throws Exception
    {
        root = getApplicationRoot();
        Logger.getLogger( PreferencesEntityStoreService.class.getName() ).info( "Preferences store:"+root.absolutePath() );
        uuid = UUID.randomUUID().toString() + "-";
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
    }

    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, ModuleSPI module )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }

    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor, ModuleSPI moduleInstance )
    {
        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), moduleInstance );

        try
        {
            String[] identities = root.childrenNames();
            for( String identity : identities )
            {
                EntityState entityState = uow.getEntityState( EntityReference.parseEntityReference(identity ));
                visitor.visitEntityState( entityState );
            }
        }
        catch( BackingStoreException e )
        {
            throw new EntityStoreException(e);
        }

        return uow;
    }

    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity, EntityDescriptor entityDescriptor )
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
            if (!entityDescriptor.state().properties().isEmpty())
            {
                Preferences propsPrefs = entityPrefs.node( "properties" );
                for( PropertyTypeDescriptor propertyDescriptor : entityDescriptor.state().<PropertyTypeDescriptor>properties() )
                {
                    ValueType propertyType = propertyDescriptor.propertyType().type();
                    if ( propertyType instanceof NumberType )
                    {
                        if (propertyType.type().name().equals("Long"))
                            properties.put( propertyDescriptor.qualifiedName(), propsPrefs.getLong( propertyDescriptor.qualifiedName().name(), (Long) propertyDescriptor.initialValue() ));
                        else if (propertyType.type().name().equals("Integer"))
                            properties.put( propertyDescriptor.qualifiedName(), propsPrefs.getInt( propertyDescriptor.qualifiedName().name(), (Integer) propertyDescriptor.initialValue() ));
                        else if (propertyType.type().name().equals("Double"))
                            properties.put( propertyDescriptor.qualifiedName(), propsPrefs.getDouble( propertyDescriptor.qualifiedName().name(), (Double) propertyDescriptor.initialValue() ));
                        else if (propertyType.type().name().equals("Float"))
                            properties.put( propertyDescriptor.qualifiedName(), propsPrefs.getFloat( propertyDescriptor.qualifiedName().name(), (Float) propertyDescriptor.initialValue() ));
                        else
                        {
                            // Load as string even though it's a number
                            String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(), "null" );
                            json = "["+json+"]";
                            JSONTokener tokener = new JSONTokener(json);
                            JSONArray array = (JSONArray) tokener.nextValue();
                            Object jsonValue = array.get( 0 );
                            Object value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
                            properties.put(propertyDescriptor.qualifiedName(), value);
                        }
                    } else if (propertyType instanceof BooleanType )
                    {
                        properties.put(propertyDescriptor.qualifiedName(), propsPrefs.getBoolean( propertyDescriptor.qualifiedName().name(), (Boolean) propertyDescriptor.initialValue() ));
                    } else if (propertyType instanceof StringType )
                    {
                        properties.put(propertyDescriptor.qualifiedName(), propsPrefs.get( propertyDescriptor.qualifiedName().name(), (String) propertyDescriptor.initialValue() ));
                    } else
                    {
                        String json = propsPrefs.get( propertyDescriptor.qualifiedName().name(), "null" );
                        json = "["+json+"]";
                        JSONTokener tokener = new JSONTokener(json);
                        JSONArray array = (JSONArray) tokener.nextValue();
                        Object jsonValue = array.get( 0 );
                        Object value;
                        if (jsonValue == JSONObject.NULL)
                            value = null;
                        else
                            value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
                        properties.put(propertyDescriptor.qualifiedName(), value);
                    }
                }
            }

            // Associations
            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            if (!entityDescriptor.state().associations().isEmpty())
            {
                Preferences assocs = entityPrefs.node( "associations" );
                for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
                {
                    String associatedEntity = assocs.get( associationType.qualifiedName().name(), null );
                    EntityReference value = associatedEntity == null ? null : EntityReference.parseEntityReference( associatedEntity );
                    associations.put( associationType.qualifiedName(), value );
                }
            }

            // ManyAssociations
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            if (!entityDescriptor.state().manyAssociations().isEmpty())
            {
                Preferences manyAssocs = entityPrefs.node( "manyassociations" );
                for( ManyAssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
                {
                    List<EntityReference> references = new ArrayList<EntityReference>();
                    String entityReferences = manyAssocs.get( manyAssociationType.qualifiedName().name(), null );
                    if (entityReferences == null)
                    {
                        // ManyAssociation not found, default to empty one
                        manyAssociations.put( manyAssociationType.qualifiedName(), references );
                    } else
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

    public StateCommitter apply( final Iterable<EntityState> state, final String version )
    {
        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    for (EntityState entityState : state)
                    {
                        DefaultEntityState state = (DefaultEntityState) entityState;
                        if (state.status().equals(EntityStatus.NEW))
                        {
                            Preferences entityPrefs = root.node( state.identity().identity() );
                            writeEntityState(state, entityPrefs, version);
                        } else if (state.status().equals(EntityStatus.UPDATED))
                        {
                            Preferences entityPrefs = root.node( state.identity().identity() );
                            writeEntityState(state, entityPrefs, version);
                        } else if (state.status().equals(EntityStatus.REMOVED))
                        {
                            root.node( state.identity().identity() ).removeNode();
                        }
                    }
                    root.flush();
                } catch( BackingStoreException e )
                {
                    throw new EntityStoreException(e);
                }
            }

            public void cancel()
            {
            }
        };
    }

    protected void writeEntityState(DefaultEntityState state, Preferences entityPrefs, String identity)
            throws EntityStoreException
    {
        try
        {
            // Store into Preferences API
            EntityType entityType = state.entityDescriptor().entityType();
            entityPrefs.put("type", state.entityDescriptor().entityType().type().name());
            entityPrefs.put( "version", identity );
            entityPrefs.putLong( "modified", state.lastModified() );

            // Properties
            Preferences propsPrefs = entityPrefs.node( "properties" );
            for( PropertyType propertyType : entityType.properties() )
            {
                Object value = state.properties().get( propertyType.qualifiedName() );

                if (value == null)
                {
                    propsPrefs.remove( propertyType.qualifiedName().name() );
                } else
                {
                    if (propertyType.type() instanceof NumberType )
                    {
                        if (propertyType.type().type().name().equals("java.lang.Long"))
                            propsPrefs.putLong( propertyType.qualifiedName().name(), (Long) value );
                        else if (propertyType.type().type().name().equals("java.lang.Integer"))
                            propsPrefs.putInt( propertyType.qualifiedName().name(), (Integer) value );
                        else if (propertyType.type().type().name().equals("java.lang.Double"))
                            propsPrefs.putDouble( propertyType.qualifiedName().name(), (Double) value );
                        else if (propertyType.type().type().name().equals("java.lang.Float"))
                            propsPrefs.putFloat( propertyType.qualifiedName().name(), (Float) value );
                        else
                        {
                            // Store as string even though it's a number
                            JSONStringer json = new JSONStringer();
                            json.array();
                            propertyType.type().toJSON( value, json );
                            json.endArray();
                            String jsonString = json.toString();
                            jsonString = jsonString.substring( 1, jsonString.length()-1 );
                            propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                        }
                    } else if (propertyType.type() instanceof BooleanType )
                    {
                        propsPrefs.putBoolean( propertyType.qualifiedName().name(), (Boolean) value );
                    } else if (propertyType.type() instanceof StringType )
                    {
                        if (value == null)
                            propsPrefs.remove( propertyType.qualifiedName().name() );
                        else
                            propsPrefs.put( propertyType.qualifiedName().name(), (String) value );
                    } else
                    {
                        JSONStringer json = new JSONStringer();
                        json.array();
                        propertyType.type().toJSON( value, json );
                        json.endArray();
                        String jsonString = json.toString();
                        jsonString = jsonString.substring( 1, jsonString.length()-1 );
                        propsPrefs.put( propertyType.qualifiedName().name(), jsonString );
                    }
                }
            }

            // Associations
            if (!entityType.associations().isEmpty())
            {
                Preferences assocsPrefs = entityPrefs.node( "associations" );
                for( AssociationType associationType : entityType.associations() )
                {
                    EntityReference ref = state.getAssociation( associationType.qualifiedName() );
                    if (ref == null)
                    {
                        assocsPrefs.remove(associationType.qualifiedName().name());
                    } else
                    {
                        assocsPrefs.put( associationType.qualifiedName().name(), ref.identity() );
                    }
                }
            }

            // ManyAssociations
            if (!entityType.manyAssociations().isEmpty())
            {
                Preferences manyAssocsPrefs = entityPrefs.node( "manyassociations" );
                for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
                {
                    String manyAssocs = "";
                    ManyAssociationState manyAssoc = state.getManyAssociation( manyAssociationType.qualifiedName() );
                    for( EntityReference entityReference : manyAssoc )
                    {
                        if (manyAssocs.length() > 0)
                            manyAssocs += "\n";
                        manyAssocs += entityReference.identity();
                    }
                    manyAssocsPrefs.put( manyAssociationType.qualifiedName().name(), manyAssocs );
                }
            }
        } catch (JSONException e)
        {
            throw new EntityStoreException("Could not store EntityState", e);
        }
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }
}
