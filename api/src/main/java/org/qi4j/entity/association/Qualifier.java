/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.entity.association;

import java.io.Serializable;
import org.qi4j.entity.EntityComposite;

/**
 * Implementation of Association Role Qualifiers. Qualifiers allow Entities in an Association
 * to have Qualifier information associated with it. The Qualifier must be an Entity itself.
 */
public final class Qualifier<E, Q>
    implements Serializable
{
    public static <E, Q> Qualifier qualifier( E entity, Q qualifier )
    {
        if( !( entity instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Associated object must an EntityComposite" );
        }

        if( !( qualifier instanceof EntityComposite ) )
        {
            throw new IllegalArgumentException( "Qualifier object must an EntityComposite" );
        }

        return new Qualifier<E, Q>( entity, qualifier );
    }

    private E entity;
    private Q qualifier;

    private Qualifier( E entity, Q qualifier )
    {
        this.entity = entity;
        this.qualifier = qualifier;
    }

    /**
     * Get the Entity of the Qualifier
     *
     * @return
     */
    public E entity()
    {
        return entity;
    }

    /**
     * Get the Qualifier part of this Qualifier
     *
     * @return
     */
    public Q qualifier()
    {
        return qualifier;
    }
}
