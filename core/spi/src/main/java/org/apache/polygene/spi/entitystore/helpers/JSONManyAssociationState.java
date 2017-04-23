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
package org.apache.polygene.spi.entitystore.helpers;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonValue;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entitystore.EntityStoreException;

/**
 * JSON implementation of ManyAssociationState.
 * <p>Backed by a JsonArray.</p>
 */
public final class JSONManyAssociationState
    implements ManyAssociationState
{
    private final JavaxJsonFactories jsonFactories;
    private final JSONEntityState entityState;
    private final String stateName;

    /* package */ JSONManyAssociationState( JavaxJsonFactories jsonFactories,
                                            JSONEntityState entityState,
                                            String stateName )
    {
        this.jsonFactories = jsonFactories;
        this.entityState = entityState;
        this.stateName = stateName;
    }

    private JsonArray getReferences()
    {
        JsonValue references = entityState.state().getJsonObject( JSONKeys.VALUE ).get( stateName );
        if( references != null && references.getValueType() == JsonValue.ValueType.ARRAY )
        {
            return (JsonArray) references;
        }
        return jsonFactories.builderFactory().createArrayBuilder().build();
    }

    @Override
    public int count()
    {
        return getReferences().size();
    }

    @Override
    public boolean contains( EntityReference entityReference )
    {
        return indexOfReference( entityReference.identity().toString() ) != -1;
    }

    @Override
    public boolean add( int idx, EntityReference entityReference )
    {
        try
        {
            if( indexOfReference( entityReference.identity().toString() ) != -1 )
            {
                return false;
            }
            entityState.stateCloneAddManyAssociation( idx, stateName, entityReference );
            entityState.markUpdated();
            return true;
        }
        catch( JsonException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public boolean remove( EntityReference entityReference )
    {
        int refIndex = indexOfReference( entityReference.identity().toString() );
        if( refIndex != -1 )
        {
            entityState.stateCloneRemoveManyAssociation( stateName, entityReference );
            entityState.markUpdated();
            return true;
        }
        return false;
    }

    @Override
    public EntityReference get( int i )
    {
        return EntityReference.parseEntityReference( getReferences().getString( i ) );
    }

    @Override
    public Iterator<EntityReference> iterator()
    {
        return new Iterator<EntityReference>()
        {
            private int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < getReferences().size();
            }

            @Override
            public EntityReference next()
            {
                try
                {
                    EntityReference ref = EntityReference.parseEntityReference( getReferences().getString( idx ) );
                    idx++;
                    return ref;
                }
                catch( JsonException e )
                {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException( "remove() is not supported on ManyAssociation iterators." );
            }
        };
    }

    @Override
    public String toString()
    {
        return getReferences().toString();
    }

    private int indexOfReference( String entityIdentityAsString )
    {
        JsonArray references = getReferences();
        for( int idx = 0; idx < references.size(); idx++ )
        {
            if( entityIdentityAsString.equals( references.getString( idx, null ) ) )
            {
                return idx;
            }
        }
        return -1;
    }
}
