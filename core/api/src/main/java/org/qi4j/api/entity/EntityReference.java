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
import org.qi4j.api.util.NullArgumentException;

/**
 * An EntityReference is identity of a specific Entity instance.
 * <p>When stringified, the identity is used as-is. Example:</p>
 * <pre>123456-abcde</pre>
 */
public final class EntityReference
    implements Serializable
{
    /**
     * Parse an URI to an EntityReference.
     * @param uri the URI to parse
     * @return the EntityReference represented by the given URI
     */
    public static EntityReference parseURI( String uri )
    {
        String identity = uri.substring( "urn:qi4j:entity:".length() );
        return new EntityReference( identity );
    }

    /**
     * Parse an Entity identity to an EntityReference.
     * @param identity the EntityReference identity
     * @return the EntityReference represented by the given identity
     */
    public static EntityReference parseEntityReference( String identity )
    {
        return new EntityReference( identity );
    }

    /**
     * @param object an EntityComposite
     * @return the EntityReference for the given EntityComposite
     */
    public static EntityReference entityReferenceFor( Object object )
    {
        return new EntityReference( (EntityComposite) object );
    }

    private static final long serialVersionUID = 1L;

    private String identity;

    /**
     * @param entityComposite a non-null EntityComposite
     * @throws NullPointerException if entityComposite is null
     */
    public EntityReference( EntityComposite entityComposite )
    {
        this( entityComposite.identity().get() );
    }

    /**
     * @param identity reference identity
     * @throws NullArgumentException if identity is null or empty
     */
    public EntityReference( String identity )
    {
        NullArgumentException.validateNotEmpty( "identity", identity );
        this.identity = identity;
    }

    /**
     * @return This EntityReference identity.
     */
    public final String identity()
    {
        return identity;
    }

    /**
     * @return An URI representation of this EntityReference.
     */
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
        return identity.hashCode();
    }

    /**
     * @return This EntityReference identity.
     */
    @Override
    public String toString()
    {
        return identity;
    }
}
