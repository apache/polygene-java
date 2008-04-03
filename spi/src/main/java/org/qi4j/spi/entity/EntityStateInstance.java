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
package org.qi4j.spi.entity;

import java.util.Collection;
import java.util.Map;
import org.qi4j.spi.serialization.EntityId;

/**
 * Standard implementation of EntityState.
 */
public class EntityStateInstance
    implements EntityState
{
    private final long entityVersion;
    private final EntityId identity;
    private EntityStatus status;

    protected final Map<String, Object> properties;
    protected final Map<String, EntityId> associations;
    protected final Map<String, Collection<EntityId>> manyAssociations;

    public EntityStateInstance( long entityVersion,
                                EntityId identity,
                                EntityStatus status,
                                Map<String, Object> properties,
                                Map<String, EntityId> associations,
                                Map<String, Collection<EntityId>> manyAssociations )
    {
        this.entityVersion = entityVersion;
        this.identity = identity;
        this.status = status;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
    }

    // EntityState implementation
    public long getEntityVersion()
    {
        return entityVersion;
    }

    public EntityId getIdentity()
    {
        return identity;
    }

    public Object getProperty( String qualifiedName )
    {
        return properties.get( qualifiedName );
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        properties.put( qualifiedName, newValue );
    }

    public EntityId getAssociation( String qualifiedName )
    {
        return associations.get( qualifiedName );
    }

    public void setAssociation( String qualifiedName, EntityId newEntity )
    {
        associations.put( qualifiedName, newEntity );
    }

    public Collection<EntityId> getManyAssociation( String qualifiedName )
    {
        Collection<EntityId> manyAssociation = manyAssociations.get( qualifiedName );
        return manyAssociation;
    }

    public Collection<EntityId> setManyAssociation( String qualifiedName, Collection<EntityId> newManyAssociation )
    {
        manyAssociations.put( qualifiedName, newManyAssociation );
        return newManyAssociation;
    }

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus getStatus()
    {
        return status;
    }

    public Iterable<String> getPropertyNames()
    {
        return properties.keySet();
    }

    public Iterable<String> getAssociationNames()
    {
        return associations.keySet();
    }

    public Iterable<String> getManyAssociationNames()
    {
        return manyAssociations.keySet();
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public Map<String, EntityId> getAssociations()
    {
        return associations;
    }

    public Map<String, Collection<EntityId>> getManyAssociations()
    {
        return manyAssociations;
    }

    @Override public String toString()
    {
        return identity + "(" + properties.size() + " properties, " + associations.size() + " associations, " + manyAssociations.size() + " many-associations)";
    }
}
