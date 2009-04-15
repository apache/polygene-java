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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StateName;

/**
 * Record of a change in a ManyAssociation. An EntityReference can either
 * be added or removed.
 */
public class ManyAssociationChange
    extends EntityStateChange
{
    int index = -1;
    EntityReference reference;

    // Added to ManyAssociation
    ManyAssociationChange(StateName stateName, int index, EntityReference reference)
    {
        super(stateName);

        this.index = index;
        this.reference = reference;
    }

    // Remove from ManyAssociation
    ManyAssociationChange(StateName stateName, EntityReference reference)
    {
        super(stateName);

        this.reference = reference;
    }

    public boolean isAdded()
    {
        return index != -1;
    }

    public int index()
    {
        return index;
    }

    public EntityReference reference()
    {
        return reference;
    }

    public void applyTo(EntityState entityState)
    {
        ManyAssociationState manyAssociation = entityState.getManyAssociation(stateName);

        if (isAdded())
            manyAssociation.add(index,  reference);
        else
            manyAssociation.remove(reference);
    }
}
