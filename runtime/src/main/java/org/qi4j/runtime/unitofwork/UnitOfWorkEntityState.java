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
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

/**
 * JAVADOC
 */
class UnitOfWorkEntityState
    implements EntityState
{
    private String entityVersion;
    private long lastModified;
    private EntityStatus status;
    private EntityState parentState;
    private Map<QualifiedName, Object> properties;
    private Map<QualifiedName, EntityReference> associations;
    private Map<QualifiedName, ManyAssociationState> manyAssociations;

    UnitOfWorkEntityState( EntityState parentState )
    {
        this.entityVersion = parentState.version();
        this.lastModified = parentState.lastModified();
        this.status = parentState.status();
        this.parentState = parentState;
    }

    UnitOfWorkEntityState( String entityVersion, long lastModified,
                           EntityStatus status )
    {
        this.entityVersion = entityVersion;
        this.lastModified = lastModified;
        this.status = status;

        properties = new HashMap<QualifiedName, Object>();
        associations = new HashMap<QualifiedName, EntityReference>();
        manyAssociations = new HashMap<QualifiedName, ManyAssociationState>();
    }

    public EntityReference identity()
    {
        return parentState.identity();
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

    public boolean isOfType( TypeName type )
    {
        return parentState.isOfType( type );
    }

    public EntityDescriptor entityDescriptor()
    {
        return parentState.entityDescriptor();
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
    }

    public Object getProperty( QualifiedName stateName )
    {
        if( properties != null && properties.containsKey( stateName ) )
        {
            return properties.get( stateName );
        }

        // Get from parent state
        return parentState.getProperty( stateName );
    }

    public void setProperty( QualifiedName stateName, Object newValue )
    {
        if( properties == null )
        {
            properties = new HashMap<QualifiedName, Object>();
        }

        properties.put( stateName, newValue );
    }

    public EntityReference getAssociation( QualifiedName stateName )
    {
        if( associations != null && associations.containsKey( stateName ) )
        {
            return associations.get( stateName );
        }

        return parentState.getAssociation( stateName );
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
    {
        if( associations == null )
        {
            associations = new HashMap<QualifiedName, EntityReference>();
        }
        associations.put( stateName, newEntity );
    }

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
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
        ManyAssociationState unitManyAssociation = new BuilderManyAssociationState( parentManyAssociation );
        manyAssociations.put( stateName, unitManyAssociation );
        return unitManyAssociation;
    }

    public EntityState getParentState()
    {
        return parentState;
    }
}
