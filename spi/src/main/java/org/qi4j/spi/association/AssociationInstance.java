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

package org.qi4j.spi.association;

import java.lang.reflect.Type;
import org.qi4j.association.Association;
import org.qi4j.association.AssociationInfo;
import org.qi4j.association.AssociationVetoException;
import org.qi4j.entity.EntityComposite;

/**
 * Implementation of Association to a single Entity.
 */
public class AssociationInstance<T>
    implements Association<T>
{
    private AssociationInfo associationInfo;
    private T value;

    public AssociationInstance( AssociationInfo associationInfo, T value )
    {
        this.associationInfo = associationInfo;
        this.value = value;
    }

    // Association implementation
    public T get()
    {
        return value;
    }

    public void set( T newValue )
        throws AssociationVetoException
    {
        if( !( newValue instanceof EntityComposite ) )
        {
            throw new AssociationVetoException( "Associated value must be an EntityComposite" );
        }

        this.value = newValue;
    }

    // AssociationInfo implementation
    public <T> T getAssociationInfo( Class<T> infoType )
    {
        return associationInfo.getAssociationInfo( infoType );
    }

    public String getName()
    {
        return associationInfo.getName();
    }

    public String getQualifiedName()
    {
        return associationInfo.getQualifiedName();
    }

    public Type getAssociationType()
    {
        return associationInfo.getAssociationType();
    }

    public void write( T value )
    {
        this.value = value;
    }

    public T read()
    {
        return value;
    }

    @Override public String toString()
    {
        if( value == null )
        {
            return "";
        }
        else
        {
            return value.toString();
        }
    }

    @Override public int hashCode()
    {
        if( value == null )
        {
            return 0;
        }
        else
        {
            return value.hashCode();
        }
    }

    @Override public boolean equals( Object o )
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

        if( value != null ? !value.equals( that.value ) : that.value != null )
        {
            return false;
        }

        return true;
    }
}
