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

import java.net.URL;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.EntityRepository;
import org.qi4j.api.IdentityGenerator;
import org.qi4j.api.persistence.Identity;
import org.qi4j.api.persistence.Persistent;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.composite.PersistentStorage;
import org.qi4j.api.persistence.impl.IdentityImpl;
import org.qi4j.api.persistence.impl.PersistentImpl;

/**
 * TODO
 */
public final class EntityRepositoryImpl
    implements EntityRepository
{
    private CompositeBuilderFactory factory;
    private PersistentStorage storage;
    private IdentityGenerator identityGenerator;

    public EntityRepositoryImpl( CompositeBuilderFactory aBuilderFactory, IdentityGenerator identityGenerator )
    {
        this.identityGenerator = identityGenerator;
        factory = aBuilderFactory;
    }

    public void setStorage( PersistentStorage storage )
    {
        this.storage = storage;
    }

    public <T extends EntityComposite> T getInstance( String anIdentity, Class<T> aType )
    {
        EntityComposite entity = storage.getEntity( anIdentity, aType );
        return aType.cast( entity );
    }

    public <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate )
    {
        T object = getInstance( identity, type );
        if( autoCreate && object == null )
        {
            object = newEntityBuilder( identity, type ).newInstance();
        }
        return object;
    }

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
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        CompositeBuilder<T> builder = factory.newCompositeBuilder( compositeType );
        if( identity == null )
        {
            identity = identityGenerator.generate( compositeType );
        }
        builder.setMixin( Identity.class, new IdentityImpl( identity ) );
        builder.setMixin( Persistent.class, new PersistentImpl( this ) );
        return builder;
    }
}
