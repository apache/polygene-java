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
package org.qi4j.runtime.query;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.VariableValueExpression;
import org.qi4j.runtime.query.grammar.impl.VariableValueExpressionImpl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.qi4j.api.query.QueryExpressions}.
 */
public class QueryExpressionsTest
{

    @Before
    public void setUp()
    {
        QueryExpressions.setProvider( new QueryExpressionsProviderImpl() );
    }

    /**
     * Tests a valid "equals".
     */
    @Test
    public void validEqual()
    {
        StringPropertyReference property = EasyMock.createMock( StringPropertyReference.class );
        EqualsPredicate<String> operator = QueryExpressions.eq( property, "Foo" );
        assertThat( "Property", operator.propertyReference(), is( equalTo( (PropertyReference) property ) ) );
        assertThat( "Value", ( (SingleValueExpression<String>) operator.valueExpression() ).value(), is( equalTo( "Foo" ) ) );
    }

    /**
     * Tests a valid "equals" with a variable value.
     */
    @Test
    public void validEqualWithVariableValue()
    {
        StringPropertyReference property = EasyMock.createMock( StringPropertyReference.class );
        VariableValueExpression<String> variableExpression = QueryExpressions.variable( "var" );
        EqualsPredicate<String> operator = QueryExpressions.eq( property, variableExpression );
        variableExpression.setValue( "Foo" );
        assertThat( "Property", operator.propertyReference(), is( equalTo( (PropertyReference) property ) ) );
        assertThat( "Value", ( (VariableValueExpressionImpl<String>) operator.valueExpression() ).value(), is( equalTo( "Foo" ) ) );
    }

    /**
     * Tests a valid "and".
     */
    @Test
    public void validAnd()
    {
        BooleanExpression left = EasyMock.createMock( BooleanExpression.class );
        BooleanExpression right = EasyMock.createMock( BooleanExpression.class );
        Conjunction conjunction = QueryExpressions.and( left, right );
        assertThat( "Left side expression", conjunction.leftSideExpression(), is( equalTo( left ) ) );
        assertThat( "Right side expression", conjunction.rightSideExpression(), is( equalTo( right ) ) );
    }

    /**
     * Tests a valid "or".
     */
    @Test
    public void validOr()
    {
        BooleanExpression left = EasyMock.createMock( BooleanExpression.class );
        BooleanExpression right = EasyMock.createMock( BooleanExpression.class );
        Disjunction disjunction = QueryExpressions.or( left, right );
        assertThat( "Left side expression", disjunction.leftSideExpression(), is( equalTo( left ) ) );
        assertThat( "Right side expression", disjunction.rightSideExpression(), is( equalTo( right ) ) );
    }

    /**
     * Tests a valid "not".
     */
    @Test
    public void validNot()
    {
        BooleanExpression expression = EasyMock.createMock( BooleanExpression.class );
        Negation negation = QueryExpressions.not( expression );
        assertThat( "Expression", negation.expression(), is( equalTo( expression ) ) );
    }

    static interface StringPropertyReference
        extends Property<String>, PropertyReference
    {

    }
}