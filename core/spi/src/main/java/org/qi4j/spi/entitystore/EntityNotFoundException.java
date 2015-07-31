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
package org.qi4j.spi.entitystore;

import org.qi4j.api.entity.EntityReference;

/**
 * This exception is thrown when an Entity could not be found.
 */
public class EntityNotFoundException
    extends EntityStoreException
{
    private EntityReference identity;

    public EntityNotFoundException( EntityReference identity )
    {
        super("Entity " + identity + " not found");
        this.identity = identity;
    }

    public EntityReference identity()
    {
        return identity;
    }
}
