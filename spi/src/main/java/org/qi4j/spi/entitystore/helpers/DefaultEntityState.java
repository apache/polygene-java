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
package org.qi4j.spi.entitystore.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;

/**
 * Standard implementation of EntityState.
 */
public final class DefaultEntityState
    implements EntityState, Serializable
{
    protected DefaultEntityStoreUnitOfWork unitOfWork;

    protected EntityStatus status;

    protected String version;
    protected long lastModified;
    private final EntityReference identity;
    private final EntityDescriptor entityDescriptor;

    protected final Map<QualifiedName, Object> properties;
    protected final Map<QualifiedName, EntityReference> associations;
    protected final Map<QualifiedName, List<EntityReference>> manyAssociations;

    public DefaultEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                               EntityReference identity,
                               EntityDescriptor entityDescriptor
    )
    {
        this( unitOfWork, "",
              System.currentTimeMillis(),
              identity,
              EntityStatus.NEW,
              entityDescriptor,
              new HashMap<QualifiedName, Object>(),
              new HashMap<QualifiedName, EntityReference>(),
              new HashMap<QualifiedName, List<EntityReference>>() );
    }

    public DefaultEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                               String version,
                               long lastModified,
                               EntityReference identity,
                               EntityStatus status,
                               EntityDescriptor entityDescriptor,
                               Map<QualifiedName, Object> properties,
                               Map<QualifiedName, EntityReference> associations,
                               Map<QualifiedName, List<EntityReference>> manyAssociations
    )
    {
        this.unitOfWork = unitOfWork;
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityDescriptor = entityDescriptor;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    // EntityState implementation

    public final String version()
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

    public Object getProperty( QualifiedName stateName )
    {
        return properties.get( stateName );
    }

    public void setProperty( QualifiedName stateName, Object newValue )
    {
        properties.put( stateName, newValue );
        markUpdated();
    }

    public EntityReference getAssociation( QualifiedName stateName )
    {
        return associations.get( stateName );
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
        markUpdated();
    }

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
    {
        List<EntityReference> manyAssociationState = manyAssociations.get( stateName );
        if( manyAssociationState == null )
        {
            manyAssociationState = new ArrayList<EntityReference>();
            manyAssociations.put( stateName, manyAssociationState );
        }
        return new DefaultManyAssociationState( this, manyAssociationState );
    }

    public void copyTo( DefaultEntityState entityState )
    {
        // Copy properties
        entityState.properties.clear();
        for( Map.Entry<QualifiedName, Object> stateNameStringEntry : properties.entrySet() )
        {
            entityState.properties.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }

        // Copy associations
        entityState.associations.clear();
        for( Map.Entry<QualifiedName, EntityReference> stateNameStringEntry : associations.entrySet() )
        {
            entityState.associations.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }

        // Copy many-associations
        entityState.manyAssociations.clear();
        for( Map.Entry<QualifiedName, List<EntityReference>> stateNameStringEntry : manyAssociations.entrySet() )
        {
            entityState.manyAssociations.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }

        // Set version and timestamp
        entityState.version = version;
        entityState.lastModified = lastModified;
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
        return entityDescriptor.entityType().type().equals( type );
    }

    public EntityDescriptor entityDescriptor()
    {
        return entityDescriptor;
    }

    public Map<QualifiedName, Object> properties()
    {
        return properties;
    }

    public Map<QualifiedName, EntityReference> associations()
    {
        return associations;
    }

    public Map<QualifiedName, List<EntityReference>> manyAssociations()
    {
        return manyAssociations;
    }

    @Override
    public String toString()
    {
        return identity + "(" +
               properties.size() + " properties, " +
               associations.size() + " associations, " +
               manyAssociations.size() + " many-associations)";
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version = unitOfWork.identity();
    }

    public void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }
}
