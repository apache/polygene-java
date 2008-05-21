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
package org.qi4j.entity.neo4j.state.indirect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.entity.neo4j.state.NeoEntityState;
import org.qi4j.entity.neo4j.state.direct.DirectEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.CompositeDescriptor;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectEntityState implements EntityState
{
    private final Map<String, Holder<Object>> properties = new HashMap<String, Holder<Object>>();
    private final Map<String, Holder<QualifiedIdentity>> associations = new HashMap<String, Holder<QualifiedIdentity>>();
    private final Map<String, IndirectCollection> manyAssociations = new HashMap<String, IndirectCollection>();
    private final long version;
    public final DirectEntityState underlyingState;
    private EntityStatus status;
    private QualifiedIdentity identity;

    IndirectEntityState( DirectEntityState underlyingState, CompositeDescriptor descriptor )
    {
        this.underlyingState = underlyingState;
        for( PropertyModel property : descriptor.getCompositeModel().getPropertyModels() )
        {
            String qName = property.getQualifiedName();
            properties.put( qName, new Holder<Object>( underlyingState.getProperty( qName ) ) );
        }
        for( AssociationModel model : descriptor.getCompositeModel().getAssociationModels() )
        {
            String qName = model.getQualifiedName();
            if( NeoEntityState.isManyAssociation( model ) )
            {
                manyAssociations.put( qName, null );
            }
            else
            {
                associations.put( qName, new Holder<QualifiedIdentity>( underlyingState.getAssociation( qName ) ) );
            }
        }
        this.version = underlyingState.getEntityVersion();
        this.status = underlyingState.getStatus();
        this.identity = underlyingState.getIdentity();
    }

    // NeoEntityState implementation

    public void prepareCommit()
    {
        if( status == EntityStatus.NEW )
        {
        }
        else if( status == EntityStatus.REMOVED )
        {
            underlyingState.remove();
            return;
        }
        // Store the state
        if( version != underlyingState.incNodeVersion() )
        { // will aquire the write lock
            throw new EntityStoreException( "Conflicting versions, the underlying representation has been modified. For :" + identity );
        }
        for( Map.Entry<String, Holder<Object>> property : properties.entrySet() )
        {
            Holder<Object> holder = property.getValue();
            if( holder.updated )
            {
                underlyingState.setProperty( property.getKey(), holder.value );
            }
        }
        for( Map.Entry<String, Holder<QualifiedIdentity>> association : associations.entrySet() )
        {
            Holder<QualifiedIdentity> holder = association.getValue();
            if( holder.updated )
            {
                underlyingState.setAssociation( association.getKey(), holder.value );
            }
        }
        for( IndirectCollection assoc : manyAssociations.values() )
        {
            assoc.prepareCommit();
        }
    }

    protected void storeProperty( String qualifiedName, Object value )
    {
        properties.get( qualifiedName ).set( value );
    }

    // EntityState implementation

    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    public EntityStatus getStatus()
    {
        return status;
    }

    public QualifiedIdentity getIdentity()
    {
        return identity;
    }

    public long getEntityVersion()
    {
        return version;
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

    public Iterable<String> getPropertyNames()
    {
        return Collections.unmodifiableCollection( properties.keySet() );
    }

    public Iterable<String> getAssociationNames()
    {
        return Collections.unmodifiableCollection( associations.keySet() );
    }

    public Iterable<String> getManyAssociationNames()
    {
        return Collections.unmodifiableCollection( manyAssociations.keySet() );
    }

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
