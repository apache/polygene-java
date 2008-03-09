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
package org.qi4j.entity.ibatis.internal.entityState;

import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.entity.ibatis.internal.IBatisEntityStateDao;
import org.qi4j.entity.ibatis.internal.IBatisEntityStateStatus;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.StoreException;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link org.qi4j.spi.entity.EntityState}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class EntityState
    implements IBatisEntityState
{
    private final String identity;

    private final CompositeBinding compositeBinding;
    private final Map<String, Property> properties;
    private final Map<String, AbstractAssociation> associations;
    private final IBatisEntityStateDao dao;
    private IBatisEntityStateStatus status;

    /**
     * Construct an instance of {@code EntityState}.
     *
     * @param anIdentity        The identity. This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @param propertiez        The entity properties. This argument must not be {@code null}.
     * @param associationz      The entity associations. This argument must not be {@code null}.
     * @param aStatus           The initial status of this state. This argument must not be {@code null}.
     * @param aDao              The entity store. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    public EntityState(
        String anIdentity, CompositeBinding aCompositeBinding,
        Map<String, Property> propertiez,
        Map<String, AbstractAssociation> associationz,
        IBatisEntityStateStatus aStatus, IBatisEntityStateDao aDao )
        throws IllegalArgumentException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );
        validateNotNull( "propertiez", propertiez );
        validateNotNull( "associationz", associationz );
        validateNotNull( "aStatus", aStatus );
        validateNotNull( "aDao", aDao );

        identity = anIdentity;
        compositeBinding = aCompositeBinding;
        properties = propertiez;
        associations = associationz;
        dao = aDao;
        status = aStatus;
    }

    /**
     * Returns the identity of the entity that this EntityState represents.
     *
     * @return the identity of the entity that this EntityState represents.
     * @since 0.1.0
     */
    public final String getIdentity()
    {
        return identity;
    }

    public final CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public final Map<String, Property> getProperties()
    {
        return properties;
    }

    public final Map<String, AbstractAssociation> getAssociations()
    {
        return associations;
    }

    public final void refresh()
    {
        // Check whether refresh is required at all
        if( status == IBatisEntityStateStatus.statusNew || status == IBatisEntityStateStatus.statusNewToDeleted || status == IBatisEntityStateStatus.statusLoadToDeleted )
        {
            return;
        }

        // TODO
    }

    public boolean delete()
        throws StoreException
    {
        switch( status )
        {
        case statusNew:
        case statusNewToDeleted:
        case statusLoadToDeleted:
            status = IBatisEntityStateStatus.statusNewToDeleted;
            return true;

        case statusLoadFromDb:
            return dao.deleteComposite( identity, compositeBinding );
        }

        return false;
    }

    /**
     * Persist this entity state.
     *
     * @since 0.1.0
     */
    public final void persist()
    {
        // TODO
    }
}