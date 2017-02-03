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

import java.time.Instant;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.serialization.javaxjson.JavaxJson;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.serialization.JsonSerialization;

/**
 * Standard JSON implementation of EntityState.
 */
public final class JSONEntityState
    implements EntityState
{
    private static final String[] CLONE_NAMES =
        {
            JSONKeys.IDENTITY,
            JSONKeys.APPLICATION_VERSION,
            JSONKeys.TYPE,
            JSONKeys.VERSION,
            JSONKeys.MODIFIED
        };

    private final ModuleDescriptor module;
    private final String version;
    private final EntityReference reference;
    private final EntityDescriptor entityDescriptor;
    private final JsonSerialization serialization;

    private EntityStatus status;
    private Instant lastModified;
    private JsonObject state;

    /* package */ JSONEntityState( ModuleDescriptor module,
                                   JsonSerialization serialization,
                                   String version,
                                   Instant lastModified,
                                   EntityReference reference,
                                   EntityStatus status,
                                   EntityDescriptor entityDescriptor,
                                   JsonObject state
    )
    {
        this.module = module;
        this.serialization = serialization;
        this.version = version;
        this.lastModified = lastModified;
        this.reference = reference;
        this.status = status;
        this.entityDescriptor = entityDescriptor;
        this.state = state;
    }

    // EntityState implementation
    @Override
    public final String version()
    {
        return version;
    }

    @Override
    public Instant lastModified()
    {
        return lastModified;
    }

    @Override
    public EntityReference entityReference()
    {
        return reference;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        try
        {
            JsonValue json = state.getJsonObject( JSONKeys.PROPERTIES ).get( stateName.name() );
            if( json == null || JsonValue.NULL.equals( json ) )
            {
                return null;
            }
            else
            {
                // TODO This rely on _type explicitely :(
                // Needed because of this mess that is JsonEntityState
                ValueType propertyValueType = null;
                if( json.getValueType() == JsonValue.ValueType.OBJECT )
                {
                    String typeInfo = ( (JsonObject) json ).getString( "_type", null );
                    if( typeInfo != null )
                    {
                        ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo );
                        if( valueDescriptor != null )
                        {
                            propertyValueType = valueDescriptor.valueType();
                        }
                    }
                }
                if( propertyValueType == null )
                {
                    PropertyDescriptor descriptor = entityDescriptor.state()
                                                                    .findPropertyModelByQualifiedName( stateName );
                    if( descriptor != null )
                    {
                        propertyValueType = descriptor.valueType();
                    }
                }
                if( propertyValueType == null )
                {
                    return null;
                }
                return serialization.fromJson( module, propertyValueType, json );
            }
        }
        catch( SerializationException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        try
        {
            JsonValue jsonValue = serialization.toJson( newValue );
            stateCloneWithProperty( stateName.name(), jsonValue );
            markUpdated();
        }
        catch( SerializationException e )
        {
            throw new EntityStoreException( "Unable to set property " + stateName + " value " + newValue, e );
        }
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        String jsonValue = state.getJsonObject( JSONKeys.ASSOCIATIONS ).getString( stateName.name(), null );
        if( jsonValue == null )
        {
            return null;
        }
        return EntityReference.parseEntityReference( jsonValue );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        stateCloneWithAssociation( stateName.name(), newEntity );
        markUpdated();
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        return new JSONManyAssociationState( this, stateName.name() );
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        return new JSONNamedAssociationState( this, stateName.name() );
    }

    @Override
    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    @Override
    public EntityStatus status()
    {
        return status;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return entityDescriptor.isAssignableTo( type );
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        return entityDescriptor;
    }

    public JsonObject state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return reference + "(" + state + ")";
    }

    void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }

    void stateCloneWithVersionAndModified( String version, Instant lastModified )
    {
        JsonObjectBuilder builder = JavaxJson.toBuilder( state );
        builder.add( JSONKeys.VERSION, version );
        builder.add( JSONKeys.MODIFIED, lastModified.toEpochMilli() );
        state = builder.build();
    }

    void stateCloneWithProperty( String stateName, JsonValue value )
    {
        JsonObjectBuilder builder = stateShallowClone();
        JsonObjectBuilder propertiesBuilder = JavaxJson.toBuilder( state.getJsonObject( JSONKeys.PROPERTIES ) );
        if( value == null )
        {
            propertiesBuilder.add( stateName, JsonValue.NULL );
        }
        else
        {
            propertiesBuilder.add( stateName, value );
        }
        builder.add( JSONKeys.PROPERTIES, propertiesBuilder.build() );
        builder.add( JSONKeys.ASSOCIATIONS, state.get( JSONKeys.ASSOCIATIONS ) );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, state.get( JSONKeys.MANY_ASSOCIATIONS ) );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, state.get( JSONKeys.NAMED_ASSOCIATIONS ) );
        state = builder.build();
    }

    void stateCloneWithAssociation( String stateName, EntityReference ref )
    {
        JsonObjectBuilder builder = stateShallowClone();
        JsonObjectBuilder assocBuilder = JavaxJson.toBuilder( state.getJsonObject( JSONKeys.ASSOCIATIONS ) );
        if( ref == null )
        {
            assocBuilder.add( stateName, JsonValue.NULL );
        }
        else
        {
            assocBuilder.add( stateName, ref.identity().toString() );
        }
        builder.add( JSONKeys.PROPERTIES, state.get( JSONKeys.PROPERTIES ) );
        builder.add( JSONKeys.ASSOCIATIONS, assocBuilder.build() );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, state.get( JSONKeys.MANY_ASSOCIATIONS ) );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, state.get( JSONKeys.NAMED_ASSOCIATIONS ) );
        state = builder.build();
    }

    void stateCloneAddManyAssociation( int idx, String stateName, EntityReference ref )
    {
        JsonObjectBuilder builder = stateShallowClone();
        JsonObjectBuilder manyAssociations = Json.createObjectBuilder();
        JsonObject previousManyAssociations = state.getJsonObject( JSONKeys.MANY_ASSOCIATIONS );
        for( Map.Entry<String, JsonValue> previousManyAssociation : previousManyAssociations.entrySet() )
        {
            String key = previousManyAssociation.getKey();
            if( !key.equals( stateName ) )
            {
                manyAssociations.add( key, previousManyAssociation.getValue() );
            }
        }
        JsonValue previousReferences = previousManyAssociations.get( stateName );
        JsonArrayBuilder references = Json.createArrayBuilder();
        String newRef = ref.identity().toString();
        if( previousReferences == null || previousReferences.getValueType() != JsonValue.ValueType.ARRAY )
        {
            references.add( newRef );
        }
        else
        {
            JsonArray previousReferencesArray = (JsonArray) previousReferences;
            boolean insert = !previousReferencesArray.contains( newRef );
            for( int i = 0; i < previousReferencesArray.size(); i++ )
            {
                if( insert && i == idx )
                {
                    references.add( newRef );
                }
                references.add( previousReferencesArray.getString( i ) );
            }
            if( insert && idx >= previousReferencesArray.size() )
            {
                references.add( newRef );
            }
        }
        manyAssociations.add( stateName, references.build() );
        builder.add( JSONKeys.PROPERTIES, state.get( JSONKeys.PROPERTIES ) );
        builder.add( JSONKeys.ASSOCIATIONS, state.get( JSONKeys.ASSOCIATIONS ) );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, manyAssociations.build() );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, state.get( JSONKeys.NAMED_ASSOCIATIONS ) );
        state = builder.build();
    }

    void stateCloneRemoveManyAssociation( String stateName, EntityReference ref )
    {
        String stringRef = ref.identity().toString();
        JsonObjectBuilder builder = stateShallowClone();
        JsonObjectBuilder manyAssociations = Json.createObjectBuilder();
        JsonObject previousManyAssociations = state.getJsonObject( JSONKeys.MANY_ASSOCIATIONS );
        for( Map.Entry<String, JsonValue> previousManyAssociation : previousManyAssociations.entrySet() )
        {
            String key = previousManyAssociation.getKey();
            if( !key.equals( stateName ) )
            {
                manyAssociations.add( key, previousManyAssociation.getValue() );
            }
        }
        JsonValue previousReferences = previousManyAssociations.get( stateName );
        JsonArrayBuilder references = Json.createArrayBuilder();
        if( previousReferences != null && previousReferences.getValueType() == JsonValue.ValueType.ARRAY )
        {
            JsonArray previousReferencesArray = (JsonArray) previousReferences;
            for( int idx = 0; idx < previousReferencesArray.size(); idx++ )
            {
                String previousRef = previousReferencesArray.getString( idx );
                if( !stringRef.equals( previousRef ) )
                {
                    references.add( previousRef );
                }
            }
        }
        manyAssociations.add( stateName, references.build() );
        builder.add( JSONKeys.PROPERTIES, state.get( JSONKeys.PROPERTIES ) );
        builder.add( JSONKeys.ASSOCIATIONS, state.get( JSONKeys.ASSOCIATIONS ) );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, manyAssociations.build() );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, state.get( JSONKeys.NAMED_ASSOCIATIONS ) );
        state = builder.build();
    }

    void stateCloneAddNamedAssociation( String stateName, String name, EntityReference ref )
    {
        JsonObjectBuilder builder = stateShallowClone();
        JsonObject previousNamedAssociations = state.getJsonObject( JSONKeys.NAMED_ASSOCIATIONS );
        JsonObjectBuilder namedAssociations = Json.createObjectBuilder();
        for( Map.Entry<String, JsonValue> previousNamedAssociation : previousNamedAssociations.entrySet() )
        {
            String key = previousNamedAssociation.getKey();
            if( !key.equals( stateName ) )
            {
                namedAssociations.add( key, previousNamedAssociation.getValue() );
            }
        }
        JsonValue previousReferences = previousNamedAssociations.get( stateName );
        JsonObjectBuilder references = Json.createObjectBuilder();
        String newRef = ref.identity().toString();
        if( previousReferences == null || !( previousReferences instanceof JsonObject ) )
        {
            references.add( name, newRef );
        }
        else
        {
            JsonObject previousReferencesObject = (JsonObject) previousReferences;
            for( Map.Entry<String, JsonValue> previousNamedReference : previousReferencesObject.entrySet() )
            {
                String key = previousNamedReference.getKey();
                if( !key.equals( name ) )
                {
                    references.add( key, previousNamedReference.getValue() );
                }
            }
            references.add( name, ref.identity().toString() );
        }
        namedAssociations.add( stateName, references.build() );
        builder.add( JSONKeys.PROPERTIES, state.get( JSONKeys.PROPERTIES ) );
        builder.add( JSONKeys.ASSOCIATIONS, state.get( JSONKeys.ASSOCIATIONS ) );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, state.get( JSONKeys.MANY_ASSOCIATIONS ) );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, namedAssociations.build() );
        state = builder.build();
    }

    void stateCloneRemoveNamedAssociation( String stateName, String name )
    {
        JsonObjectBuilder builder = stateShallowClone();
        JsonObjectBuilder namedAssociations = Json.createObjectBuilder();
        JsonObject previousNamedAssociations = state.getJsonObject( JSONKeys.NAMED_ASSOCIATIONS );
        for( Map.Entry<String, JsonValue> previousNamedAssociation : previousNamedAssociations.entrySet() )
        {
            String key = previousNamedAssociation.getKey();
            if( !key.equals( stateName ) )
            {
                namedAssociations.add( key, previousNamedAssociation.getValue() );
            }
        }
        JsonValue previousReferences = previousNamedAssociations.get( stateName );
        JsonObjectBuilder references = Json.createObjectBuilder();
        if( previousReferences != null && previousReferences.getValueType() == JsonValue.ValueType.OBJECT )
        {
            JsonObject previousReferencesObject = (JsonObject) previousReferences;
            for( Map.Entry<String, JsonValue> previousNamedRef : previousReferencesObject.entrySet() )
            {
                String previousName = previousNamedRef.getKey();
                if( !name.equals( previousName ) )
                {
                    references.add( previousName, previousNamedRef.getValue() );
                }
            }
        }
        namedAssociations.add( stateName, references.build() );
        builder.add( JSONKeys.PROPERTIES, state.get( JSONKeys.PROPERTIES ) );
        builder.add( JSONKeys.ASSOCIATIONS, state.get( JSONKeys.ASSOCIATIONS ) );
        builder.add( JSONKeys.MANY_ASSOCIATIONS, state.get( JSONKeys.MANY_ASSOCIATIONS ) );
        builder.add( JSONKeys.NAMED_ASSOCIATIONS, namedAssociations.build() );
        state = builder.build();
    }

    private JsonObjectBuilder stateShallowClone()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for( String cloneName : CLONE_NAMES )
        {
            JsonValue cloneValue = state.get( cloneName );
            builder.add( cloneName, cloneValue == null ? JsonValue.NULL : cloneValue );
        }
        return builder;
    }
}
