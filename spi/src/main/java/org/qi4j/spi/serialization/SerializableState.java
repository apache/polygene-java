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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Serializable state for a single entity. This includes the version
 * of the state and the version of the type.
 */
public final class SerializableState
    implements Serializable
{
    private final long entityVersion;
    private final Map<String, Object> properties;
    private final Map<String, EntityId> associations;
    private final Map<String, Collection<EntityId>> manyAssociations;

    public SerializableState( long entityVersion, Map<String, Object> properties, Map<String, EntityId> associations, Map<String, Collection<EntityId>> manyAssociations )
    {
        this.entityVersion = entityVersion;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public long getEntityVersion()
    {
        return entityVersion;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public Map<String, EntityId> getAssociations()
    {
        return associations;
    }

    public Map<String, Collection<EntityId>> getManyAssociations()
    {
        return manyAssociations;
    }
}
