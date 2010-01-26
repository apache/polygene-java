/*
 * Copyright 2009 Niclas Hedhman.
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

import java.util.Collection;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;

/**
 * Default {@link org.qi4j.api.query.grammar.EqualsPredicate} implementation.
 */
public final class ContainsPredicateImpl<T>
    implements ContainsPredicate<T>
{

    /**
     * Property reference (left side of the predicate).
     */
    private final PropertyReference<Collection<T>> propertyReference;
    /**
     * Value expression (right side of the predicate).
     */
    private final SingleValueExpression<T> valueExpression;

    /**
     * Constructor.
     *
     * @param propertyReference property reference; cannot be null
     * @param valueExpression   value expression; cannot be null
     *
     * @throws IllegalArgumentException - If property reference is null
     *                                  - If value expression is null
     */
    public ContainsPredicateImpl( final PropertyReference<Collection<T>> propertyReference,
                                  final SingleValueExpression<T> valueExpression
    )
    {
        this.propertyReference = propertyReference;
        this.valueExpression = valueExpression;
    }

    public PropertyReference<Collection<T>> propertyReference()
    {
        return propertyReference;
    }

    public ValueExpression<T> valueExpression()
    {
        return valueExpression;
    }

    public boolean eval( Object target )
    {
        final T value = valueExpression.value();
        final Property<Collection<T>> prop = propertyReference().eval( target );
        if( prop == null )
        {
            return value == null;
        }
        final Collection<T> propValue = prop.get();
        if( propValue == null )
        {
            return value == null;
        }
        if( !( propValue instanceof Comparable ) )
        {
            String clazz = value.getClass().getSimpleName();
            String message = "Cannot use type " + clazz + " for comparisons. Must implement java.lang.Comparable.";
            throw new QueryExpressionException( message );
        }
        return propValue.contains( value );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( propertyReference() )
            .append( ".contains( " )
            .append( valueExpression() )
            .append( " )^^" )
            .append( propertyReference().propertyType().getSimpleName() )
            .append( " )" )
            .toString();
    }
}