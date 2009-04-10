/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entitystore.neo4j.state;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.helpers.DefaultValueState;
import org.qi4j.spi.value.ValueState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityState implements CommittableEntityState
{
    // Cached state
    private final DirectEntityState state;
    private long version;
    private final long lastModified;
    private EntityStatus status;

    // Properties and associations
    private final Map<QualifiedName, Holder<Object>> properties = new HashMap<QualifiedName, Holder<Object>>();
    private final Map<QualifiedName, Holder<QualifiedIdentity>> associations = new HashMap<QualifiedName, Holder<QualifiedIdentity>>();
    private final Map<QualifiedName, IndirectCollection> manyAssociations = new HashMap<QualifiedName, IndirectCollection>();
    private boolean loaded = false;

    public IndirectEntityState( DirectEntityState state )
    {
        this.state = state;
        this.version = state.version();
        this.lastModified = state.lastModified();
        this.status = state.status();
    }

    // CommittableEntityState implementation

    public void preloadState()
    {
        if( !loaded )
        {
            loaded = true;
            for( QualifiedName qName : propertyNames() )
            {
                properties.put( qName, new Holder<Object>( state.getProperty( qName ) ) );
            }
            for( QualifiedName qName : associationNames() )
            {
                associations.put( qName, new Holder<QualifiedIdentity>( state.getAssociation( qName ) ) );
            }
            for( ManyAssociationFactory factory : getManyAssociationFactories() )
            {
                QualifiedName qName = factory.getQualifiedName();
                manyAssociations.put( qName, factory.createPreloadedCollection( state.getManyAssociation( qName ) ) );
            }
        }
    }

    public void prepareState()
    {
        state.preloadState();
    }

    public void prepareCommit()
    {
        if( status == EntityStatus.REMOVED )
        {
            state.remove();
            return;
        }
        if( version != state.version() )
        {
            throw new EntityStoreException( "Conflicting versions, the underlying representation has been modified. For :" + qualifiedIdentity() );
        }
        for( Map.Entry<QualifiedName, Holder<Object>> property : properties.entrySet() )
        {
            Holder<Object> holder = property.getValue();
            if( holder.updated )
            {
                state.setProperty( property.getKey(), holder.value );
            }
        }
        for( Map.Entry<QualifiedName, Holder<QualifiedIdentity>> association : associations.entrySet() )
        {
            Holder<QualifiedIdentity> holder = association.getValue();
            if( holder.updated )
            {
                state.setAssociation( association.getKey(), holder.value );
            }
        }
        for( IndirectCollection assoc : manyAssociations.values() )
        {
            assoc.prepareCommit();
        }
        state.prepareCommit();
    }

    public void prepareRemove()
    {
        state.prepareRemove();
    }

    public Iterable<ManyAssociationFactory> getManyAssociationFactories()
    {
        return state.getManyAssociationFactories();
    }

    public boolean isUpdated()
    {
        return true;  // TODO: implement some logic here
    }

    // EntityState implementation

    public QualifiedIdentity qualifiedIdentity()
    {
        return state.qualifiedIdentity();
    }

    public long version()
    {
        return version;
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

    public EntityType entityType()
    {
        return null;
    }

    public Iterable<QualifiedName> propertyNames()
    {
        return state.propertyNames();
    }

    public Iterable<QualifiedName> associationNames()
    {
        return state.associationNames();
    }

    public Iterable<QualifiedName> manyAssociationNames()
    {
        return state.manyAssociationNames();
    }

    public Object getProperty( QualifiedName qualifiedName )
    {
        return properties.get( qualifiedName ).value;
    }

    public void setProperty( QualifiedName qualifiedName, Object newValue )
    {
        properties.get( qualifiedName ).set( newValue );
    }

    public QualifiedIdentity getAssociation( QualifiedName qualifiedName )
    {
        return associations.get( qualifiedName ).value;
    }

    public void setAssociation( QualifiedName qualifiedName, QualifiedIdentity newEntity )
    {
        associations.get( qualifiedName ).set( newEntity );
    }

    public Collection<QualifiedIdentity> getManyAssociation( QualifiedName qualifiedName )
    {
        return manyAssociations.get( qualifiedName );
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version++;
    }

    public ValueState newValueState( Map<QualifiedName, Object> values )
    {
        return new DefaultValueState( values );
    }

    // Implementation internals

    private static class Holder<T>
    {
        T value;
        boolean updated = false;

        public Holder( T initialValue )
        {
            this.value = initialValue;
        }

        void set( T newValue )
        {
            value = newValue;
            updated = true;
        }
    }
}
