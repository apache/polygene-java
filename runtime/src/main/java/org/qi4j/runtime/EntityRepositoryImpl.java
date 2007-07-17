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
package org.qi4j.runtime;

import java.util.HashMap;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.EntityRepository;
import org.qi4j.api.persistence.composite.PersistentStorage;
import org.qi4j.api.persistence.composite.EntityComposite;

/**
 * TODO
 */
public final class EntityRepositoryImpl
    implements EntityRepository
{
    private CompositeFactory factory;
    private PersistentStorage storage;

    //TODo: Use softreference and GC queueing
    private HashMap<String, EntityComposite> cache;

    public EntityRepositoryImpl( CompositeFactory aFactory )
    {
        factory = aFactory;
        cache = new HashMap<String, EntityComposite>();
    }

    public void setStorage( PersistentStorage storage )
    {
        this.storage = storage;
    }

    public <T extends EntityComposite> T getInstance( String anIdentity, Class<T> aType )
    {
        EntityComposite entity = cache.get( anIdentity );
        if( entity == null )
        {
            entity = storage.getEntity( anIdentity, aType );
            if( entity != null )
            {
                cache.put( anIdentity, entity );
            }
            return aType.cast( entity );
        }
        return aType.cast( entity );
    }

    public <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate )
    {
        T object = getInstance( identity, type );
        if( autoCreate && object == null )
        {
            object = factory.newInstance( type );
            object.setIdentity( identity );
            object.setEntityRepository( this );
            object.initialize();
        }
        return object;
    }

    public <T extends EntityComposite> T newInstance( String identity, Class<T> type )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> void create( T t )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
