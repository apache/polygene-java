/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.value.ValueState;

import java.util.Collection;
import java.util.Map;

/**
 * Adapter for EntityState. Subclass and override methods to use.
 */
public class EntityStateAdapter
    implements EntityState
{
    private EntityState entityState;

    public EntityStateAdapter( EntityState entityState )
    {
        this.entityState = entityState;
    }

    public EntityState wrappedEntityState()
    {
        return entityState;
    }

    public QualifiedIdentity qualifiedIdentity()
    {
        return entityState.qualifiedIdentity();
    }

    public long version()
    {
        return entityState.version();
    }

    public long lastModified()
    {
        return entityState.lastModified();
    }

    public void remove()
    {
        entityState.remove();
    }

    public EntityStatus status()
    {
        return entityState.status();
    }

    public EntityType entityType()
    {
        return entityState.entityType();
    }

    public Object getProperty( QualifiedName qualifiedName )
    {
        return entityState.getProperty( qualifiedName );
    }

    public void setProperty( QualifiedName qualifiedName, Object newValue )
    {
        entityState.setProperty( qualifiedName, newValue );
    }

    public QualifiedIdentity getAssociation( QualifiedName qualifiedName )
    {
        return entityState.getAssociation( qualifiedName );
    }

    public void setAssociation( QualifiedName qualifiedName, QualifiedIdentity newEntity )
    {
        entityState.setAssociation( qualifiedName, newEntity );
    }

    public Collection<QualifiedIdentity> getManyAssociation( QualifiedName qualifiedName )
    {
        return entityState.getManyAssociation( qualifiedName );
    }

    public Iterable<QualifiedName> propertyNames()
    {
        return entityState.propertyNames();
    }

    public Iterable<QualifiedName> associationNames()
    {
        return entityState.associationNames();
    }

    public Iterable<QualifiedName> manyAssociationNames()
    {
        return entityState.manyAssociationNames();
    }

    public void hasBeenApplied()
    {
        entityState.hasBeenApplied();
    }

    public ValueState newValueState( Map<QualifiedName, Object> values )
    {
        return entityState.newValueState( values );
    }
}