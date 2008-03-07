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
package org.qi4j.entity.ibatis;

import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StoreException;

/**
 * {@code IBatisEntityState} represents {@code IBatis} version of {@link EntityState}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
final class IBatisEntityState
    implements EntityState
{
    private final String identity;
    private final CompositeBinding compositeBinding;

    /**
     * Construct an instance of {@code IBatisEntityState}.
     *
     * @param anIdentity        The identity. This argument must not be {@code null}.
     * @param aCompositeBinding The composite binding. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    IBatisEntityState( String anIdentity, CompositeBinding aCompositeBinding )
        throws IllegalArgumentException
    {
        validateNotNull( "anIdentity", anIdentity );
        validateNotNull( "aCompositeBinding", aCompositeBinding );

        identity = anIdentity;
        compositeBinding = aCompositeBinding;
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

    public CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public Map<String, Property> getProperties()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, AbstractAssociation> getAssociations()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void refresh()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean delete() throws StoreException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
