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
 * Default implementation of {@link AssociationExpression}.
 *
 * @author Alin Dreghiciu
 * @since March 28, 2008
 */
public class AssociationExpressionImpl
    implements AssociationExpression
{

    /**
     * Assocaition name.
     */
    private final String name;
    /**
     * Interface that declared the association.
     */
    private final Class declaringType;
    /**
     * Association type.
     */
    private final Class type;
    /**
     * Traversed association.
     */
    private final AssociationExpression traversed;

    /**
     * Constructor.
     *
     * @param name          association name; cannot be null
     * @param declaringType type that declared the association; cannot be null
     * @param type;         association type
     */
    public AssociationExpressionImpl( final String name,
                                      final Class declaringType,
                                      final Class type )
    {
        this( name, declaringType, type, null );
    }

    /**
     * Constructor.
     *
     * @param name          association name; cannot be null
     * @param declaringType type that declared the association; cannot be null
     * @param type;         association type
     * @param traversed     traversed association
     */
    public AssociationExpressionImpl( final String name,
                                      final Class declaringType,
                                      final Class type,
                                      final AssociationExpression traversed )
    {
        this.name = name;
        this.declaringType = declaringType;
        this.type = type;
        this.traversed = traversed;
    }

    /**
     * Constructor.
     *
     * @param method method that acts as association
     */
    public AssociationExpressionImpl( final Method method )
    {
        this( method, null );
    }


    /**
     * Constructor.
     *
     * @param method    method that acts as association
     * @param traversed traversed association
     */
    public AssociationExpressionImpl( final Method method,
                                      final AssociationExpression traversed )
    {
        name = method.getName();
        declaringType = method.getDeclaringClass();
        Type returnType = method.getGenericReturnType();
        if( !( returnType instanceof ParameterizedType ) )
        {
            throw new UnsupportedOperationException( "Unsupported association type:" + returnType );
        }
        Type associationTypeAsType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        if( !( associationTypeAsType instanceof Class ) )
        {
            throw new UnsupportedOperationException( "Unsupported association type:" + associationTypeAsType );
        }
        type = (Class) associationTypeAsType;
        this.traversed = traversed;
    }

    /**
     * @see AssociationExpression#getName()
     */
    public String getName()
    {
        return name;
    }

    /**
     * @see AssociationExpression#getDeclaringType()
     */
    public Class getDeclaringType()
    {
        return declaringType;
    }

    /**
     * @see AssociationExpression#getType()
     */
    public Class getType()
    {
        return type;
    }

    /**
     * @see AssociationExpression#getTraversedAssociation()
     */
    public AssociationExpression getTraversedAssociation()
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