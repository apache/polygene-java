/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entitystore.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * JSON implementation of ManyAssociationState. Backed by JSONArray.
 */
public final class JSONManyAssociationState
    implements ManyAssociationState
{
    private JSONEntityState entityState;
    private JSONArray references;

    public JSONManyAssociationState( JSONEntityState entityState, JSONArray references )
    {
        this.entityState = entityState;
        this.references = references;
    }

    public int count()
    {
        return references.length();
    }

    public boolean contains( EntityReference entityReference )
    {
        try
        {
            for( int i = 0; i < references.length(); i++ )
            {
                if( references.get( i ).equals( entityReference.identity() ) )
                {
                    return true;
                }
            }
            return false;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public boolean add( int idx, EntityReference entityReference )
    {
        try
        {
            if( contains( entityReference ) )
            {
                return false;
            }
            entityState.cloneStateIfGlobalStateLoaded();
            references.insert( idx, entityReference.identity() );
            entityState.markUpdated();
            return true;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public boolean remove( EntityReference entityReference )
    {
        try
        {
            for( int i = 0; i < references.length(); i++ )
            {
                if( references.get( i ).equals( entityReference.identity() ) )
                {
                    entityState.cloneStateIfGlobalStateLoaded();
                    references.remove( i );
                    entityState.markUpdated();
                    return true;
                }
            }
            return false;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public EntityReference get( int i )
    {
        try
        {
            return new EntityReference( references.getString( i ) );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public Iterator<EntityReference> iterator()
    {
        return new Iterator<EntityReference>()
        {
            int idx = 0;

            public boolean hasNext()
            {
                return idx < references.length();
            }

            public EntityReference next()
            {
                try
                {
                    EntityReference ref = new EntityReference( references.getString( idx ) );
                    idx++;
                    return ref;
                }
                catch( JSONException e )
                {
                    throw new NoSuchElementException();
                }
            }

            public void remove()
            {
                throw new UnsupportedOperationException( "remove() is not supported on ManyAssociation iterators." );
            }
        };
    }
}