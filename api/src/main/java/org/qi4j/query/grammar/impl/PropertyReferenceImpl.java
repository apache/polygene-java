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
public final class PropertyReferenceImpl<T>
    implements PropertyReference<T>
{

    /**
     * Property name.
     */
    private final String name;
    /**
     * Interface that declared the property.
     */
    private final Class<?> declaringType;
    /**
     * Property accessor method.
     */
    private final Method accessor;
    /**
     * Property type.
     */
    private final Class<T> type;
    /**
     * Traversed association.
     */
    private final AssociationReference traversed;

    /**
     * Constructor.
     *
     * @param accessor method that acts as property
     */
    public PropertyReferenceImpl( final Method accessor )
    {
        this( accessor, null );
    }

    /**
     * Constructor.
     *
     * @param accessor  method that acts as property
     * @param traversed traversed association
     */
    @SuppressWarnings("unchecked")
    public PropertyReferenceImpl( final Method accessor,
                                  final AssociationReference traversed )
    {
        this.accessor = accessor;
        name = accessor.getName();
        declaringType = accessor.getDeclaringClass();
        Type returnType = accessor.getGenericReturnType();
        if( !( returnType instanceof ParameterizedType ) )
        {
            throw new UnsupportedOperationException( "Unsupported property type:" + returnType );
        }
        Type propertyTypeAsType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        if( !( propertyTypeAsType instanceof Class ) )
        {
            throw new UnsupportedOperationException( "Unsupported property type:" + propertyTypeAsType );
        }
        type = (Class<T>) propertyTypeAsType;
        this.traversed = traversed;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#propertyName()
     */
    public String propertyName()
    {
        return name;
    }

    /**
     * @see PropertyReference#propertyDeclaringType()
     */
    public Class<?> propertyDeclaringType()
    {
        return declaringType;
    }

    /**
     * @see PropertyReference#propertyAccessor()
     */
    public Method propertyAccessor()
    {
        return accessor;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#propertyType()
     */
    public Class<T> propertyType()
    {
        return type;
    }

    /**
     * @see org.qi4j.query.grammar.PropertyReference#traversedAssociation()
     */
    public AssociationReference traversedAssociation()
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