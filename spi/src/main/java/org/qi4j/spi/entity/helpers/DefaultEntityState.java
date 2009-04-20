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

import java.io.Serializable;
import java.util.*;

/**
 * Standard implementation of EntityState.
 */
public class DefaultEntityState
    implements EntityState, Serializable
{
    protected DefaultEntityStoreUnitOfWork unitOfWork;

    protected EntityStatus status;

    protected String version;
    protected long lastModified;
    private final EntityReference identity;
    private final Set<EntityTypeReference> entityTypes;

    protected final Map<StateName, String> properties;
    protected final Map<StateName, EntityReference> associations;
    protected final Map<StateName, List<EntityReference>> manyAssociations;

    public DefaultEntityState( DefaultEntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        this( unitOfWork, "",
              System.currentTimeMillis(),
              identity,
              EntityStatus.NEW,
              new HashSet<EntityTypeReference>(),
              new HashMap<StateName, String>(),
              new HashMap<StateName, EntityReference>(),
              new HashMap<StateName, List<EntityReference>>() );
    }

    public DefaultEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                               String version,
                               long lastModified,
                               EntityReference identity,
                               EntityStatus status,
                               Set<EntityTypeReference> entityTypes,
                               Map<StateName, String> properties,
                               Map<StateName, EntityReference> associations,
                               Map<StateName, List<EntityReference>> manyAssociations )
    {
        this.unitOfWork = unitOfWork;
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

    public String getProperty( StateName stateName )
    {
        return properties.get( stateName );
    }

    public void setProperty( StateName stateName, String newValue )
    {
        properties.put( stateName, newValue );
        unitOfWork.setProperty( identity, stateName, newValue );
    }

    public EntityReference getAssociation( StateName stateName )
    {
        return associations.get( stateName );
    }

    public void setAssociation( StateName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
        unitOfWork.setAssociation( identity, stateName, newEntity );
    }

    public ManyAssociationState getManyAssociation( StateName stateName )
    {
        List<EntityReference> manyAssociationState = manyAssociations.get( stateName );
        if( manyAssociationState == null )
        {
            manyAssociationState = new ArrayList<EntityReference>();
            manyAssociations.put( stateName, manyAssociationState );
        }
        return new DefaultManyAssociationState( manyAssociationState, identity, stateName, unitOfWork );
    }

    public void refresh()
    {
        if( status == EntityStatus.LOADED )
        {
            unitOfWork.refresh( this );
        }
    }

    public void copyTo( DefaultEntityState entityState )
    {
        // Copy entity types
        entityState.entityTypes.clear();
        for( EntityTypeReference entityType : entityTypes )
        {
            entityState.entityTypes.add( entityType );
        }

        // Copy properties
        entityState.properties.clear();
        for( Map.Entry<StateName, String> stateNameStringEntry : properties.entrySet() )
        {
            entityState.properties.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }

        // Copy associations
        entityState.associations.clear();
        for( Map.Entry<StateName, EntityReference> stateNameStringEntry : associations.entrySet() )
        {
            entityState.associations.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }

        // Copy many-associations
        entityState.manyAssociations.clear();
        for( Map.Entry<StateName, List<EntityReference>> stateNameStringEntry : manyAssociations.entrySet() )
        {
            entityState.manyAssociations.put( stateNameStringEntry.getKey(), stateNameStringEntry.getValue() );
        }
    }

    public void remove()
    {
        status = EntityStatus.REMOVED;
        unitOfWork.removeEntityState( identity );
    }

    public EntityStatus status()
    {
        return status;
    }

    public void addEntityTypeReference( EntityTypeReference entityType )
    {
        entityTypes.add( entityType );
        unitOfWork.addEntityType( identity, entityType );
    }

    public void removeEntityTypeReference( EntityTypeReference entityTypeReference )
    {
        entityTypes.remove( entityTypeReference );
        unitOfWork.removeEntityType( identity, entityTypeReference );
    }

    public boolean hasEntityTypeReference( EntityTypeReference type )
    {
        return entityTypes.contains( type );
    }

    public Set<EntityTypeReference> entityTypeReferences()
    {
        return entityTypes;
    }

    @Override
    public String toString()
    {
        return identity + "(" + properties.size() + " properties, " + associations.size() + " associations, " + manyAssociations.size() + " many-associations)";
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
    }
}
