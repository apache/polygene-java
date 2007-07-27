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
package org.qi4j.api;

import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.Identity;
import java.net.URL;

/**
 * This repository is used to get proxies representing persistent objects.
 */
public interface EntityRepository
{
    /**
     * Get a proxy to a persistent object. This proxy may have been cached
     * and returned from previous invocations with the same identity.
     *
     * @param identity The identity of the object to retrieve from the repository.
     * @param type The type of the object to retrieve from the repository.
     * @return Returns the instance of the given identity of the given type. 
     */
    <T extends EntityComposite> T getInstance( String identity, Class<T> type );

    <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate );

    /** Create a URL for the composite of the given identity.
     *
     * @param identity The identity of the object to convert into a URL.
     * @return The URL to the composite of the given identity.
     */
    URL toURL( Identity identity );

    /** Deletes the given object from the repository.
     *
     * After this method call, the entity must be considered invalid.
     *
     * @param entity The entity to be permanently deleted from the repository.
     */
    <T extends EntityComposite> void deleteInstance( T entity );

    <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType );
}
