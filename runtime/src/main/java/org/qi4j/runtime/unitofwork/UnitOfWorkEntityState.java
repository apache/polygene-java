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
import org.qi4j.spi.unitofwork.event.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
class UnitOfWorkEntityState
    implements EntityState
{
    private String entityVersion;
    private long lastModified;
    private EntityReference identity;
    private EntityStatus status;
    private EntityState parentState;
    private Set<EntityTypeReference> entityTypes;
    private Map<StateName, String> properties;
    private Map<StateName, EntityReference> associations;
    private Map<StateName, ManyAssociationState> manyAssociations;
    private UnitOfWorkEvents uow;

    UnitOfWorkEntityState( UnitOfWorkEvents uow, EntityState parentState )
    {
        this.uow = uow;
        this.entityVersion = parentState.version();
        this.lastModified = parentState.lastModified();
        this.identity = parentState.identity();
        this.status = parentState.status();
        this.parentState = parentState;
    }

    UnitOfWorkEntityState( UnitOfWorkEvents uow, String entityVersion, long lastModified,
                           EntityReference identity,
                           EntityStatus status )
    {
        this.uow = uow;
        this.entityVersion = entityVersion;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;

        entityTypes = new HashSet<EntityTypeReference>();
        properties = new HashMap<StateName, String>();
        associations = new HashMap<StateName, EntityReference>();
        manyAssociations = new HashMap<StateName, ManyAssociationState>();
    }

    public EntityReference identity()
    {
        return identity;
    }

    public String version()
    {
        return entityVersion;
    }

    public long lastModified()
    {
        return lastModified;
    }

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus status()
    {
        return status;
    }

    public void addEntityTypeReference( EntityTypeReference type )
    {
        if( entityTypes == null )
        {
            entityTypes = new HashSet<EntityTypeReference>( parentState.entityTypeReferences() );
        }

        entityTypes.add( type );
        uow.addEvent( new AddEntityTypeEvent( identity, type ) );
    }

    public void removeEntityTypeReference( EntityTypeReference type )
    {
        if( entityTypes == null )
        {
            entityTypes = new HashSet<EntityTypeReference>( parentState.entityTypeReferences() );
        }

        entityTypes.remove( type );
        uow.addEvent( new RemoveEntityTypeEvent( identity, type ) );
    }

    public boolean hasEntityTypeReference( EntityTypeReference type )
    {
        return entityTypes == null ? parentState.hasEntityTypeReference( type ) : entityTypes.contains( type );
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypes == null ? parentState.entityTypeReferences() : entityTypes;
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
    }

    public String getProperty( StateName stateName )
    {
        if( properties != null && properties.containsKey( stateName ) )
        {
            return properties.get( stateName );
        }

        // Get from parent state
        return parentState.getProperty( stateName );
    }

    public void setProperty( StateName stateName, String newValue )
    {
        if( properties == null )
        {
            properties = new HashMap<StateName, String>();
        }

        properties.put( stateName, newValue );
        uow.addEvent( new SetPropertyEvent( identity, stateName, newValue ) );
    }

    public EntityReference getAssociation( StateName stateName )
    {
        if( associations != null && associations.containsKey( stateName ) )
        {
            return associations.get( stateName );
        }

        return parentState.getAssociation( stateName );
    }

    public void setAssociation( StateName stateName, EntityReference newEntity )
    {
        if( associations == null )
        {
            associations = new HashMap<StateName, EntityReference>();
        }
        associations.put( stateName, newEntity );
        uow.addEvent( new SetAssociationEvent( identity, stateName, newEntity ) );
    }

    public ManyAssociationState getManyAssociation( StateName stateName )
    {
        if( manyAssociations != null && manyAssociations.containsKey( stateName ) )
        {
            return manyAssociations.get( stateName );
        }

        if( parentState == null )
        {
            return null;
        }

        // Copy parent
        ManyAssociationState parentManyAssociation = parentState.getManyAssociation( stateName );
        ManyAssociationState unitManyAssociation = new BuilderManyAssociationState( stateName, parentManyAssociation, new EntityStateChanges( uow, identity ) );
        manyAssociations.put( stateName, unitManyAssociation );
        return unitManyAssociation;
    }

    public void refresh()
    {
    }

    public EntityState getParentState()
    {
        return parentState;
    }
}
