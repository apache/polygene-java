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

package org.qi4j.spi.serialization;

import java.io.Serializable;
import org.qi4j.entity.EntityComposite;

/**
 * TODO
 */
public final class SerializedEntity
    implements Serializable
{
    private String identity;
    private Class<? extends EntityComposite> compositeType;

    public SerializedEntity( String identity, Class<? extends EntityComposite> clazz )
    {
        this.identity = identity;
        this.compositeType = clazz;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Class<? extends EntityComposite> getCompositeType()
    {
        return compositeType;
    }

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

        SerializedEntity that = (SerializedEntity) o;

        if( !compositeType.equals( that.compositeType ) )
        {
            return false;
        }
        if( !identity.equals( that.identity ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = identity.hashCode();
        result = 31 * result + compositeType.hashCode();
        return result;
    }

    @Override public String toString()
    {
        return compositeType.getName() + ":" + identity;
    }
}
