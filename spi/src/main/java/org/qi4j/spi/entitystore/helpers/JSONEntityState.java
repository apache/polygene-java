/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entitystore.helpers;

import java.io.Serializable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyTypeDescriptor;

/**
 * Standard implementation of EntityState.
 */
public final class JSONEntityState
    implements EntityState, Serializable
{
    protected DefaultEntityStoreUnitOfWork unitOfWork;

    protected EntityStatus status;

    protected String version;
    protected long lastModified;
    private final EntityReference identity;
    private final EntityDescriptor entityDescriptor;

    protected final JSONObject state;

    public JSONEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                            EntityReference identity,
                            EntityDescriptor entityDescriptor,
                            JSONObject initialState
    )
    {
        this( unitOfWork, "",
              System.currentTimeMillis(),
              identity,
              EntityStatus.NEW,
              entityDescriptor,
              initialState );
    }

    public JSONEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                            String version,
                            long lastModified,
                            EntityReference identity,
                            EntityStatus status,
                            EntityDescriptor entityDescriptor,
                            JSONObject state
    )
    {
        this.unitOfWork = unitOfWork;
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityDescriptor = entityDescriptor;
        this.state = state;
    }

    // EntityState implementation

    public final String version()
    {
        return version;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public EntityReference identity()
    {
        return identity;
    }

    public Object getProperty( QualifiedName stateName )
    {
        try
        {
            Object jsonValue = state.getJSONObject( "properties" ).opt( stateName.name() );
            if( jsonValue == null || jsonValue == JSONObject.NULL )
            {
                return null;
            }
            else
            {
                PropertyDescriptor propertyDescriptor = entityDescriptor.state()
                    .getPropertyByQualifiedName( stateName );
                Object value = ( (PropertyTypeDescriptor) propertyDescriptor ).propertyType()
                    .type()
                    .fromJSON( jsonValue, unitOfWork.module() );
                return value;
            }
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public void setProperty( QualifiedName stateName, Object newValue )
    {
        try
        {
            Object jsonValue;
            if( newValue == null )
            {
                jsonValue = JSONObject.NULL;
            }
            else
            {
                PropertyTypeDescriptor propertyDescriptor = entityDescriptor.state()
                    .getPropertyByQualifiedName( stateName );
                jsonValue = propertyDescriptor.propertyType().type().toJSON( newValue );
            }
            state.getJSONObject( "properties" ).put( stateName.name(), jsonValue );
            markUpdated();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public EntityReference getAssociation( QualifiedName stateName )
    {
        try
        {
            Object jsonValue = state.getJSONObject( "associations" ).opt( stateName.name() );
            if( jsonValue == null )
            {
                return null;
            }

            EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference( (String) jsonValue );
            return value;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
    {
        try
        {
            state.getJSONObject( "associations" )
                .put( stateName.name(), newEntity == null ? null : newEntity.identity() );
            markUpdated();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
    {
        try
        {
            JSONObject manyAssociations = state.getJSONObject( "manyassociations" );
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

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus status()
    {
        return status;
    }

    public boolean isOfType( TypeName type )
    {
        return entityDescriptor.entityType().type().equals( type );
    }

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

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version = unitOfWork.identity();
    }

    public void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }
}