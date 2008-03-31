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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.property.Property;

/**
 * TODO
 */
public final class PropertyModel
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static String getName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( idx + 1 );
    }

    public static String getDeclaringClassName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( 0, idx + 1 );
    }

    public static String getQualifiedName( Method accessor )
    {
        return accessor.getDeclaringClass().getName() + ":" + accessor.getName();
    }

    public static String getQualifiedName( Class declaringClass, String name )
    {
        return declaringClass.getName() + ":" + name;
    }

    public static Type getPropertyType( Method accessor )
    {
        return getPropertyType( accessor.getGenericReturnType() );
    }

    private static Type getPropertyType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( Property.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        Type[] interfaces = ( (Class) methodReturnType ).getInterfaces();
        for( Type anInterface : interfaces )
        {
            Type propertyType = getPropertyType( anInterface );
            if( propertyType != null )
            {
                return propertyType;
            }
        }
        return null;
    }

    private String name;
    private Type type;
    private Method accessor; // Interface accessor
    private String qualifiedName;

    public PropertyModel( Method anAccessor )
    {
        name = anAccessor.getName();
        type = getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = getQualifiedName( anAccessor );
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
        // TODO: Shall the URI contain the type (property), or is it always understood in a larger context??
        return "urn:qi4j:property:" + getQualifiedName();
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
