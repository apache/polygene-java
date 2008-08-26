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
package org.qi4j.runtime.query.grammar.impl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.entity.association.Association;
import org.qi4j.query.grammar.AssociationReference;

/**
 * Default {@link AssociationReference}.
 *
 * @author Alin Dreghiciu
 * @since March 28, 2008
 */
public class AssociationReferenceImpl
    implements AssociationReference
{

    /**
     * Association name.
     */
    private final String name;
    /**
     * Interface that declared the association.
     */
    private final Class<?> declaringType;
    /**
     * Association accessor method.
     */
    private final Method accessor;
    /**
     * Association type.
     */
    private final Class<?> type;
    /**
     * Traversed association.
     */
    private final AssociationReference traversed;

    /**
     * Constructor.
     *
     * @param accessor method that acts as association
     */
    public AssociationReferenceImpl( final Method accessor )
    {
        this( accessor, null );
    }


    /**
     * Constructor.
     *
     * @param accessor  method that acts as association
     * @param traversed traversed association
     */
    public AssociationReferenceImpl( final Method accessor,
                                     final AssociationReference traversed )
    {
        this.accessor = accessor;
        name = accessor.getName();
        declaringType = accessor.getDeclaringClass();
        Type returnType = accessor.getGenericReturnType();
        if( !( returnType instanceof ParameterizedType ) )
        {
            throw new UnsupportedOperationException( "Unsupported association type:" + returnType );
        }
        Type associationTypeAsType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
        if( !( associationTypeAsType instanceof Class ) )
        {
            throw new UnsupportedOperationException( "Unsupported association type:" + associationTypeAsType );
        }
        type = (Class<?>) associationTypeAsType;
        this.traversed = traversed;
    }

    /**
     * @see AssociationReference#associationName()
     */
    public String associationName()
    {
        return name;
    }

    /**
     * @see AssociationReference#associationDeclaringType()
     */
    public Class<?> associationDeclaringType()
    {
        return declaringType;
    }

    /**
     * @see AssociationReference#associationAccessor()
     */
    public Method associationAccessor()
    {
        return accessor;
    }

    /**
     * @see AssociationReference#associationType()
     */
    public Class<?> associationType()
    {
        return type;
    }

    /**
     * @see AssociationReference#traversedAssociation()
     */
    public AssociationReference traversedAssociation()
    {
        return traversed;
    }

    /**
     * @see AssociationReference#eval(Object)
     */
    public Association eval( final Object target )
    {
        Object actual = target;
        if( traversedAssociation() != null )
        {
            final Association assoc = traversedAssociation().eval( target );
            if( assoc != null )
            {
                actual = assoc.get();
            }
        }
        if( actual != null )
        {
            try
            {
                return (Association) associationAccessor().invoke( actual );
            }
            catch( Exception e )
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder fragment = new StringBuilder();
        if( traversed != null )
        {
            fragment.append( traversed.toString() ).append( "." );
        }
        fragment.append( declaringType.getSimpleName() );
        fragment.append( ":" );
        fragment.append( name );
        return fragment.toString();
    }

}