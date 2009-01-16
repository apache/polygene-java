/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity.association;

import java.lang.reflect.Type;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.unitofwork.AssociationStateChange;
import org.qi4j.api.unitofwork.StateChangeListener;
import org.qi4j.api.unitofwork.StateChangeVoter;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Implementation of Association to a single Entity.
 */
public final class AssociationInstance<T> extends AbstractAssociationInstance<T>
    implements Association<T>
{
    private static final Object NOT_LOADED = new Object();

    private T value = (T) NOT_LOADED;
    private EntityState entityState;

    public AssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, EntityState entityState )
    {
        super( associationInfo, unitOfWork );
        this.entityState = entityState;
        if( entityState == null )
        {
            value = null;
        }
    }

    // Association implementation
    public T get()
    {
        if( !isSet() )
        {
            QualifiedIdentity entityId = entityState.getAssociation( qualifiedName() );
            value = getEntity( entityId );
        }
        return value;
    }

    public void set( T newValue )
        throws IllegalArgumentException
    {
        checkImmutable();
        checkType( newValue );

        AssociationModel associationModel = (AssociationModel) associationInfo;
        associationModel.checkConstraints( newValue );

        // Allow voters to vote on change
        Iterable<StateChangeVoter> stateChangeVoters = unitOfWork.stateChangeVoters();
        AssociationStateChange change = null;
        if( stateChangeVoters != null )
        {
            change = new AssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), (Entity) newValue, (Entity) get() );

            for( StateChangeVoter stateChangeVoter : stateChangeVoters )
            {
                stateChangeVoter.acceptChange( change );
            }
        }

        Iterable<StateChangeListener> stateChangeListeners = unitOfWork.stateChangeListeners();
        if( stateChangeListeners != null )
        {
            // Have to create this here in order to get old value
            change = new AssociationStateChange( entityState.qualifiedIdentity().identity(), qualifiedName(), (Entity) newValue, (Entity) get() );
        }

        // Change association
        if( entityState != null )
        {
            entityState.setAssociation( qualifiedName(), getEntityId( newValue ) );
        }
        this.value = newValue;

        // Notify listeners
        if( stateChangeListeners != null )
        {
            for( StateChangeListener stateChangeListener : stateChangeListeners )
            {
                stateChangeListener.notify( change );
            }
        }
    }

    protected boolean isSet()
    {
        return value != NOT_LOADED;
    }

    // AssociationInfo implementation
    public <T> T metaInfo( Class<T> infoType )
    {
        return associationInfo.metaInfo( infoType );
    }

    public String name()
    {
        return associationInfo.name();
    }

    public String qualifiedName()
    {
        return associationInfo.qualifiedName();
    }

    public Type type()
    {
        return associationInfo.type();
    }

    public void write( T value )
    {
        this.value = value;
    }

    public T read()
    {
        return value;
    }

    @Override public String toString()
    {
        if( value == null )
        {
            return "";
        }
        else
        {
            return value.toString();
        }
    }

    @Override public int hashCode()
    {
        if( value == null )
        {
            return 0;
        }
        else
        {
            return value.hashCode();
        }
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AssociationInstance that = (AssociationInstance) o;

        if( value != null ? !value.equals( that.value ) : that.value != null )
        {
            return false;
        }

        return true;
    }

    public void refresh( EntityState newState )
    {
        value = (T) NOT_LOADED;
        entityState = newState;
    }
}
