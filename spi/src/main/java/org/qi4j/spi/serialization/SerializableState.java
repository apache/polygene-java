/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.StateName;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Serializable state for a single entity. This includes the version
 * of the state and the version of the type.
 */
public final class SerializableState
        implements Serializable
{
    private static final long serialVersionUID = 4L;

    private final EntityReference identity;
    private final long entityVersion;
    private final long lastModified;
    private final Set<EntityTypeReference> entityTypeReferences;
    private final Map<StateName, String> properties;
    private final Map<StateName, EntityReference> associations;
    private final Map<StateName, ManyAssociationState> manyAssociations;

    public SerializableState(EntityReference identity,
                             long entityVersion, long lastModified,
                             Set<EntityTypeReference> entityTypeReferences,
                             Map<StateName, String> properties,
                             Map<StateName, EntityReference> associations,
                             Map<StateName, ManyAssociationState> manyAssociations)
    {
        this.identity = identity;
        this.entityVersion = entityVersion;
        this.lastModified = lastModified;
        this.entityTypeReferences = entityTypeReferences;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public EntityReference identity()
    {
        return identity;
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypeReferences;
    }

    public long version()
    {
        return entityVersion;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public Map<StateName, String> properties()
    {
        return properties;
    }

    public Map<StateName, EntityReference> associations()
    {
        return associations;
    }

    public Map<StateName, ManyAssociationState> manyAssociations()
    {
        return manyAssociations;
    }
}
