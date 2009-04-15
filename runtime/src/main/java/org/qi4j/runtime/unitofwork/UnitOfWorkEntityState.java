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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.helpers.DefaultDiffManyAssociationState;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.EntityStateChanges;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.value.ValueType;

import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
*/
class UnitOfWorkEntityState
    extends DefaultEntityState
{
    private final EntityState parentState;
    private final EntityStateChanges changes;

    UnitOfWorkEntityState( long entityVersion, long lastModified,
                                   EntityReference identity,
                                   EntityStatus status,
                                   Set<EntityTypeReference> entityTypes,
                                   Map<StateName, String> properties,
                                   Map<StateName, EntityReference> associations,
                                   Map<StateName, ManyAssociationState> manyAssociations,
                                   EntityState parentState )
    {
        super( entityVersion, lastModified, identity, status, entityTypes, properties, associations, manyAssociations );
        this.parentState = parentState;
        this.changes = new EntityStateChanges();
    }

    public String getProperty(StateName stateName)
    {
        if( properties.containsKey( stateName ) )
        {
            return properties.get( stateName );
        }

        // Get from parent state
        return parentState == null ? null : parentState.getProperty( stateName);
    }

    @Override
    public void setProperty(StateName stateName, String newValue)
    {
        super.setProperty(stateName, newValue);
        changes.setProperty(stateName, newValue);
    }

    public EntityReference getAssociation( StateName stateName )
    {
        if( associations.containsKey( stateName ) )
        {
            return associations.get( stateName );
        }

        return parentState == null ? null : parentState.getAssociation( stateName );
    }

    @Override
    public void setAssociation(StateName stateName, EntityReference newEntity)
    {
        super.setAssociation(stateName, newEntity);
        changes.setAssociation(stateName, newEntity);
    }

    public ManyAssociationState getManyAssociation( StateName stateName )
    {
        if( manyAssociations.containsKey( stateName ) )
        {
            return manyAssociations.get( stateName );
        }

        if (parentState == null)
            return null;

        // Copy parent
        ManyAssociationState parentManyAssociation = parentState.getManyAssociation( stateName );
        ManyAssociationState unitManyAssociation = new DefaultDiffManyAssociationState(stateName, parentManyAssociation, changes);
        manyAssociations.put(stateName, unitManyAssociation);
        return unitManyAssociation;
    }

    public EntityState getParentState()
    {
        return parentState;
    }

    public void mergeTo( EntityState state )
    {
        changes.applyTo(state);
    }
}
