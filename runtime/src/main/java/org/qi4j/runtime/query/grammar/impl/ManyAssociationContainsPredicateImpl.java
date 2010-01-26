/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.query.grammar.impl;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.ManyAssociationReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;

/**
 * Default {@link org.qi4j.api.query.grammar.ManyAssociationReference} implementation.
 */
public final class ManyAssociationContainsPredicateImpl<T>
    implements ManyAssociationContainsPredicate<T>
{

    /**
     * Property reference (left side of the predicate).
     */
    private final ManyAssociationReference associationReference;
    /**
     * Value expression (right side of the predicate).
     */
    private final SingleValueExpression<T> valueExpression;

    /**
     * Constructor.
     *
     * @param associationReference manyassociation reference; cannot be null
     * @param valueExpression      value expression; cannot be null
     *
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    public ManyAssociationContainsPredicateImpl( final ManyAssociationReference associationReference,
                                                 final SingleValueExpression<T> valueExpression
    )
    {
        this.associationReference = associationReference;
        this.valueExpression = valueExpression;
    }

    public ManyAssociationReference associationReference()
    {
        return associationReference;
    }

    public ValueExpression<T> valueExpression()
    {
        return valueExpression;
    }

    public boolean eval( Object target )
    {
        final T value = valueExpression.value();
        final ManyAssociation<Object> manyAssoc = (ManyAssociation<Object>) associationReference().eval( target );
        if( manyAssoc == null )
        {
            return value == null;
        }
        return manyAssoc.contains( value );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( associationReference() )
            .append( ".contains( " )
            .append( valueExpression() )
            .append( " )^^" )
            .append( associationReference().associationType().toString() )
            .append( " )" )
            .toString();
    }
}