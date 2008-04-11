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
package org.qi4j.query.grammar.impl;

import org.qi4j.query.grammar.MatchesPredicate;
import org.qi4j.query.grammar.PropertyReference;
import org.qi4j.query.grammar.ValueExpression;

/**
 * Default {@link org.qi4j.query.grammar.MatchesPredicate} implementation.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, April 11, 2008
 */
public class MatchesPredicateImpl
    extends ComparisonPredicateImpl<String>
    implements MatchesPredicate
{

    /**
     * Constructor.
     *
     * @param propertyReference property reference; cannot be null
     * @param valueExpression   value expression; cannot be null
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    public MatchesPredicateImpl( final PropertyReference<String> propertyReference,
                                 final ValueExpression<String> valueExpression )
    {
        super( propertyReference, valueExpression );
    }

    @Override public String toString()
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