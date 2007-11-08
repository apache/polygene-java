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
import org.qi4j.entity.EntityComposite;
import org.qi4j.model.CompositeModel;
import org.qi4j.spi.persistence.EntityAlreadyExistsException;
import org.qi4j.spi.persistence.EntityNotFoundException;
import org.qi4j.spi.persistence.EntityStateHolder;
import org.qi4j.spi.persistence.PersistenceException;
import org.qi4j.spi.persistence.PersistentStore;
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

    public <T extends EntityComposite> EntityStateHolder<T> newEntityInstance( String identity, CompositeModel<T> compositeModel )
        throws PersistenceException
    {
        if( entityStore.contains( identity ) )
        {
            throw new EntityAlreadyExistsException( getName(), identity );
        }
        entityStore.put( identity, new HashMap<Class, SerializedObject>() );
        MemoryEntityStateHolder<T> stateHolder = new MemoryEntityStateHolder<T>( identity, compositeModel, this );
        cache.put( identity, stateHolder );
        return stateHolder;
    }

    public <T extends EntityComposite> EntityStateHolder<T> getEntityInstance( String identity, CompositeModel<T> compositeModel )
        throws PersistenceException
    {
        EntityStateHolder<T> stateHolder = cache.get( identity );
        if( stateHolder == null )
        {
            if( !exists( identity ) )
            {
                throw new EntityNotFoundException( getName(), identity );
            }
            stateHolder = new MemoryEntityStateHolder<T>( identity, compositeModel, this );
            cache.put( identity, stateHolder );
        }
        return stateHolder;
    }

    public <T extends EntityComposite> List<EntityStateHolder<T>> getEntityInstances( List<String> identities, CompositeModel<T> compositeModel )
        throws PersistenceException
    {
        List<EntityStateHolder<T>> result = new ArrayList<EntityStateHolder<T>>( identities.size() );
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

    public <T extends EntityComposite> Object getMixin( MemoryEntityStateHolder<T> memoryEntityStateHolder, Class mixinType )
    {
        return null;
    }
}
