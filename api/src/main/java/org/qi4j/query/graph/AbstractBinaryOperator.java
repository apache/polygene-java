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
package org.qi4j.query.graph;

/**
 * TODO Add JavaDoc
 */
abstract class AbstractBinaryOperator<L extends Expression, R extends Expression>
    implements BinaryOperator<L, R>
{

    /**
     * Left side expression.
     */
    private final L left;
    /**
     * Right side expression.
     */
    private final R right;

    /**
     * Constructor.
     *
     * @param left  left side expression
     * @param right right side expression
     */
    AbstractBinaryOperator( final L left,
                            final R right )
    {
        this.left = left;
        this.right = right;
    }

    public L getLeftArgument()
    {
        return left;
    }

    public R getRightArgument()
    {
        return right;
    }

}