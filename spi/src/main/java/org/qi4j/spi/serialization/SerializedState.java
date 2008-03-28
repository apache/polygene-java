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
 * TODO
 */
public final class SerializedState
    implements Serializable
{
    private Map<String, Serializable> properties;
    private Map<String, EntityId> associations;
    private Map<String, Collection<EntityId>> manyAssociations;

    public SerializedState( Map<String, Serializable> properties, Map<String, EntityId> associations, Map<String, Collection<EntityId>> manyAssociations )
    {
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    public Map<String, Serializable> getProperties()
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
