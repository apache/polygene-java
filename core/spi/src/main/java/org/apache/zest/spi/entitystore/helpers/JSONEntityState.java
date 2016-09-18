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
package org.apache.zest.spi.entitystore.helpers;

import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entity.ManyAssociationState;
import org.apache.zest.spi.entity.NamedAssociationState;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Standard JSON implementation of EntityState.
 */
public final class JSONEntityState
    implements EntityState
{
    private static final String[] EMPTY_NAMES = new String[ 0 ];
    private static final String[] CLONE_NAMES =
        {
            JSONKeys.IDENTITY,
            JSONKeys.APPLICATION_VERSION,
            JSONKeys.TYPE,
            JSONKeys.VERSION,
            JSONKeys.MODIFIED
        };

    private final ModuleDescriptor module;
    private final ValueSerialization valueSerialization;
    private final String version;
    private final EntityReference identity;
    private final EntityDescriptor entityDescriptor;

    private EntityStatus status;
    private long lastModified;
    private JSONObject state;

    /* package */ JSONEntityState( ModuleDescriptor module,
                                   ValueSerialization valueSerialization,
                                   String version,
                                   long lastModified,
                                   EntityReference identity,
                                   EntityStatus status,
                                   EntityDescriptor entityDescriptor,
                                   JSONObject state
    )
    {
        this.module = module;
        this.valueSerialization = valueSerialization;
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
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
    public long lastModified()
    {
        return lastModified;
    }

    @Override
    public EntityReference identity()
    {
        return identity;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        try
        {
            Object json = state.getJSONObject( JSONKeys.PROPERTIES ).opt( stateName.name() );
            if( JSONObject.NULL.equals( json ) )
            {
                return null;
            }
            else
            {
                PropertyDescriptor descriptor = entityDescriptor.state().findPropertyModelByQualifiedName( stateName );
                if( descriptor == null )
                {
                    return null;
                }
                return valueSerialization.deserialize( module, descriptor.valueType(), json.toString() );
            }
        }
        catch( ValueSerializationException | JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        try
        {
            Object jsonValue;
            if( newValue == null || ValueType.isPrimitiveValue( newValue ) )
            {
                jsonValue = newValue;
            }
            else
            {
                String serialized = valueSerialization.serialize( newValue );
                if( serialized.startsWith( "{" ) )
                {
                    jsonValue = new JSONObject( serialized );
                }
                else if( serialized.startsWith( "[" ) )
                {
                    jsonValue = new JSONArray( serialized );
                }
                else
                {
                    jsonValue = serialized;
                }
            }
            cloneStateIfGlobalStateLoaded();
            state.getJSONObject( JSONKeys.PROPERTIES ).put( stateName.name(), jsonValue );
            markUpdated();
        }
        catch( ValueSerializationException | JSONException e )
        {
            throw new EntityStoreException( "Unable to set property " + stateName + " value " + newValue, e );
        }
    }

    private JSONObject cloneJSON( JSONObject jsonObject )
        throws JSONException
    {
        String[] names = JSONObject.getNames( jsonObject );
        if( names == null )
        {
            names = EMPTY_NAMES;
        }
        return new JSONObject( jsonObject, names );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        try
        {
            Object jsonValue = state.getJSONObject( JSONKeys.ASSOCIATIONS ).opt( stateName.name() );
            if( jsonValue == null )
            {
                return null;
            }

            EntityReference value = jsonValue == JSONObject.NULL
                                    ? null
                                    : EntityReference.parseEntityReference( (String) jsonValue );
            return value;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        try
        {
            cloneStateIfGlobalStateLoaded();
            state.getJSONObject( JSONKeys.ASSOCIATIONS ).put( stateName.name(), newEntity == null
                                                                                ? null
                                                                                : newEntity.identity() );
            markUpdated();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        try
        {
            JSONObject manyAssociations = state.getJSONObject( JSONKeys.MANY_ASSOCIATIONS );
            JSONArray jsonValues = manyAssociations.optJSONArray( stateName.name() );
            if( jsonValues == null )
            {
                jsonValues = new JSONArray();
                manyAssociations.put( stateName.name(), jsonValues );
            }
            return new JSONManyAssociationState( this, jsonValues );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        try
        {
            JSONObject namedAssociations = state.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS );
            JSONObject jsonValues = namedAssociations.optJSONObject( stateName.name() );
            if( jsonValues == null )
            {
                jsonValues = new JSONObject();
                namedAssociations.put( stateName.name(), jsonValues );
            }
            return new JSONNamedAssociationState( this, jsonValues );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
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

    public JSONObject state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return identity + "(" + state + ")";
    }

    public void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }

    void cloneStateIfGlobalStateLoaded()
    {
        if( status != EntityStatus.LOADED )
        {
            return;
        }

        try
        {
            JSONObject newProperties = cloneJSON( state.getJSONObject( JSONKeys.PROPERTIES ) );
            JSONObject newAssoc = cloneJSON( state.getJSONObject( JSONKeys.ASSOCIATIONS ) );
            JSONObject newManyAssoc = cloneJSON( state.getJSONObject( JSONKeys.MANY_ASSOCIATIONS ) );
            JSONObject newNamedAssoc = cloneJSON( state.getJSONObject( JSONKeys.NAMED_ASSOCIATIONS ) );
            JSONObject stateClone = new JSONObject( state, CLONE_NAMES );
            stateClone.put( JSONKeys.PROPERTIES, newProperties );
            stateClone.put( JSONKeys.ASSOCIATIONS, newAssoc );
            stateClone.put( JSONKeys.MANY_ASSOCIATIONS, newManyAssoc );
            stateClone.put( JSONKeys.NAMED_ASSOCIATIONS, newNamedAssoc );
            state = stateClone;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }
}
