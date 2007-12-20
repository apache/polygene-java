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
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.serialization.SerializedObject;

public class MemoryEntityStore
    implements EntityStore
{
    private ConcurrentHashMap<String, Map<Class, SerializedObject>> entityStore;
    private ConcurrentHashMap<String, EntityStateHolder> cache;

    private String name;

    public MemoryEntityStore( String name )
    {
        this.name = name;
        entityStore = new ConcurrentHashMap<String, Map<Class, SerializedObject>>();
    }

    public EntityStateHolder newEntityInstance( String identity, CompositeModel compositeModel )
        throws StoreException
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

    public EntityStateHolder getEntityInstance( String identity, Class compositeType )
        throws StoreException
    {
        EntityStateHolder stateHolder = cache.get( identity );
        if( stateHolder == null )
        {
            if( !exists( identity ) )
            {
                throw new EntityNotFoundException( getName(), identity );
            }
            stateHolder = new MemoryEntityStateHolder( identity, null, this );
            cache.put( identity, stateHolder );
        }
        return stateHolder;
    }

    public EntityStateHolder newEntityInstance( String identity, Class compositeType ) throws StoreException
    {
        return null;
    }

    public List<EntityStateHolder> getEntityInstances( List<String> identities, Class compositeType ) throws StoreException
    {
        return null;
    }

    public List<EntityStateHolder> getEntityInstances( List<String> identities, CompositeModel compositeModel )
        throws StoreException
    {
        List<EntityStateHolder> result = new ArrayList<EntityStateHolder>( identities.size() );
        for( String id : identities )
        {
            result.add( getEntityInstance( id, compositeModel.getCompositeClass() ) );
        }
        return result;
    }

    public boolean delete( String identity )
        throws StoreException
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
