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
package org.qi4j.query.el;

/**
 * TODO Add JavaDoc
 * TODO Shall this be merged with AbstractUnaryOperator?
 */
abstract class AbstractNotNullUnaryOperator<E extends Expression>
    extends AbstractUnaryOperator<E>
{

    /**
     * Constructor.
     *
     * @param name       expression name; cannot be null
     * @param expression expression; canot be null
     * @throws IllegalArgumentException - If expression is null
     */
    AbstractNotNullUnaryOperator( final String name,
                                  final E expression )
    {
        super( validateNotNull( name, expression ) );
    }

    /**
     * Validate that expression is not null.
     *
     * @param name       exprestion name
     * @param expression to be validated
     * @return validated property expression
     * @throws IllegalArgumentException - If expression is null
     */
    private static <E> E validateNotNull( final String name,
                                          final E expression )
    {
        if( expression == null )
        {
            throw new IllegalArgumentException( name + " cannot be null" );
        }
        return expression;
    }

}