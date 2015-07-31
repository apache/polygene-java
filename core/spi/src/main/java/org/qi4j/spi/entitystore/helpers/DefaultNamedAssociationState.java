/*
 * Copyright (c) 2011-2013, Niclas Hedhman. All Rights Reserved.
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
import java.util.Map;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.NamedAssociationState;

/**
 * Default implementation of NamedAssociationState.
 * Backed by HashMap.
 */
public final class DefaultNamedAssociationState
    implements NamedAssociationState
{
    private final DefaultEntityState entityState;
    private final Map<String, EntityReference> references;

    public DefaultNamedAssociationState( DefaultEntityState entityState, Map<String, EntityReference> references )
    {
        this.entityState = entityState;
        this.references = references;
    }

    @Override
    public int count()
    {
        return references.size();
    }

    @Override
    public boolean containsName( String name )
    {
        return references.containsKey( name );
    }

    @Override
    public boolean put( String name, EntityReference entityReference )
    {
        if( references.put( name, entityReference ) == null )
        {
            return false;
        }
        entityState.markUpdated();
        return true;
    }

    @Override
    public boolean remove( String name )
    {
        if( references.remove( name ) == null )
        {
            return false;
        }
        entityState.markUpdated();
        return true;
    }

    @Override
    public EntityReference get( String name )
    {
        return references.get( name );
    }

    @Override
    public String nameOf( EntityReference entityReference )
    {
        for( Map.Entry<String, EntityReference> entry : references.entrySet() )
        {
            if( entry.getValue().equals( entityReference ) )
            {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> iterator()
    {
        final Iterator<String> iter = references.keySet().iterator();
        return new Iterator<String>()
        {
            private String current;

            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public String next()
            {
                current = iter.next();
                return current;
            }

            @Override
            public void remove()
            {
                iter.remove();
                entityState.markUpdated();
            }
        };
    }
}
