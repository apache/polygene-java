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

import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.Conjunction;

/**
 * Default {@link org.qi4j.query.grammar.Conjunction} implementation.
 *
 * @author Alin Dreghiciu
 * @since March 28, 2008
 */
public class ConjunctionImpl
    extends JunctionImpl
    implements Conjunction
{

    /**
     * Constructor.
     *
     * @param leftSideExpression  left side boolean expression; cannot be null
     * @param rightSideExpression right side boolean expression; cannot be null
     * @throws IllegalArgumentException - If left side expression is null
     *                                  - If right side expression is null
     */
    public ConjunctionImpl( final BooleanExpression leftSideExpression,
                            final BooleanExpression rightSideExpression )
    {
        super( leftSideExpression, rightSideExpression );
    }

    @Override public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( getLeftSideExpression() )
            .append( " AND " )
            .append( getRightSideExpression() )
            .append( " )" )
            .toString();
    }

}