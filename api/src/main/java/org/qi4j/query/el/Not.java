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
 * A logical "NOT" on a boolean expression.
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public class Not
    extends AbstractNotNullUnaryOperator<BooleanExpression>
    implements BooleanExpression
{

    /**
     * Constructor.
     *
     * @param expression expression; canot be null
     * @throws IllegalArgumentException - If expression is null
     */
    public Not( final BooleanExpression expression )
    {
        super( "Expression", expression );
    }

}