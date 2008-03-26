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
 * Unit tests for {@link AbstractNotNullUnaryOperator}.
 */
public class AbstractNotNullUnaryOperatorTest
{

    /**
     * Tests that expression is not allowed to be null.
     */
    @Test( expected = IllegalArgumentException.class )
    public void nullExpression()
    {
        new AbstractNotNullUnaryOperator<Expression>(
            "Expression",
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
        Expression expression = createMock( Expression.class );
        UnaryOperator operator = new AbstractNotNullUnaryOperator<Expression>(
            "Left",
            expression
        )
        {
        };
        assertThat( "Expression", operator.getArgument(), is( equalTo( expression ) ) );
    }

}