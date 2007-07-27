/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.test.model;

import java.io.Serializable;
import java.net.URL;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.Identity;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.composite.PersistentStorage;
import org.qi4j.api.EntityRepository;
import org.qi4j.api.CompositeBuilder;

public final class DummyEntityRepository
    implements EntityRepository
{
    public <T extends EntityComposite> T getInstance( String identity, Class<T> type )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
