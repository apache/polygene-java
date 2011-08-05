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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.functional.Function2;
import org.qi4j.runtime.composite.ConstraintsCheck;

import java.lang.reflect.Type;

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
        super( associationInfo, entityFunction);
        this.associationState = associationState;
    }

    // Association implementation
    public T get()
    {
        return getEntity( associationState.get() );
    }

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
        if( associationState.get() == null )
        {
            return 0;
        }
        else
        {
            return associationState.get().hashCode();
        }
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

        AssociationInstance that = (AssociationInstance) o;

        if( associationState.get() != null ? !associationState.get().equals( that.associationState.get() ) : that.associationState.get() != null )
        {
            return false;
        }

        return true;
    }
}
