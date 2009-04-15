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

package org.qi4j.runtime.unitofwork;

import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.EntityStateChanges;
import org.qi4j.spi.entity.helpers.DefaultDiffManyAssociationState;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.entity.EntityTypeReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.api.entity.EntityReference;

/**
 * JAVADOC
 */
public class DefaultDiffEntityState
        extends DefaultEntityState
{
    EntityStateChanges changes = new EntityStateChanges();

    public DefaultDiffEntityState()
    {
        super(EntityReference.NULL);
    }

    @Override
    public void setProperty(StateName stateName, String newValue)
    {
        super.setProperty(stateName, newValue);
        changes.setProperty(stateName, newValue);
    }

    @Override
    public void setAssociation(StateName stateName, EntityReference newEntity)
    {
        super.setAssociation(stateName, newEntity);
        changes.setAssociation(stateName, newEntity);
    }

    @Override
    public void addEntityTypeReference(EntityTypeReference entityType)
    {
        super.addEntityTypeReference(entityType);
        changes.addEntityTypeReference(entityType);
    }

    @Override
    public void removeEntityTypeReference(EntityTypeReference type)
    {
        super.removeEntityTypeReference(type);
        changes.removeEntityTypeReference(type);
    }

    @Override
    public ManyAssociationState getManyAssociation(StateName stateName)
    {
        return new DefaultDiffManyAssociationState(stateName, changes);
    }

    public void applyTo(EntityState entityState)
    {
        changes.applyTo(entityState);
    }
}
