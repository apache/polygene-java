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

import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.ValueExpression;

/**
 * Default {@link org.qi4j.api.query.grammar.MatchesPredicate} implementation.
 */
public final class MatchesPredicateImpl
    extends ComparisonPredicateImpl<String>
    implements MatchesPredicate
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
    public MatchesPredicateImpl( final PropertyReference<String> propertyReference,
                                 final ValueExpression<String> valueExpression
    )
    {
        super( propertyReference, valueExpression );
    }

    /**
     * @see ComparisonPredicateImpl#eval(Comparable, Object)
     */
    protected boolean eval( final Comparable<String> propertyValue, final String expressionValue )
    {
        final String stringValue = propertyValue.toString();
        if( stringValue == null )
        {
            return expressionValue == null;
        }
        return stringValue.matches( expressionValue );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( propertyReference() )
            .append( " MATCHES " )
            .append( "\"" )
            .append( valueExpression() )
            .append( "\"" )
            .append( " )" )
            .toString();
    }
}