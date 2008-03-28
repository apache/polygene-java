/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.query;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.property.Property;
import org.qi4j.query.graph.BinaryOperator;
import org.qi4j.query.graph.BooleanExpression;
import org.qi4j.query.graph.Equals;
import org.qi4j.query.graph.PropertyExpression;
import org.qi4j.query.graph.UnaryOperator;
import org.qi4j.query.graph.VariableValue;
import org.qi4j.query.graph.And;
import org.qi4j.query.graph.Or;
import org.qi4j.query.graph.Not;

/**
 * Unit tests for {@link QueryExpressions}.
 */
public class QueryExpressionsTest
{

    /**
     * Tests a valid "equals".
     */
    @Test
    public void validEqual()
    {
        StringPropertyExpression property = createMock( StringPropertyExpression.class );
        Equals<String> operator = (Equals<String>) QueryExpressions.eq( property, "Foo" );
        assertThat( "Property", operator.getLeftArgument(), is( equalTo( (PropertyExpression) property ) ) );
        assertThat( "Value", operator.getRightArgument().getValue(), is( equalTo( "Foo" ) ) );
    }

    /**
     * Tests a valid "equals" with a variable value.
     */
    @Test
    public void validEqualWithVariableValue()
    {
        StringPropertyExpression property = createMock( StringPropertyExpression.class );
        VariableValue<String> variable = QueryExpressions.variable( "var" );
        Equals<String> operator = (Equals<String>) QueryExpressions.eq( property, variable );
        variable.setValue( "Foo" );
        assertThat( "Property", operator.getLeftArgument(), is( equalTo( (PropertyExpression) property ) ) );
        assertThat( "Value", operator.getRightArgument().getValue(), is( equalTo( "Foo" ) ) );
    }

    /**
     * Tests a valid "and".
     */
    @Test
    public void validAnd()
    {
        BooleanExpression left = createMock( BooleanExpression.class );
        BooleanExpression right = createMock( BooleanExpression.class );
        And operator = (And) QueryExpressions.and( left, right );
        assertThat( "Left side expression", operator.getLeftArgument(), is( equalTo( left ) ) );
        assertThat( "Right side expression", operator.getRightArgument(), is( equalTo( right ) ) );
    }

    /**
     * Tests a valid "or".
     */
    @Test
    public void validOr()
    {
        BooleanExpression left = createMock( BooleanExpression.class );
        BooleanExpression right = createMock( BooleanExpression.class );
        Or operator = (Or) QueryExpressions.or( left, right );
        assertThat( "Left side expression", operator.getLeftArgument(), is( equalTo( left ) ) );
        assertThat( "Right side expression", operator.getRightArgument(), is( equalTo( right ) ) );
    }

    /**
     * Tests a valid "not".
     */
    @Test
    public void validNot()
    {
        BooleanExpression expression = createMock( BooleanExpression.class );
        Not unaryOperator = (Not) QueryExpressions.not( expression );
        assertThat( "Expression", unaryOperator.getArgument(), is( equalTo( expression ) ) );
    }

    static interface StringPropertyExpression
        extends Property<String>, PropertyExpression
    {

    }

}