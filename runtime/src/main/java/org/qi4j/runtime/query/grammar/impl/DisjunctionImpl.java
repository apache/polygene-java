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

import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.Disjunction;

/**
 * Default {@link org.qi4j.api.query.grammar.Disjunction} implementation.
 */
public final class DisjunctionImpl
    extends JunctionImpl
    implements Disjunction
{

    /**
     * Constructor.
     *
     * @param leftSideExpression  left side boolean expression; cannot be null
     * @param rightSideExpression right side boolean expression; cannot be null
     *
     * @throws IllegalArgumentException - If left side expression is null
     *                                  - If right side expression is null
     */
    public DisjunctionImpl( final BooleanExpression leftSideExpression,
                            final BooleanExpression rightSideExpression
    )
    {
        super( leftSideExpression, rightSideExpression );
    }

    /**
     * @see org.qi4j.api.query.grammar.BooleanExpression#eval(Object)
     */
    public boolean eval( final Object target )
    {
        return leftSideExpression().eval( target ) || rightSideExpression().eval( target );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( leftSideExpression() )
            .append( " OR " )
            .append( rightSideExpression() )
            .append( " )" )
            .toString();
    }
}