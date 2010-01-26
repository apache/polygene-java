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

package org.qi4j.api.entity;

import java.io.Serializable;

/**
 * An EntityIdentity is identity of a specific
 * Entity instance. When stringified, the identity is used as-is. Example:
 * <pre>
 * 123456-abcde
 * </pre>
 */
public final class EntityReference
    implements Serializable
{
    public static EntityReference parseURI( String uri )
    {
        String identity = uri.substring( "urn:qi4j:entity:".length() );
        return new EntityReference( identity );
    }

    public static EntityReference parseEntityReference( String id )
    {
        return new EntityReference( id );
    }

    public static EntityReference getEntityReference( Object o )
    {
        return new EntityReference( (EntityComposite) o );
    }

    private static final long serialVersionUID = 1L;

    private String identity;

    public EntityReference( EntityComposite entityComposite )
    {
        this( entityComposite.identity().get() );
    }

    public EntityReference( String identity )
    {
        this.identity = identity;
    }

    public final String identity()
    {
        return identity;
    }

    public String toURI()
    {
        return "urn:qi4j:entity:" + identity;
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
        EntityReference that = (EntityReference) o;
        return identity.equals( that.identity );
    }

    @Override
    public int hashCode()
    {
        int result;
        result = identity.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return identity;
    }
}
