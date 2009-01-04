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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityState implements CommittableEntityState
{
    // Cached state
    private final DirectEntityState state;
    private final long version;
    private final long lastModified;
    private EntityStatus status;

    // Properties and associations
    private final Map<String, Holder<Object>> properties = new HashMap<String, Holder<Object>>();
    private final Map<String, Holder<QualifiedIdentity>> associations = new HashMap<String, Holder<QualifiedIdentity>>();
    private final Map<String, IndirectCollection> manyAssociations = new HashMap<String, IndirectCollection>();
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
            for( String qName : propertyNames() )
            {
                properties.put( qName, new Holder<Object>( state.getProperty( qName ) ) );
            }
            for( String qName : associationNames() )
            {
                associations.put( qName, new Holder<QualifiedIdentity>( state.getAssociation( qName ) ) );
            }
            for( ManyAssociationFactory factory : getManyAssociationFactories() )
            {
                String qName = factory.getQualifiedName();
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
        for( Map.Entry<String, Holder<Object>> property : properties.entrySet() )
        {
            Holder<Object> holder = property.getValue();
            if( holder.updated )
            {
                state.setProperty( property.getKey(), holder.value );
            }
        }
        for( Map.Entry<String, Holder<QualifiedIdentity>> association : associations.entrySet() )
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

    public Iterable<String> propertyNames()
    {
        return state.propertyNames();
    }

    public Iterable<String> associationNames()
    {
        return state.associationNames();
    }

    public Iterable<String> manyAssociationNames()
    {
        return state.manyAssociationNames();
    }

    public Object getProperty( String qualifiedName )
    {
        return properties.get( qualifiedName ).value;
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        properties.get( qualifiedName ).set( newValue );
    }

    public QualifiedIdentity getAssociation( String qualifiedName )
    {
        return associations.get( qualifiedName ).value;
    }

    public void setAssociation( String qualifiedName, QualifiedIdentity newEntity )
    {
        associations.get( qualifiedName ).set( newEntity );
    }

    public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
    {
        return manyAssociations.get( qualifiedName );
    }

    public Collection<QualifiedIdentity> setManyAssociation( String qualifiedName, Collection<QualifiedIdentity> newManyAssociation )
    {
        throw new UnsupportedOperationException();
    }

    public void markAsLoaded()
    {
        status = EntityStatus.LOADED;
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
