/*
 * Copyright (c) 2009-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.spi.entitystore.helpers;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.NamedAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * JSON implementation of NamedAssociationState.
 * <p>Backed by a JSONObject.</p>
 */
public final class JSONNamedAssociationState
    implements NamedAssociationState
{

    private final JSONEntityState entityState;
    private final JSONObject references;

    public JSONNamedAssociationState( JSONEntityState entityState, JSONObject references )
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
    public boolean containsName( String name )
    {
        return references.has( name );
    }

    @Override
    public boolean put( String name, EntityReference entityReference )
    {
        try
        {
            if( references.has( name ) && entityReference.identity().equals( references.getString( name ) ) )
            {
                return false;
            }
            entityState.cloneStateIfGlobalStateLoaded();
            references.put( name, entityReference.identity() );
            entityState.markUpdated();
            return true;
        }
        catch( JSONException ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public boolean remove( String name )
    {
        if( !references.has( name ) )
        {
            return false;
        }
        entityState.cloneStateIfGlobalStateLoaded();
        references.remove( name );
        entityState.markUpdated();
        return true;
    }

    @Override
    public EntityReference get( String name )
    {
        try
        {
            return new EntityReference( references.getString( name ) );
        }
        catch( JSONException ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public String nameOf( EntityReference entityReference )
    {
        JSONArray names = references.names();
        if( names == null )
        {
            return null;
        }
        try
        {
            for( int idx = 0; idx < names.length(); idx++ )
            {
                String name = names.getString( idx );
                if( entityReference.identity().equals( references.getString( name ) ) )
                {
                    return name;
                }
            }
            return null;
        }
        catch( JSONException ex )
        {
            throw new EntityStoreException( ex );
        }
    }

    @Override
    public Iterator<String> iterator()
    {
        final JSONArray names = references.names() == null ? new JSONArray() : references.names();
        return new Iterator<String>()
        {
            private int idx = 0;

            @Override
            public boolean hasNext()
            {
                return idx < names.length();
            }

            @Override
            public String next()
            {
                try
                {
                    String next = names.getString( idx );
                    idx++;
                    return next;
                }
                catch( JSONException ex )
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
        return references.toString();
    }

}
