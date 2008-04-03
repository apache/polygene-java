/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.entity.ibatis.internal.association;

import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.ibatis.internal.common.Status;
import static org.qi4j.entity.ibatis.internal.common.Util.*;
import org.qi4j.runtime.entity.association.AssociationInstance;
import org.qi4j.spi.entity.association.AssociationBinding;

/**
 * {@code IBatisAssociation} provides implementation of {@code Association}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisAssociation<T> extends AssociationInstance
{
    private final UnitOfWork unitOfWork;

    private String valueIdentity;
    private T value;
    private boolean isDirty;
    private Status status;

    /**
     * Construct an instance of {@code IBatisAbstractAssociationInstance}.
     *
     * @param anInitialIdentity The initial identity.
     * @param aBinding          The association binding. This argument must not be {@code null}.
     * @param aStatus           The status of this association. This argument must not be {@code null}.
     * @param unitOfWork        The unit of work. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or both {@code aBinding} and {@code unitOfWork} arguments are
     *                                  {@code null}.
     * @since 0.1.0
     */
    public IBatisAssociation(
        String anInitialIdentity, AssociationBinding aBinding,
        Status aStatus, UnitOfWork unitOfWork )
        throws IllegalArgumentException
    {
        super( aBinding, null, null );

        validateNotNull( "aBinding", aBinding );
        validateNotNull( "aStatus", aStatus );
        validateNotNull( "unitOfWork", unitOfWork );

        valueIdentity = anInitialIdentity;
        status = aStatus;
        this.unitOfWork = unitOfWork;
        isDirty = false;
    }

    /**
     * Returns the association value.
     *
     * @return The association value.
     * @since 0.1.0
     */
    @SuppressWarnings( "unchecked" )
    public final T get()
    {
        if( valueIdentity == null )
        {
            return null;
        }

        // If retrieved before
        if( value != null )
        {
            return value;
        }

        // Retrieves the value given the value identity
        value = (T) unitOfWork.getReference( valueIdentity, (Class<? extends EntityComposite>) type() );
        return value;
    }

    /**
     * Sets the new association value.
     *
     * @param aNewAssociationValue A new association value.
     */
    public T set( T aNewAssociationValue )
    {
        Identity newAssociationIdentity = (Identity) aNewAssociationValue;
        if( !isDirty )
        {
            // If value is not fetched before
            if( value != null )
            {
                isDirty = isNotEquals( value, aNewAssociationValue );
            }
            else if( valueIdentity != null )
            {
                String associationIdentity = null;
                if( aNewAssociationValue != null )
                {
                    associationIdentity = newAssociationIdentity.identity().get();
                }
                isDirty = isNotEquals( valueIdentity, associationIdentity );
            }
        }

        value = aNewAssociationValue;

        if( aNewAssociationValue == null )
        {
            valueIdentity = null;
        }
        else
        {
            valueIdentity = newAssociationIdentity.identity().get();
        }
        isDirty = true;

        return aNewAssociationValue;
    }

    /**
     * Returns {@code true} if this {@code IBatisMutablePropertyInstance} instance is dirty, {@code false} otherwise.
     *
     * @return A {@code boolean} indicator whether this {@code IBatisMutablePropertyInstance} instance is dirty.
     * @since 0.1.0
     */
    public final boolean isDirty()
    {
        return isDirty;
    }

    /**
     * Marks this {@code IBatisMutablePropertyInstance} clean.
     *
     * @since 0.1.0
     */
    public final void markAsClean()
    {
        isDirty = false;
    }

    /**
     * Complete the persistence for this assocation instance.
     *
     * @since 0.1.0
     */
    public final void complete()
    {
        // TODO: Should we check whether the entity pointed by this association exists?
        if( !isDirty )
        {
            return;
        }

        // TODO: How could this trigger for the other association to complete ?

        // Persistent for this type of association that doesn't have any properties is done by entity state.

        // TODO: Persistence for this type of association that has some properties.
        // e.g. in the test case of primary contact, we also wants to capture reasoning why this person is
        // the primary contact.
        markAsClean();
    }
}