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

package org.qi4j.runtime.association;

import org.qi4j.association.Association;
import org.qi4j.association.AssociationVetoException;
import org.qi4j.spi.property.AssociationBinding;

/**
 * TODO
 */
public class AssociationInstance<T>
    implements Association<T>
{
    private AssociationBinding associationBinding;
    private T value;

    public AssociationInstance( AssociationBinding associationBinding, T value )
    {
        this.associationBinding = associationBinding;
        this.value = value;
    }

    // ReadableAssociation
    public T get()
    {
        return value;
    }

    // WritableAssociation
    public void set( T newValue )
        throws AssociationVetoException
    {
        this.value = newValue;
    }

    // AssociationInfo
    public <T> T getAssociationInfo( Class<T> infoType )
    {
        return associationBinding.getAssociationInfo( infoType );
    }

    public String getName()
    {
        return associationBinding.getAssociationResolution().getAssociationModel().getName();
    }

    public String getQualifiedName()
    {
        return associationBinding.getAssociationResolution().getAssociationModel().getQualifiedName();
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
}
