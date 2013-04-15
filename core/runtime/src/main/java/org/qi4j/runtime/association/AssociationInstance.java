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

package org.qi4j.runtime.association;

import java.lang.reflect.Type;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationWrapper;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.functional.Function2;

/**
 * Implementation of Association to a single Entity.
 */
public final class AssociationInstance<T>
    extends AbstractAssociationInstance<T>
    implements Association<T>
{
    private Property<EntityReference> associationState;

    public AssociationInstance( AssociationInfo associationInfo,
                                Function2<EntityReference, Type, Object> entityFunction,
                                Property<EntityReference> associationState
    )
    {
        super( associationInfo, entityFunction );
        this.associationState = associationState;
    }

    // Association implementation
    @Override
    public T get()
    {
        return getEntity( associationState.get() );
    }

    @Override
    public void set( T newValue )
        throws IllegalArgumentException
    {
        checkImmutable();
        checkType( newValue );

        associationInfo.checkConstraints( newValue );

        // Change association
        associationState.set( getEntityReference( newValue ) );
    }

    public Property<EntityReference> getAssociationState()
    {
        return associationState;
    }

    @Override
    public String toString()
    {
        if( associationState.get() == null )
        {
            return "";
        }
        else
        {
            return associationState.get().toString();
        }
    }

    @Override
    public int hashCode()
    {
        int hash = associationInfo.hashCode() * 61; // Descriptor
        if( associationState.get() != null )
        {
            hash += associationState.get().hashCode() * 3; // State
        }
        return hash;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        Association<?> that = (Association) o;
        // Unwrap if needed
        while( that instanceof AssociationWrapper )
        {
            that = ( (AssociationWrapper) that ).next();
        }
        // Descriptor equality
        AssociationInstance<?> thatInstance = (AssociationInstance) that;
        AssociationDescriptor thatDescriptor = (AssociationDescriptor) thatInstance.associationInfo();
        if( !associationInfo.equals( thatDescriptor ) )
        {
            return false;
        }
        // State equality
        if( associationState.get() != null
            ? !associationState.get().equals( thatInstance.associationState.get() )
            : thatInstance.associationState.get() != null )
        {
            return false;
        }
        return true;
    }
}
