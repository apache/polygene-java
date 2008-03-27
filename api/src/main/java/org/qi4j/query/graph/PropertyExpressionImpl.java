/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.query.graph;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An expression related to {@link org.qi4j.property.Property}.
 */
public class PropertyExpressionImpl
    implements PropertyExpression
{

    /**
     * Property name.
     */
    private final String name;
    /**
     * Interface that declared the property.
     */
    private final Class declaringType;
    /**
     * Property type.
     */
    private final Class type;

    /**
     * Constructor.
     *
     * @param name          property name; cannot be null
     * @param declaringType type that declared the property; cannot be null
     * @param type;         property type
     */
    public PropertyExpressionImpl( final String name,
                                   final Class declaringType,
                                   final Class type )
    {
        this.name = name;
        this.declaringType = declaringType;
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param propertyMethod method that acts as property
     */
    public PropertyExpressionImpl( final Method propertyMethod )
    {
        name = propertyMethod.getName();
        declaringType = propertyMethod.getDeclaringClass();
        Type returnType = propertyMethod.getGenericReturnType();
        if( !( returnType instanceof ParameterizedType ) )
        {
            throw new UnsupportedOperationException( "Unsupported property type:" + returnType );
        }
        Type propertyTypeAsType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        if( !( propertyTypeAsType instanceof Class ) )
        {
            throw new UnsupportedOperationException( "Unsupported property type:" + propertyTypeAsType );
        }
        type = (Class) propertyTypeAsType;
    }

    /**
     * Get the name of the property, which is equal to the name of the method that declared it.
     *
     * @return the name of the property
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the type of the interface that declared the property.
     *
     * @return the type of property that declared the property
     */
    public Class getDeclaringType()
    {
        return declaringType;
    }

    /**
     * Get the type of the property. If the property is declared as Property<X> then X is returned.
     *
     * @return the property type
     */
    public Class getType()
    {
        return type;
    }

    @Override public String toString()
    {
        return new StringBuilder()
            .append( declaringType.getName() )
            .append( "." )
            .append( name )
            .append( "()^^" )
            .append( type.getName() )
            .toString();
    }

}