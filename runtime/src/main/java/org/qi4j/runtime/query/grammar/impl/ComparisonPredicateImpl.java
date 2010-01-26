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

import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;

/**
 * Generic {@link org.qi4j.api.query.grammar.ComparisonPredicate} implementation.
 */
abstract class ComparisonPredicateImpl<T>
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
     *
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    ComparisonPredicateImpl( final PropertyReference<T> propertyReference,
                             final ValueExpression<T> valueExpression
    )
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
     * @see org.qi4j.api.query.grammar.ComparisonPredicate#propertyReference()
     */
    public PropertyReference<T> propertyReference()
    {
        return propertyReference;
    }

    /**
     * @see org.qi4j.api.query.grammar.ComparisonPredicate#valueExpression()
     */
    public ValueExpression<T> valueExpression()
    {
        return valueExpression;
    }

    /**
     * @see org.qi4j.api.query.grammar.BooleanExpression#eval(Object)
     */
    public boolean eval( final Object target )
    {
        if( !( valueExpression() instanceof SingleValueExpression ) )
        {
            throw new QueryExecutionException( "Value " + valueExpression() + " is not supported" );
        }
        final T value = ( (SingleValueExpression<T>) valueExpression() ).value();
        final Property<T> prop = propertyReference().eval( target );
        if( prop == null )
        {
            return value == null;
        }
        final T propValue = prop.get();
        if( propValue == null )
        {
            return value == null;
        }
        if( !( propValue instanceof Comparable ) )
        {
            String clazz = value.getClass().getSimpleName();
            String message = "Cannot use type " + clazz + " for comparisons. Must implement Comparable.";
            throw new QueryExpressionException( message );
        }
        return eval( (Comparable<T>) propValue, value );
    }

    /**
     * Implemented by subclasses in order to perform the actual comparison.
     *
     * @param propertyValue   property value
     * @param expressionValue expression value
     *
     * @return true if the comparison is TRUE
     */
    abstract protected boolean eval( final Comparable<T> propertyValue, final T expressionValue );
}