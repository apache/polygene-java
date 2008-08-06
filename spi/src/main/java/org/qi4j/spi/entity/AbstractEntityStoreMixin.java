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
 * Base implementation of EntityStore.
 *
 * It provides management of EntityTypes.
 */
public abstract class AbstractEntityStoreMixin
    implements EntityStore
{
    private Map<String, EntityType> entityTypes = new ConcurrentHashMap<String, EntityType>();

    public void registerEntityType( EntityType entityType )
    {
        entityTypes.put( entityType.type(), entityType );
    }

    protected EntityType getEntityType( QualifiedIdentity identity )
        throws UnknownEntityTypeException
    {
        EntityType entityType = entityTypes.get( identity.type() );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }
        return entityType;
    }
}
