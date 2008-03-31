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
package org.qi4j.query.grammar.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.query.grammar.PropertyReference;

/**
 * Default {@link org.qi4j.query.grammar.PropertyReference} implementation.
 *
 * @author Alin Dreghiciu
 * @since March 28, 2008
 */
public class PropertyReferenceImpl
    implements PropertyReference
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
     * Traversed association.
     */
    private final AssociationReference traversed;

    /**
     * Constructor.
     *
     * @param name          property name; cannot be null
     * @param declaringType type that declared the property; cannot be null
     * @param type;         property type
     */
    public PropertyReferenceImpl( final String name,
                                  final Class declaringType,
                                  final Class type )
    {
        this( name, declaringType, type, null );
    }

    /**
     * Constructor.
     *
     * @param name          property name; cannot be null
     * @param declaringType type that declared the property; cannot be null
     * @param type;         property type
     * @param traversed     traversed association
     */
    public PropertyReferenceImpl( final String name,
                                  final Class declaringType,
                                  final Class type,
                                  final AssociationReference traversed )
    {
        this.name = name;
        this.declaringType = declaringType;
        this.type = type;
        this.traversed = traversed;
    }

    /**
     * Constructor.
     *
     * @param propertyMethod method that acts as property
     */
    public PropertyReferenceImpl( final Method propertyMethod )
    {
        this( propertyMethod, null );
    }

    /**
     * Constructor.
     *
     * @param propertyMethod method that acts as property
     * @param traversed      traversed association
     */
    public PropertyReferenceImpl( final Method propertyMethod,
                                  final AssociationReference traversed )
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
        this.traversed = traversed;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#getPropertyReferenceName()
     */
    public String getPropertyReferenceName()
    {
        return name;
    }

    /**
     * @see PropertyReference#getPropertyReferenceDeclaringType()
     */
    public Class getPropertyReferenceDeclaringType()
    {
        return declaringType;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#getPropertyReferenceType()
     */
    public Class getPropertyReferenceType()
    {
        return type;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#getTraversedAssociation()
     */
    public AssociationReference getTraversedAssociation()
    {
        return traversed;
    }

    @Override public String toString()
    {
        return new StringBuilder()
            .append( traversed == null ? "" : traversed.toString() + "." )
            .append( declaringType.getSimpleName() )
            .append( ":" )
            .append( name )
            .toString();
    }

}