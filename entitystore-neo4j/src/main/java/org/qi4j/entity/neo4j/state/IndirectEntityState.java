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
package org.qi4j.entity.neo4j.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityState implements CommittableEntityState
{
    // Cached state
    private final DirectEntityState state;
    private final long version;
    private EntityStatus status;

    // Properties and associations
    private final Map<String, Holder<Object>> properties = new HashMap<String, Holder<Object>>();
    private final Map<String, Holder<QualifiedIdentity>> associations = new HashMap<String, Holder<QualifiedIdentity>>();
    private final Map<String, IndirectCollection> manyAssociations = new HashMap<String, IndirectCollection>();
    private boolean loaded = false;

    public IndirectEntityState( DirectEntityState state )
    {
        this.state = state;
        this.version = state.getEntityVersion();
        this.status = state.getStatus();
    }

    // CommittableEntityState implementation

    public void preloadState()
    {
        if( !loaded )
        {
            loaded = true;
            for( String qName : getPropertyNames() )
            {
                properties.put( qName, new Holder<Object>( state.getProperty( qName ) ) );
            }
            for( String qName : getAssociationNames() )
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
        if( version != state.getEntityVersion() )
        {
            throw new EntityStoreException( "Conflicting versions, the underlying representation has been modified. For :" + getIdentity() );
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

    public QualifiedIdentity getIdentity()
    {
        return state.getIdentity();
    }

    public long getEntityVersion()
    {
        return version;
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
        return state.getPropertyNames();
    }

    public Iterable<String> getAssociationNames()
    {
        return state.getAssociationNames();
    }

    public Iterable<String> getManyAssociationNames()
    {
        return state.getManyAssociationNames();
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
