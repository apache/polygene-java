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
import org.qi4j.runtime.unitofwork.BuilderManyAssociationState;
import org.qi4j.runtime.unitofwork.EntityStateChanges;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
public final class BuilderEntityState
    implements EntityState
{
    EntityStateChanges changes;

    private final Set<EntityTypeReference> entityTypes;
    private final Map<StateName, String> properties;
    private final Map<StateName, EntityReference> associations;
    private final Map<StateName, ManyAssociationState> manyAssociations;

    public BuilderEntityState( UnitOfWorkEvents events )
    {
        entityTypes = new HashSet<EntityTypeReference>();
        properties = new HashMap<StateName, String>();
        associations = new HashMap<StateName, EntityReference>();
        manyAssociations = new HashMap<StateName, ManyAssociationState>();
        changes = new EntityStateChanges( events, EntityReference.NULL );
    }

    public EntityReference identity()
    {
        return EntityReference.NULL;
    }

    public String version()
    {
        return "";
    }

    public long lastModified()
    {
        return 0;
    }

    public void remove()
    {
    }

    public EntityStatus status()
    {
        return EntityStatus.NEW;
    }

    public boolean hasEntityTypeReference( EntityTypeReference type )
    {
        return entityTypes.contains( type );
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypes;
    }

    public String getProperty( StateName stateName )
    {
        return properties.get( stateName );
    }

    public EntityReference getAssociation( StateName stateName )
    {
        return associations.get( stateName );
    }

    public void hasBeenApplied()
    {
    }

    public void setProperty( StateName stateName, String newValue )
    {
        properties.put( stateName, newValue );
        changes.setProperty( stateName, newValue );
    }

    public void setAssociation( StateName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
        changes.setAssociation( stateName, newEntity );
    }

    public void addEntityTypeReference( EntityTypeReference entityTypeReference )
    {
        entityTypes.add( entityTypeReference );
        changes.addEntityTypeReference( entityTypeReference );
    }

    public void removeEntityTypeReference( EntityTypeReference entityTypeReference )
    {
        entityTypes.add( entityTypeReference );
        changes.removeEntityTypeReference( entityTypeReference );
    }

    public ManyAssociationState getManyAssociation( StateName stateName )
    {
        ManyAssociationState state = manyAssociations.get( stateName );
        if( state == null )
        {
            state = new BuilderManyAssociationState( stateName, changes );
            manyAssociations.put( stateName, state );
        }

        return state;
    }

    public void refresh()
    {
    }
}
