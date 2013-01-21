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

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

/**
 * JAVADOC
 */
public final class BuilderEntityState
    implements EntityState
{
    private final EntityDescriptor entityType;
    private EntityReference reference;
    private final Map<QualifiedName, Object> properties;
    private final Map<QualifiedName, EntityReference> associations;
    private final Map<QualifiedName, ManyAssociationState> manyAssociations;

    public BuilderEntityState( EntityDescriptor type, EntityReference reference )
    {
        entityType = type;
        this.reference = reference;
        properties = new HashMap<QualifiedName, Object>();
        associations = new HashMap<QualifiedName, EntityReference>();
        manyAssociations = new HashMap<QualifiedName, ManyAssociationState>();
    }

    @Override
    public EntityReference identity()
    {
        return reference;
    }

    @Override
    public String version()
    {
        return "";
    }

    @Override
    public long lastModified()
    {
        return 0;
    }

    @Override
    public void remove()
    {
    }

    @Override
    public EntityStatus status()
    {
        return EntityStatus.NEW;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return Classes.exactTypeSpecification( type ).satisfiedBy( entityType );
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        return entityType;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        return properties.get( stateName );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        return associations.get( stateName );
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        properties.put( stateName, newValue );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        ManyAssociationState state = manyAssociations.get( stateName );
        if( state == null )
        {
            state = new BuilderManyAssociationState();
            manyAssociations.put( stateName, state );
        }

        return state;
    }

    public void copyTo( EntityState newEntityState )
    {
        for( Map.Entry<QualifiedName, Object> stateNameStringEntry : properties.entrySet() )
        {
            newEntityState.setPropertyValue( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }
        for( Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : associations.entrySet() )
        {
            newEntityState.setAssociationValue( stateNameEntityReferenceEntry.getKey(), stateNameEntityReferenceEntry.getValue() );
        }
        for( Map.Entry<QualifiedName, ManyAssociationState> stateNameManyAssociationStateEntry : manyAssociations.entrySet() )
        {
            ManyAssociationState manyAssoc = newEntityState.manyAssociationValueOf( stateNameManyAssociationStateEntry.getKey() );
            int idx = 0;
            for( EntityReference entityReference : stateNameManyAssociationStateEntry.getValue() )
            {
                manyAssoc.add( idx, entityReference );
            }
        }
    }
}
