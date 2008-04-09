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

package org.qi4j.spi.property;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.property.ComputedPropertyInstance;

/**
 * TODO
 */
public final class PropertyModel
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;
    private Type type;
    private Method accessor; // Interface accessor
    private String qualifiedName;

    /**
     * Get URI for a property.
     *
     * @param accessor accessor method
     * @return property URI
     */
    public static String toURI( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:property:" + ComputedPropertyInstance.getQualifiedName( accessor );
    }

    /**
     * Get namespace for a property.
     *
     * @param accessor accessor method
     * @return property namespace
     */
    public static String toNamespace( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:property:" + ComputedPropertyInstance.getDeclaringClassName( accessor ) + ":";
    }

    public PropertyModel( Method anAccessor )
    {
        name = anAccessor.getName();
        type = ComputedPropertyInstance.getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = ComputedPropertyInstance.getQualifiedName( anAccessor );
    }

    public String getName()
    {
        return name;
    }

    /**
     * The qualified name of a Property is constructed as follows:
     * <name of declaring class>:<name of property>
     * <p/>
     * Example:
     * com.mycompany.Person:firstName
     *
     * @return the qualified name of the property
     */
    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public Type getType()
    {
        return type;
    }

    public Method getAccessor()
    {
        return accessor;
    }

    public String toURI()
    {
        return toURI( accessor );
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

        PropertyModel that = (PropertyModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override public String toString()
    {
        return accessor.toGenericString();
    }
}
