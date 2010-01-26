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
import org.qi4j.api.query.grammar.Negation;

/**
 * Default {@link org.qi4j.api.query.grammar.Negation} implementation.
 */
public final class NegationImpl
    implements Negation
{

    /**
     * Negated boolean expression.
     */
    private final BooleanExpression expression;

    /**
     * Constructor.
     *
     * @param expression boolean expression; canot be null
     *
     * @throws IllegalArgumentException - If expression is null
     */
    public NegationImpl( final BooleanExpression expression )
    {
        this.expression = expression;
    }

    /**
     * @see org.qi4j.api.query.grammar.Negation#expression()
     */
    public BooleanExpression expression()
    {
        return expression;
    }

    /**
     * @see org.qi4j.api.query.grammar.BooleanExpression#eval(Object)
     */
    public boolean eval( final Object target )
    {
        return !expression().eval( target );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "NOT ( " )
            .append( expression )
            .append( " )" )
            .toString();
    }
}