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
import java.util.Objects;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.serialization.JsonSerialization;

import static org.apache.polygene.api.serialization.Serializer.Options.ALL_TYPE_INFO;

/**
 * Standard JSON implementation of EntityState.
 */
public final class JSONEntityState
    implements EntityState
{
    private final ModuleDescriptor module;
    private final String version;
    private final EntityReference reference;
    private final EntityDescriptor entityDescriptor;
    private final JsonSerialization serialization;
    private final JavaxJsonFactories jsonFactories;

    private EntityStatus status;
    private Instant lastModified;
    private JsonObject state;

    /* package */ JSONEntityState( ModuleDescriptor module,
                                   JsonSerialization serialization,
                                   JavaxJsonFactories jsonFactories,
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
        this.jsonFactories = jsonFactories;
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
            ValueType valueType = entityDescriptor.state().findPropertyModelByQualifiedName( stateName ).valueType();
            JsonValue jsonValue = state.getJsonObject( JSONKeys.VALUE ).get( stateName.name() );
            return serialization.fromJson( module, valueType, jsonValue );
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
            JsonValue jsonValue = serialization.toJson( ALL_TYPE_INFO, newValue );
            if( stateCloneWithProperty( stateName.name(), jsonValue ) )
            {
                markUpdated();
            }
        }
        catch( SerializationException e )
        {
            throw new EntityStoreException( "Unable to set property " + stateName + " value " + newValue, e );
        }
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        JsonValue associationValue = state.getJsonObject( JSONKeys.VALUE ).get( stateName.name() );
        if( associationValue == JsonValue.NULL )
        {
            return null;
        }
        return EntityReference.parseEntityReference( ( (JsonString) associationValue ).getString() );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference entityReference )
    {
        if( stateCloneWithAssociation( stateName.name(), entityReference ) )
        {
            markUpdated();
        }
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        return new JSONManyAssociationState( jsonFactories, this, stateName.name() );
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        return new JSONNamedAssociationState( jsonFactories, this, stateName.name() );
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
        return state.toString();
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
        state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VERSION, JSONKeys.MODIFIED )
                             .add( JSONKeys.VERSION, version )
                             .add( JSONKeys.MODIFIED, lastModified.toEpochMilli() )
                             .build();
    }

    private boolean stateCloneWithProperty( String stateName, JsonValue value )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        if( Objects.equals( valueState.get( stateName ), value ) )
        {
            return false;
        }
        JsonObjectBuilder valueBuilder = jsonFactories.cloneBuilderExclude( valueState, stateName );
        if( value == null )
        {
            valueBuilder.addNull( stateName );
        }
        else
        {
            valueBuilder.add( stateName, value );
        }
        state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                             .add( JSONKeys.VALUE, valueBuilder.build() )
                             .build();
        return true;
    }

    private boolean stateCloneWithAssociation( String stateName, EntityReference ref )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        JsonValue jsonRef = ref == null ? JsonValue.NULL : jsonFactories.toJsonString( ref.identity().toString() );
        if( Objects.equals( valueState.get( stateName ), jsonRef ) )
        {
            return false;
        }
        valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                  .add( stateName, jsonRef )
                                  .build();
        state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                             .add( JSONKeys.VALUE, valueState )
                             .build();
        return true;
    }

    void stateCloneAddManyAssociation( int idx, String stateName, EntityReference ref )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        String identity = ref.identity().toString();
        JsonArray manyAssoc;
        if( valueState.containsKey( stateName ) )
        {
            JsonArrayBuilder manyAssocBuilder = jsonFactories.builderFactory().createArrayBuilder();
            JsonArray previousManyAssoc = valueState.getJsonArray( stateName );
            int currentIdx = 0;
            for( JsonValue jsonRef : previousManyAssoc )
            {
                if( currentIdx == idx )
                {
                    manyAssocBuilder.add( identity );
                }
                manyAssocBuilder.add( jsonRef );
                currentIdx++;
            }
            if( idx >= previousManyAssoc.size() )
            {
                manyAssocBuilder.add( identity );
            }
            manyAssoc = manyAssocBuilder.build();
        }
        else
        {
            manyAssoc = jsonFactories.builderFactory().createArrayBuilder().add( identity ).build();
        }
        valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                  .add( stateName, manyAssoc )
                                  .build();
        state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                             .add( JSONKeys.VALUE, valueState )
                             .build();
    }

    void stateCloneRemoveManyAssociation( String stateName, EntityReference ref )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        if( valueState.containsKey( stateName ) )
        {
            String identity = ref.identity().toString();
            JsonArray manyAssoc = jsonFactories.cloneBuilderExclude( valueState.getJsonArray( stateName ),
                                                                     jsonFactories.toJsonString( identity ) )
                                               .build();
            valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                      .add( stateName, manyAssoc ).build();
            state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                                 .add( JSONKeys.VALUE, valueState )
                                 .build();
        }
    }

    void stateCloneClearManyAssociation( String stateName )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        if( valueState.containsKey( stateName ) )
        {
            valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                      .add( stateName, jsonFactories.builderFactory().createArrayBuilder().build() )
                                      .build();
            state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                                 .add( JSONKeys.VALUE, valueState )
                                 .build();
        }
    }

    void stateCloneAddNamedAssociation( String stateName, String name, EntityReference ref )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        JsonObjectBuilder namedAssoc = valueState.containsKey( stateName )
                                       ? jsonFactories.cloneBuilder( valueState.getJsonObject( stateName ) )
                                       : jsonFactories.builderFactory().createObjectBuilder();
        namedAssoc.add( name, ref.identity().toString() );
        valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                  .add( stateName, namedAssoc.build() )
                                  .build();
        state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                             .add( JSONKeys.VALUE, valueState )
                             .build();
    }

    void stateCloneRemoveNamedAssociation( String stateName, String name )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        if( valueState.containsKey( stateName ) )
        {
            JsonObject namedAssoc = jsonFactories.cloneBuilderExclude( valueState.getJsonObject( stateName ), name )
                                                 .build();
            valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                      .add( stateName, namedAssoc )
                                      .build();
            state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                                 .add( JSONKeys.VALUE, valueState )
                                 .build();
        }
    }

    void stateCloneClearNamedAssociation( String stateName )
    {
        JsonObject valueState = state.getJsonObject( JSONKeys.VALUE );
        if( valueState.containsKey( stateName ) )
        {
            valueState = jsonFactories.cloneBuilderExclude( valueState, stateName )
                                      .add( stateName, jsonFactories.builderFactory().createObjectBuilder().build() )
                                      .build();
            state = jsonFactories.cloneBuilderExclude( state, JSONKeys.VALUE )
                                 .add( JSONKeys.VALUE, valueState )
                                 .build();
        }
    }
}
