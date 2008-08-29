/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EntityType registry mixin which helps EntityStore implementations.
 */
public abstract class EntityTypeRegistryMixin
    implements EntityStore
{
    protected Map<String, EntityType> entityTypes = new ConcurrentHashMap<String, EntityType>();

    public void registerEntityType( EntityType entityType )
    {
        entityTypes.put( entityType.type(), entityType );
    }

    public EntityType getEntityType( String aEntityType )
        throws UnknownEntityTypeException
    {
        EntityType entityType = entityTypes.get( aEntityType );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( aEntityType );
        }
        return entityType;
    }
}
