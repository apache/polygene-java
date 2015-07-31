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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.json.JSONArray;
import org.json.JSONException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * JSON implementation of ManyAssociationState.
 * <p>Backed by a JSONArray.</p>
 */
public final class JSONManyAssociationState
    implements ManyAssociationState
{

    private final JSONEntityState entityState;
    private final JSONArray references;

    public JSONManyAssociationState( JSONEntityState entityState, JSONArray references )
    {
        this.entityState = entityState;
        this.references = references;
    }

    @Override
    public int count()
    {
        return references.length();
    }

    @Override
    public boolean contains( EntityReference entityReference )
    {
        return indexOfReference( entityReference.identity() ) != -1;
    }

    @Override
    public boolean add( int idx, EntityReference entityReference )
    {
        try
        {
            if( indexOfReference( entityReference.identity() ) != -1 )
            {
                return false;
            }
            entityState.cloneStateIfGlobalStateLoaded();
            insertReference( idx, entityReference.identity() );
            entityState.markUpdated();
            return true;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public boolean remove( EntityReference entityReference )
    {
        int refIndex = indexOfReference( entityReference.identity() );
        if( refIndex != -1 )
        {
            entityState.cloneStateIfGlobalStateLoaded();
            references.remove( refIndex );
            entityState.markUpdated();
            return true;
        }
        return false;
    }

    @Override
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

    @Override
    public Iterator<EntityReference> iterator()
    {
        return new Iterator<EntityReference>()
        {
            private int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < references.length();
            }

            @Override
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
        return references.toString();
    }

    private int indexOfReference( Object item )
    {
        for( int idx = 0; idx < references.length(); idx++ )
        {
            if( item.equals( references.opt( idx ) ) )
            {
                return idx;
            }
        }
        return -1;
    }

    private void insertReference( int insert, Object item )
        throws JSONException
    {
        if( insert < 0 || insert > references.length() )
        {
            throw new JSONException( "JSONArray[" + insert + "] is out of bounds." );
        }
        if( insert == references.length() )
        {
            // append
            references.put( item );
        }
        else
        {
            // insert (copy/insert/apply)
            JSONArray output = new JSONArray();
            for( int idx = 0; idx < references.length(); idx++ )
            {
                if( idx == insert )
                {
                    output.put( item );
                }
                output.put( references.opt( idx ) );
            }
            for( int idx = 0; idx < output.length(); idx++ )
            {
                references.put( idx, output.opt( idx ) );
            }
        }
    }
}
