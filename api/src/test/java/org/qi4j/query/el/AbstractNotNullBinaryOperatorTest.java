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
package org.qi4j.query.el;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for {@link AbstractNotNullBinaryOperator}.
 */
public class AbstractNotNullBinaryOperatorTest
{

    /**
     * Tests that a left side expression is not allowed to be null.
     */
    @Test( expected = IllegalArgumentException.class )
    public void nullLeftExpression()
    {
        new AbstractNotNullBinaryOperator<Expression, Expression>(
            "Left",
            null,
            "Right",
            createMock( Expression.class )
        )
        {
        };
    }

    /**
     * Tests that a right side expression is not allowed to be null.
     */
    @Test( expected = IllegalArgumentException.class )
    public void nullRightExpression()
    {
        new AbstractNotNullBinaryOperator<Expression, Expression>(
            "Left",
            createMock( Expression.class ),
            "Right",
            null
        )
        {
        };
    }

    /**
     * Tests that a valid constructed object does not fail.
     */
    @Test
    public void valid()
    {
        Expression left = createMock( Expression.class );
        Expression right = createMock( Expression.class );
        BinaryOperator operator = new AbstractNotNullBinaryOperator<Expression, Expression>(
            "Left",
            left,
            "Right",
            right
        )
        {
        };
        assertThat( "Left side expression", operator.getLeftArgument(), is( equalTo( left ) ) );
        assertThat( "Right side expression", operator.getRightArgument(), is( equalTo( right ) ) );
    }

}