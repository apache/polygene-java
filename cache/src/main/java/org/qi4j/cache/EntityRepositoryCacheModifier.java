/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.cache;

import org.qi4j.api.EntityRepository;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.Identity;
import java.net.URL;

/**
 * Implement caching of created proxies to persistent objects.
 */
public final class EntityRepositoryCacheModifier
    implements EntityRepository
{
    @Uses private CompositeRepositoryCache cache;
    @Modifies private EntityRepository repository;

    public <T extends EntityComposite> T getInstance( String anIdentity, Class<T> aType )
    {
        // Check cache
        EntityComposite cachedObj = cache.getObject( anIdentity );
        if( cachedObj != null )
        {
            return (T) cachedObj;
        }

        // Not found in cache - create it
        cachedObj = repository.getInstance( anIdentity, aType );

        // Add to cache
        cache.addObject( anIdentity, cachedObj );

        return (T) cachedObj;
    }

    public <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate )
    {
        return repository.getInstance( identity, type, autoCreate );
    }

//    public <T extends EntityComposite> T newInstance( String identity, Class<T> type )
//    {
//        T instance = repository.newInstance( identity, type );
//        cache.addObject( identity, instance );
//        return instance;
//    }
//
    /**
     * Create a URL for the composite of the given identity.
     *
     * @param identity The identity of the object to convert into a URL.
     * @return The URL to the composite of the given identity.
     */
    public URL toURL( Identity identity )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Deletes the given object from the repository.
     * <p/>
     * After this method call, the entity must be considered invalid.
     *
     * @param entity The entity to be permanently deleted from the repository.
     */
    public <T extends EntityComposite> void deleteInstance( T entity )
    {
        repository.deleteInstance( entity );
        cache.removeObject( entity.getIdentity() );
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        return repository.newEntityBuilder( identity, compositeType );
    }

}
