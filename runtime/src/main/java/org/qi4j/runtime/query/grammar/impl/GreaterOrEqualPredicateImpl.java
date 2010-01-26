/*
 * Copyright 2008 Alin Dreghiciu.
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

import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.ValueExpression;

/**
 * Default {@link org.qi4j.api.query.grammar.GreaterOrEqualPredicate} implementation.
 */
public final class GreaterOrEqualPredicateImpl<T>
    extends ComparisonPredicateImpl<T>
    implements GreaterOrEqualPredicate<T>
{

    /**
     * Constructor.
     *
     * @param propertyReference property reference; cannot be null
     * @param valueExpression   value expression; cannot be null
     *
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    public GreaterOrEqualPredicateImpl( final PropertyReference<T> propertyReference,
                                        final ValueExpression<T> valueExpression
    )
    {
        super( propertyReference, valueExpression );
    }

    /**
     * @see ComparisonPredicateImpl#eval(Comparable, Object)
     */
    protected boolean eval( final Comparable<T> propertyValue, final T expressionValue )
    {
        return propertyValue.compareTo( expressionValue ) >= 0;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( propertyReference() )
            .append( " >= " )
            .append( "\"" )
            .append( valueExpression() )
            .append( "\"^^" )
            .append( propertyReference().propertyType().getSimpleName() )
            .append( " )" )
            .toString();
    }
}