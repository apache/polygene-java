/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity.helpers;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.entity.EntityTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.io.Serializable;

/**
 * Tracker for changes in an EntityState
 */
public class EntityStateChanges
    implements Serializable
{
    private List<EntityStateChange> changes;

    public void setProperty(StateName stateName, String newValue)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new PropertyChange(stateName, newValue));
    }

    public void setAssociation(StateName stateName, EntityReference entityReference)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new AssociationChange(stateName, entityReference));
    }

    public void added(StateName stateName, int index, EntityReference reference)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new ManyAssociationChange(stateName, index, reference));
    }

    public void removed(StateName stateName, EntityReference reference)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new ManyAssociationChange(stateName, reference));
    }

    public void applyTo(EntityState state)
    {
        if (changes != null)
        {
            for (EntityStateChange change : changes)
            {
                change.applyTo(state);
            }
        }
    }

    public Iterable<EntityStateChange> changes()
    {
        return changes == null ? Collections.<EntityStateChange>emptyList() : changes;
    }

    public void addEntityTypeReference(EntityTypeReference entityTypeReference)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new AddedEntityTypeChange(entityTypeReference));
    }


    public void removeEntityTypeReference(EntityTypeReference entityTypeReference)
    {
        if (changes == null)
            changes = new ArrayList<EntityStateChange>();
        changes.add(new RemovedEntityTypeChange(entityTypeReference));
    }
}
