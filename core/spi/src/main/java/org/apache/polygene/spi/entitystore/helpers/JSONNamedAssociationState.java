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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.EntityStoreException;

/**
 * JSON implementation of NamedAssociationState.
 * <p>Backed by a JsonObject.</p>
 */
public final class JSONNamedAssociationState
    implements NamedAssociationState
{
    private final JavaxJsonFactories jsonFactories;
    private final JSONEntityState entityState;
    private final String stateName;

    /* package */ JSONNamedAssociationState( JavaxJsonFactories jsonFactories,
                                             JSONEntityState entityState,
                                             String stateName )
    {
        this.jsonFactories = jsonFactories;
        this.entityState = entityState;
        this.stateName = stateName;
    }

    private JsonObject getReferences()
    {
        JsonValue references = entityState.state().getJsonObject( JSONKeys.VALUE ).get( stateName );
        if( references != null && references.getValueType() == JsonValue.ValueType.OBJECT )
        {
            return (JsonObject) references;
        }
        return jsonFactories.builderFactory().createObjectBuilder().build();
    }

    @Override
    public int count()
    {
        return getReferences().size();
    }

    @Override
    public boolean containsName( String name )
    {
        return getReferences().containsKey( name );
    }

    @Override
    public boolean put( String name, EntityReference entityReference )
    {
        try
        {
            if( containsName( name )
                && entityReference.identity().toString().equals( getReferences().getString( name ) ) )
            {
                return false;
            }
            entityState.stateCloneAddNamedAssociation( stateName, name, entityReference );
            entityState.markUpdated();
            return true;
        }
        catch( JsonException ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public boolean remove( String name )
    {
        if( !containsName( name ) )
        {
            return false;
        }
        entityState.stateCloneRemoveNamedAssociation( stateName, name );
        entityState.markUpdated();
        return true;
    }

    @Override
    public boolean clear()
    {
        if( count() > 0 )
        {
            entityState.stateCloneClearNamedAssociation( stateName );
            entityState.markUpdated();
        }
        return false;
    }

    @Override
    public EntityReference get( String name )
    {
        String stringRef = getReferences().getString( name, null );
        return stringRef == null ? null : EntityReference.parseEntityReference( stringRef );
    }

    @Override
    public String nameOf( EntityReference entityReference )
    {
        try
        {
            JsonObject references = getReferences();
            for( String name : references.keySet() )
            {
                if( entityReference.identity().toString().equals( references.getString( name ) ) )
                {
                    return name;
                }
            }
            return null;
        }
        catch( JsonException ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public Iterator<String> iterator()
    {
        List<String> names = new ArrayList<>( getReferences().keySet() );
        return new Iterator<String>()
        {
            private int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < names.size();
            }

            @Override
            public String next()
            {
                try
                {
                    String next = names.get( idx );
                    idx++;
                    return next;
                }
                catch( JsonException ex )
                {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException( "remove() is not supported on NamedAssociation iterators." );
            }
        };
    }

    @Override
    public String toString()
    {
        return getReferences().toString();
    }
}
