/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.api.entity;

import java.io.Serializable;
import java.util.Objects;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.util.NullArgumentException;

/**
 * An EntityReference is reference of a specific Entity instance.
 * <p>When stringified, the reference is used as-is. Example:</p>
 * <pre>123456-abcde</pre>
 */
public final class EntityReference
        implements Serializable
{
    /**
     * Parse an Entity reference to an EntityReference.
     *
     * @param identityString the EntityReference reference
     * @return the EntityReference represented by the given reference
     */
    public static EntityReference parseEntityReference(String identityString)
    {
        return new EntityReference( new StringIdentity( identityString ) );
    }

    /**
     * @param object an EntityComposite
     * @return the EntityReference for the given EntityComposite
     */
    public static EntityReference entityReferenceFor(Object object)
    {
        return new EntityReference(((EntityComposite) object).identity().get());
    }

    public static EntityReference create(Identity identity)
    {
        if (identity == null)
        {
            return null;
        }
        return new EntityReference(identity);
    }

    private static final long serialVersionUID = 1L;

    private Identity identity;

    /**
     * @param identity reference reference
     * @throws NullArgumentException if reference is null or empty
     */
    private EntityReference( Identity identity )
    {
        Objects.requireNonNull(identity,"reference must not be null");
        this.identity = identity;
    }

    /**
     *
     * @return The reference of the Entity that this EntityReference.is referring to
     */
    public final Identity identity()
    {
        return identity;
    }

    /**
     * @return An URI representation of this EntityReference.
     */
    public String toURI()
    {
        return "urn:polygene:entity:" + identity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        EntityReference that = (EntityReference) o;
        return identity.equals(that.identity);
    }

    @Override
    public int hashCode()
    {
        return identity.hashCode();
    }

    @Override
    public String toString()
    {
        return identity.toString();
    }
}
