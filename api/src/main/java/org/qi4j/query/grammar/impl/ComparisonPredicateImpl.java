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

import org.qi4j.query.grammar.ComparisonPredicate;
import org.qi4j.query.grammar.PropertyReference;
import org.qi4j.query.grammar.ValueExpression;

/**
 * Generic {@link org.qi4j.query.grammar.ComparisonPredicate} implementation.
 *
 * @author Alin Dreghiciu
 * @since March 28, 2008
 */
public abstract class ComparisonPredicateImpl<T>
    implements ComparisonPredicate<T>
{

    /**
     * Property reference (left side of the predicate).
     */
    private final PropertyReference<T> propertyReference;
    /**
     * Value expression (right side of the predicate).
     */
    private final ValueExpression<T> valueExpression;

    /**
     * Constructor.
     *
     * @param propertyReference property reference; cannot be null
     * @param valueExpression   value expression; cannot be null
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    protected ComparisonPredicateImpl( final PropertyReference<T> propertyReference,
                                       final ValueExpression<T> valueExpression )
    {
        if( propertyReference == null )
        {
            throw new IllegalArgumentException( "Property reference cannot be null" );
        }
        if( valueExpression == null )
        {
            throw new IllegalArgumentException( "Value expression cannot be null" );
        }
        this.propertyReference = propertyReference;
        this.valueExpression = valueExpression;
    }

    /**
     * @see org.qi4j.query.grammar.ComparisonPredicate#getPropertyReference()
     */
    public PropertyReference<T> getPropertyReference()
    {
        return propertyReference;
    }

    /**
     * @see org.qi4j.query.grammar.ComparisonPredicate#getValueExpression()
     */
    public ValueExpression<T> getValueExpression()
    {
        return valueExpression;
    }

}