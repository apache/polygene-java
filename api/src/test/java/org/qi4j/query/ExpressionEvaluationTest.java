/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.query;
/**
 *  TODO
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import static org.qi4j.query.QueryExpression.eq;
import static org.qi4j.query.QueryExpression.lt;
import static org.qi4j.query.QueryExpression.ne;
import static org.qi4j.query.QueryExpression.var;
import org.qi4j.query.operators.Equals;
import org.qi4j.query.operators.LessThan;
import org.qi4j.query.operators.NotEquals;

public class ExpressionEvaluationTest extends TestCase
{
    public void testEquals() throws Exception
    {
        {
            Equals eq = eq( "Foo", "Foo" );
            assertTrue( eq.evaluate( null, null ) );
        }

        {
            Equals eq = eq( "Foo", "Bar" );
            assertFalse( eq.evaluate( null, null ) );
        }
    }

    public void testNotEquals() throws Exception
    {
        {
            NotEquals ne = ne( "Foo", "Foo" );
            assertFalse( ne.evaluate( null, null ) );
        }

        {
            NotEquals ne = ne( "Foo", "Bar" );
            assertTrue( ne.evaluate( null, null ) );
        }
    }

    public void testLessThan() throws Exception
    {
        {
            LessThan lt = lt( 4, 4 );
            assertFalse( lt.evaluate( null, null ) );
        }

        {
            LessThan lt = lt( 3, 4 );
            assertTrue( lt.evaluate( null, null ) );
        }
    }

    public void testVariable() throws Exception
    {
        {
            Equals eq = eq( var( "bar", "Foo" ), var( "name", "Foo" ) );
            assertTrue( eq.evaluate( null, Collections.EMPTY_MAP ) );
        }

        {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put( "name", "Foo" );
            Equals eq = eq( var( "name", "Bar" ), "Foo" );
            assertTrue( eq.evaluate( null, variables ) );
        }
    }

}