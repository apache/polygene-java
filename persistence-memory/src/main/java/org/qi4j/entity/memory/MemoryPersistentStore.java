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
package org.qi4j.entity.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStateHolder;
import org.qi4j.spi.entity.PersistenceException;
import org.qi4j.spi.entity.PersistentStore;
import org.qi4j.spi.serialization.SerializedObject;

public class MemoryPersistentStore
    implements PersistentStore
{
    private ConcurrentHashMap<String, Map<Class, SerializedObject>> entityStore;
    private ConcurrentHashMap<String, EntityStateHolder> cache;

    private String name;

    public MemoryPersistentStore( String name )
    {
        this.name = name;
        entityStore = new ConcurrentHashMap<String, Map<Class, SerializedObject>>();
    }

    public EntityStateHolder newEntityInstance( String identity, CompositeModel compositeModel )
        throws PersistenceException
    {
        if( entityStore.contains( identity ) )
        {
            throw new EntityAlreadyExistsException( getName(), identity );
        }
        entityStore.put( identity, new HashMap<Class, SerializedObject>() );
        MemoryEntityStateHolder stateHolder = new MemoryEntityStateHolder( identity, compositeModel, this );
        cache.put( identity, stateHolder );
        return stateHolder;
    }

    public EntityStateHolder getEntityInstance( String identity, CompositeModel compositeModel )
        throws PersistenceException
    {
        EntityStateHolder stateHolder = cache.get( identity );
        if( stateHolder == null )
        {
            if( !exists( identity ) )
            {
                throw new EntityNotFoundException( getName(), identity );
            }
            stateHolder = new MemoryEntityStateHolder( identity, compositeModel, this );
            cache.put( identity, stateHolder );
        }
        return stateHolder;
    }

    public List<EntityStateHolder> getEntityInstances( List<String> identities, CompositeModel compositeModel )
        throws PersistenceException
    {
        List<EntityStateHolder> result = new ArrayList<EntityStateHolder>( identities.size() );
        for( String id : identities )
        {
            result.add( getEntityInstance( id, compositeModel ) );
        }
        return result;
    }

    public boolean delete( String identity )
        throws PersistenceException
    {
        cache.remove( identity );
        return entityStore.remove( identity ) != null;
    }

    public String getName()
    {
        return name;
    }

    public boolean exists( String identity )
    {
        return cache.contains( identity ) || entityStore.contains( identity );
    }

    public Object getMixin( MemoryEntityStateHolder memoryEntityStateHolder, Class mixinType )
    {
        return null;
    }
}
