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

package org.qi4j.spi.entity.association;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.property.ComputedPropertyInstance;

/**
 * TODO
 */
public final class AssociationModel
    implements Serializable
{
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

    public static String getDeclaringClassName( Method accessor )
    {
        return accessor.getDeclaringClass().getName();
    }

    public static String getQualifiedName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }

    /**
     * Get URI for an association.
     *
     * @param accessor accessor method
     * @return association URI
     */
    public static String toURI( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:association:" + getQualifiedName( accessor );
    }

    /**
     * Get namespace for an association.
     *
     * @param accessor accessor method
     * @return association namespace
     */
    public static String toNamespace( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:association:" + ComputedPropertyInstance.getDeclaringClassName( accessor ) + ":";
    }

    private String name;
    private Type type;
    private Method accessor; // Interface accessor
    private String qualifiedName;

    public AssociationModel( Type type, Method accessor )
    {
        this.name = accessor.getName();
        this.type = type;
        this.accessor = accessor;
        qualifiedName = getQualifiedName( accessor );
    }

    public String getName()
    {
        return name;
    }

    /**
     * The qualified name of an Association is constructed as follows:
     * <name of declaring class>:<name of association>
     * <p/>
     * Example:
     * com.mycompany.Person:father
     *
     * @return the qualified name of the association
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

    @Override public String toString()
    {
        return qualifiedName;
    }

    public String toURI()
    {
        return toURI( accessor );
    }

}
