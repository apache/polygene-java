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

import org.qi4j.property.Property;

/**
 * Constraints that a {@link Property} is equal to another {@link Property}
 * ( as in Property<X>.get() = Property<X>.get() ).
 *
 * @author Alin Dreghiciu
 * @since March 26, 2008
 */
public class EqualsProperty<T>
    extends AbstractNotNullBinaryOperator<PropertyExpression<T>, PropertyExpression<T>>
    implements BooleanExpression
{

    /**
     * Constructor.
     *
     * @param left  left side property expression; cannot be null
     * @param right right side property expression; cannot be null
     * @throws IllegalArgumentException - If left or right is null
     */
    public EqualsProperty( final PropertyExpression<T> left,
                           final PropertyExpression<T> right )
    {
        super( "Left side property", left, "Right side property", right );
    }

    @Override public String toString()
    {
        return new StringBuilder( )
            .append( "( " )
            .append( getLeftArgument() )
            .append( " = " )
            .append( getRightArgument() )
            .append( " )" )
            .toString();
    }    

}