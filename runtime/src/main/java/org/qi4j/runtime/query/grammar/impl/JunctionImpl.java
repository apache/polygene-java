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

/**
 * Generic implementation of a junction (con/dis).
 */
abstract class JunctionImpl
{

    /**
     * Left side boolean expression.
     */
    private final BooleanExpression leftSideExpression;
    /**
     * Right side boolean expression.
     */
    private final BooleanExpression rightSideExpression;

    /**
     * Constructor.
     *
     * @param leftSideExpression  left side boolean expression; cannot be null
     * @param rightSideExpression right side boolean expression; cannot be null
     *
     * @throws IllegalArgumentException - If left side expression is null
     *                                  - If right side expression is null
     */
    JunctionImpl( final BooleanExpression leftSideExpression,
                  final BooleanExpression rightSideExpression
    )
    {
        if( leftSideExpression == null )
        {
            throw new IllegalArgumentException( "Left side boolean expression cannot be null" );
        }
        if( rightSideExpression == null )
        {
            throw new IllegalArgumentException( "Right side boolean expression cannot be null" );
        }
        this.leftSideExpression = leftSideExpression;
        this.rightSideExpression = rightSideExpression;
    }

    /**
     * Getter.
     *
     * @return left side boolean expression
     */
    public BooleanExpression leftSideExpression()
    {
        return leftSideExpression;
    }

    /**
     * Getter.
     *
     * @return right side boolean expression
     */
    public BooleanExpression rightSideExpression()
    {
        return rightSideExpression;
    }
}