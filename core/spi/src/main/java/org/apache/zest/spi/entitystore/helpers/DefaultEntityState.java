/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.spi.entitystore.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.util.Classes;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entity.ManyAssociationState;
import org.apache.zest.spi.entity.NamedAssociationState;

/**
 * Standard implementation of EntityState.
 */
public final class DefaultEntityState
    implements EntityState
{
    private EntityStatus status;

    private String version;
    private long lastModified;
    private final EntityReference identity;
    private final EntityDescriptor entityDescriptor;

    private final Map<QualifiedName, Object> properties;
    private final Map<QualifiedName, EntityReference> associations;
    private final Map<QualifiedName, List<EntityReference>> manyAssociations;
    private final Map<QualifiedName, Map<String, EntityReference>> namedAssociations;

    public DefaultEntityState( long currentTime,
                               EntityReference identity,
                               EntityDescriptor entityDescriptor
    )
    {
        this( "",
              currentTime,
              identity,
              EntityStatus.NEW,
              entityDescriptor,
              new HashMap<QualifiedName, Object>(),
              new HashMap<QualifiedName, EntityReference>(),
              new HashMap<QualifiedName, List<EntityReference>>(),
              new HashMap<QualifiedName, Map<String, EntityReference>>() );
    }

    public DefaultEntityState( String version,
                               long lastModified,
                               EntityReference identity,
                               EntityStatus status,
                               EntityDescriptor entityDescriptor,
                               Map<QualifiedName, Object> properties,
                               Map<QualifiedName, EntityReference> associations,
                               Map<QualifiedName, List<EntityReference>> manyAssociations,
                               Map<QualifiedName, Map<String, EntityReference>> namedAssociations
    )
    {
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityDescriptor = entityDescriptor;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
        this.namedAssociations = namedAssociations;
    }

    // EntityState implementation
    @Override
    public final String version()
    {
        return version;
    }

    @Override
    public long lastModified()
    {
        return lastModified;
    }

    @Override
    public EntityReference identity()
    {
        return identity;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        return properties.get( stateName );
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        properties.put( stateName, newValue );
        markUpdated();
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        return associations.get( stateName );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        associations.put( stateName, newEntity );
        markUpdated();
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        List<EntityReference> manyAssociationState = manyAssociations.get( stateName );
        if( manyAssociationState == null )
        {
            manyAssociationState = new ArrayList<>();
            manyAssociations.put( stateName, manyAssociationState );
        }
        return new DefaultManyAssociationState( this, manyAssociationState );
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        Map<String, EntityReference> namedAssociationState = namedAssociations.get( stateName );
        if( namedAssociationState == null )
        {
            namedAssociationState = new LinkedHashMap<>();
            namedAssociations.put( stateName, namedAssociationState );
        }
        return new DefaultNamedAssociationState( this, namedAssociationState );
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

        // Copy named-associations
        entityState.namedAssociations.clear();
        for( Map.Entry<QualifiedName, Map<String, EntityReference>> entry : namedAssociations.entrySet() )
        {
            entityState.namedAssociations.put( entry.getKey(), entry.getValue() );
        }

        // Set version and timestamp
        entityState.version = version;
        entityState.lastModified = lastModified;
    }

    @Override
    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    @Override
    public EntityStatus status()
    {
        return status;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return Classes.exactTypeSpecification( type ).test( entityDescriptor );
    }

    @Override
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

    public Map<QualifiedName, Map<String, EntityReference>> namedAssociations()
    {
        return namedAssociations;
    }

    @Override
    public String toString()
    {
        return identity + "("
               + properties.size() + " properties, "
               + associations.size() + " associations, "
               + manyAssociations.size() + " many-associations, "
               + namedAssociations.size() + " named-associations)";
    }

    public void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }
}
