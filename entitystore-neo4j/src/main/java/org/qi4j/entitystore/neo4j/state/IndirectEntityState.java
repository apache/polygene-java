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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityState
    //implements CommittableEntityState
{
    // Cached state
    private final DirectEntityState state;
    private long version;
    private final long lastModified;
    private EntityStatus status;

    // Properties and manyAssociations
    private final Map<QualifiedName, Holder<String>> properties = new HashMap<QualifiedName, Holder<String>>();
    private final Map<QualifiedName, Holder<EntityReference>> associations = new HashMap<QualifiedName, Holder<EntityReference>>();
    private final Map<QualifiedName, IndirectCollection> manyAssociations = new HashMap<QualifiedName, IndirectCollection>();
    private boolean loaded = false;

    public IndirectEntityState(DirectEntityState state)
    {
        this.state = state;
        this.version = state.version();
        this.lastModified = state.lastModified();
        this.status = state.status();
    }

    // CommittableEntityState implementation

    public void preloadState()
    {
/* TODO
        if( !loaded )
        {
            loaded = true;
            for( QualifiedName qName : propertyTypes() )
            {
                properties.put( qName, new Holder<Object>( state.getProperty( qName ) ) );
            }
            for( QualifiedName qName : associationTypes() )
            {
                manyAssociations.put( qName, new Holder<EntityReference>( state.getAssociation( qName ) ) );
            }
            for( ManyAssociationFactory factory : getManyAssociationFactories() )
            {
                StateName qName = factory.getStateName();
                manyAssociations.put( qName, factory.createPreloadedCollection( state.getManyAssociation( qName ) ) );
            }
        }
*/
    }

    public void prepareState()
    {
        state.preloadState();
    }

    public void prepareCommit()
    {
/* TODO
        if( status == EntityStatus.REMOVED )
        {
            state.remove();
            return;
        }
        if( version != state.version() )
        {
            throw new EntityStoreException( "Conflicting versions, the underlying representation has been modified. For :" + identity() );
        }
        for( Map.Entry<QualifiedName, Holder<Object>> property : properties.entrySet() )
        {
            Holder<Object> holder = property.getValue();
            if( holder.updated )
            {
                state.setProperty( property.getKey(), holder.value );
            }
        }
        for( Map.Entry<QualifiedName, Holder<EntityReference>> association : manyAssociations.entrySet() )
        {
            Holder<EntityReference> holder = association.getValue();
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
*/
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

    public EntityReference identity()
    {
        return state.identity();
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

    public String getProperty(QualifiedName qualifiedName)
    {
        return properties.get(qualifiedName).value;
    }

    public void setProperty(QualifiedName qualifiedName, String newValue)
    {
        properties.get(qualifiedName).set(newValue);
    }

    public EntityReference getAssociation(QualifiedName qualifiedName)
    {
        return associations.get(qualifiedName).value;
    }

    public void setAssociation(QualifiedName qualifiedName, EntityReference newEntity)
    {
        associations.get(qualifiedName).set(newEntity);
    }

    public ManyAssociationState getManyAssociation(QualifiedName qualifiedName)
    {
        return null; // manyAssociations.get(qualifiedName);
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version++;
    }

    // Implementation internals

    private static class Holder<T>
    {
        T value;
        boolean updated = false;

        public Holder(T initialValue)
        {
            this.value = initialValue;
        }

        void set(T newValue)
        {
            value = newValue;
            updated = true;
        }
    }
}
