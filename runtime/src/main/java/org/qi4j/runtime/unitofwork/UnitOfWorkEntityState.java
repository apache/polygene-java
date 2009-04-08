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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.helpers.DefaultEntityState;

import java.util.Collection;
import java.util.Map;

/**
 * JAVADOC
 */
class UnitOfWorkEntityState
    extends DefaultEntityState
{
    private final EntityState parentState;

    UnitOfWorkEntityState( long entityVersion, long lastModified,
                           QualifiedIdentity identity,
                           EntityStatus status,
                           EntityType entityType,
                           Map<QualifiedName, Object> properties,
                           Map<QualifiedName, QualifiedIdentity> associations,
                           Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations,
                           EntityState parentState )
    {
        super( entityVersion, lastModified, identity, status, entityType, properties, associations, manyAssociations );
        this.parentState = parentState;
    }

    public Object getProperty( QualifiedName qualifiedName )
    {
        if( properties.containsKey( qualifiedName ) )
        {
            return properties.get( qualifiedName );
        }

        // Get from parent state
        return parentState == null ? null : parentState.getProperty( qualifiedName );
    }

    public QualifiedIdentity getAssociation( QualifiedName qualifiedName )
    {
        if( associations.containsKey( qualifiedName ) )
        {
            return associations.get( qualifiedName );
        }

        return parentState == null ? null : parentState.getAssociation( qualifiedName );
    }

    public Collection<QualifiedIdentity> getManyAssociation( QualifiedName qualifiedName )
    {
        if( manyAssociations.containsKey( qualifiedName ) )
        {
            return manyAssociations.get( qualifiedName );
        }

        return parentState == null ? null : parentState.getManyAssociation( qualifiedName );
    }

    public EntityState getParentState()
    {
        return parentState;
    }

    public boolean isChanged()
    {
        return properties.size() > 0 || associations.size() > 0 || manyAssociations.size() > 0;
    }

    public void mergeTo( EntityState state )
    {
        // Merge Properties
        for( Map.Entry<QualifiedName, Object> entry : properties.entrySet() )
        {
            state.setProperty( entry.getKey(), entry.getValue() );
        }

        // Merge Associations
        for( Map.Entry<QualifiedName, QualifiedIdentity> entry : associations.entrySet() )
        {
            state.setAssociation( entry.getKey(), entry.getValue() );
        }

        // Merge ManyAssociations
        for( Map.Entry<QualifiedName, Collection<QualifiedIdentity>> entry : manyAssociations.entrySet() )
        {
            // TODO More intelligent merging
            Collection<QualifiedIdentity> entities = state.getManyAssociation( entry.getKey() );
            entities.clear();
            entities.addAll( entry.getValue() );
        }
    }
}
