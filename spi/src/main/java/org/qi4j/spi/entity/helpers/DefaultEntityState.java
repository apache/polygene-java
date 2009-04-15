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
package org.qi4j.spi.entity.helpers;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.value.ValueState;
import org.qi4j.spi.value.ValueType;

import java.io.Serializable;
import java.util.*;

/**
 * Standard implementation of EntityState.
 */
public class DefaultEntityState
        implements EntityState, Serializable
{
    protected EntityStatus status;
    private boolean modified;

    protected long version;
    protected long lastModified;
    private final EntityReference identity;
    private final Set<EntityTypeReference> entityTypes;

    protected final Map<StateName, String> properties;
    protected final Map<StateName, EntityReference> associations;
    protected final Map<StateName, ManyAssociationState> manyAssociations;

    public DefaultEntityState(EntityReference identity)
    {
        this(0,
                System.currentTimeMillis(),
                identity,
                EntityStatus.NEW,
                new HashSet<EntityTypeReference>(),
                new HashMap<StateName, String>(),
                new HashMap<StateName, EntityReference>(),
                new HashMap<StateName, ManyAssociationState>());
    }

    public DefaultEntityState(long version,
                              long lastModified,
                              EntityReference identity,
                              EntityStatus status,
                              Set<EntityTypeReference> entityTypes,
                              Map<StateName, String> properties,
                              Map<StateName, EntityReference> associations,
                              Map<StateName, ManyAssociationState> manyAssociations)
    {
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityTypes = entityTypes;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    // EntityState implementation
    public final long version()
    {
        return version;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public EntityReference identity()
    {
        return identity;
    }

    public String getProperty(StateName stateName)
    {
        return properties.get(stateName);
    }

    public void setProperty(StateName stateName, String newValue)
    {
        properties.put(stateName, newValue);
        modified = true;
    }

    public EntityReference getAssociation(StateName stateName)
    {
        return associations.get(stateName);
    }

    public void setAssociation(StateName stateName, EntityReference newEntity)
    {
        associations.put(stateName, newEntity);
        modified = true;
    }

    public ManyAssociationState getManyAssociation(StateName stateName)
    {
        ManyAssociationState manyAssociationState = manyAssociations.get(stateName);
        if (manyAssociationState == null)
            manyAssociations.put(stateName, new DefaultManyAssociationState());
        return manyAssociationState;
    }

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus status()
    {
        return status;
    }

    public void addEntityTypeReference(EntityTypeReference entityType)
    {
        entityTypes.add(entityType);
    }

    public void removeEntityTypeReference(EntityTypeReference type)
    {
        entityTypes.remove(type);
    }

    public boolean hasEntityTypeReference(EntityTypeReference type)
    {
        return entityTypes.contains(type);
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypes;
    }

    public boolean isModified()
    {
        if (modified)
            return true;

        for (ManyAssociationState manyAssociationState : manyAssociations.values())
        {
            DefaultManyAssociationState state = (DefaultManyAssociationState) manyAssociationState;
            if (state.isModified())
                return true;
        }

        return false;
    }

    public Map<StateName, String> getProperties()
    {
        return properties;
    }

    public Map<StateName, EntityReference> getAssociations()
    {
        return associations;
    }

    public Map<StateName, ManyAssociationState> getManyAssociations()
    {
        return manyAssociations;
    }

    @Override
    public String toString()
    {
        return identity + "(" + properties.size() + " properties, " + associations.size() + " associations, " + manyAssociations.size() + " many-associations)";
    }

    public void clearModified()
    {
        modified = false;
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version++;
    }

    protected ManyAssociationState createManyAssociationState()
    {
        return new DefaultManyAssociationState();
    }
}
