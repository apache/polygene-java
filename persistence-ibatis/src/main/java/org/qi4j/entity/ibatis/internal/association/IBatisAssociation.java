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

import org.qi4j.association.Association;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.Identity;
import static org.qi4j.entity.ibatis.internal.util.Util.isNotEquals;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.property.AssociationBinding;

/**
 * {@code IBatisAssociation} provides implementation of {@code Association}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisAssociation<T> extends IBatisAbstractAssociationInstance
    implements Association<T>
{
    private final EntitySession session;

    private String valueIdentity;
    private T value;
    private boolean isDirty;

    /**
     * Construct an instance of {@code IBatisAbstractAssociationInstance}.
     *
     * @param anInitialIdentity The initial identity.
     * @param aBinding          The association binding. This argument must not be {@code null}.
     * @param aSession          The entity session. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or both {@code aBinding} and {@code aSession} arguments are
     *                                  {@code null}.
     * @since 0.1.0
     */
    public IBatisAssociation( String anInitialIdentity, AssociationBinding aBinding, EntitySession aSession )
        throws IllegalArgumentException
    {
        super( aBinding );

        validateNotNull( "aBinding", aBinding );
        validateNotNull( "aSession", aSession );
        valueIdentity = anInitialIdentity;
        session = aSession;
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

        // Retrieves the 
        AssociationResolution associationResolution = associationBinding.getAssociationResolution();
        AssociationModel model = associationResolution.getAssociationModel();
        Class modelType = (Class) model.getType();
        value = (T) session.getReference( valueIdentity, modelType );
        return value;
    }

    /**
     * Sets the new association value.
     *
     * @param aNewAssociationValue A new association value.
     */
    public void set( T aNewAssociationValue )
    {
        if( !isDirty )
        {
            isDirty = isNotEquals( value, aNewAssociationValue );
        }

        value = aNewAssociationValue;
        if( aNewAssociationValue == null )
        {
            valueIdentity = null;
        }
        else
        {
            Identity hasIdentity = (Identity) aNewAssociationValue;
            valueIdentity = hasIdentity.identity().get();
        }
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
        // TODO: Should we check whether the entity still exists?
        // TODO: In the case of cross entity store occured

        if( !isDirty )
        {
            return;
        }


    }
}