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

package org.qi4j.spi.entity.typeregistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.EntityTypeRegistry;
import org.qi4j.spi.entity.UnknownEntityTypeException;

/**
 * EntityType registry mixin which helps EntityStore implementations.
 */
public final class EntityTypeRegistryMixin
    implements EntityTypeRegistry
{
    protected Map<EntityTypeReference, EntityType> entityTypes = new ConcurrentHashMap<EntityTypeReference, EntityType>();

    public void registerEntityType( EntityType entityType )
    {
        EntityTypeReference reference = entityType.reference();
        if( !entityTypes.containsKey( reference ) )
        {
            Logger.getLogger( EntityTypeRegistry.class.getName() ).info( "Registered entity type:" + entityType.reference() );
            entityTypes.put( reference, entityType );
        }
    }

    public EntityType getEntityType( EntityTypeReference type )
        throws UnknownEntityTypeException
    {
        EntityType entityType = entityTypes.get( type );
        if( entityType == null )
        {
            Logger.getLogger( EntityTypeRegistry.class.getName() ).warning( "Unknown entity type:" + type );
            throw new UnknownEntityTypeException( type.toString() );
        }
        return entityType;
    }

    public Iterable<EntityType> getEntityTypes()
    {
        return entityTypes.values();
    }
}
