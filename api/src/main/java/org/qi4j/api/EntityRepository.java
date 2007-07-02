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

    <T extends EntityComposite> T newInstance( String identity, Class<T> type );
}
