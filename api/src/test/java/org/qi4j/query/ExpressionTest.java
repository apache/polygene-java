/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.query;

import java.util.ArrayList;
import junit.framework.TestCase;
import static org.qi4j.query.QueryExpression.eq;
import static org.qi4j.query.QueryExpression.ne;
import static org.qi4j.query.QueryExpression.contains;
import static org.qi4j.query.QueryExpression.not;
import static org.qi4j.query.QueryExpression.and;

public class ExpressionTest extends TestCase
{
    private QueryBuilder<AbcComposite> builder;

    protected void setUp() throws Exception
    {
        while( QueryStack.getSize() > 0 )
        {
            QueryStack.popExpression();
        }
        ArrayList data = new ArrayList();
        QueryableIterable source = new QueryableIterable( data );
        QueryBuilderFactory factory = new QueryBuilderFactoryImpl( source );
        builder = factory.newQueryBuilder( AbcComposite.class );
    }

    public void testEquals1()
        throws Exception
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = eq( abc.getName(), "Niclas" );
        assertEquals( "(Abc.getName() = \"Niclas\")", expression.toString() );
    }

    public void testNotEquals1()
        throws Exception
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = ne( abc.getName(), "Niclas" );
        assertEquals( "(Abc.getName() != \"Niclas\")", expression.toString() );
    }

    public void testContains1()
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = contains( abc.getName(), "Niclas" );
        assertEquals( "(Abc.getName().contains(\"Niclas\"))", expression.toString() );
    }

    public void testContains2()
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = contains( abc.getDefs(), "Niclas" );
        assertEquals( "(Abc.getDefs().contains(\"Niclas\"))", expression.toString() );
    }

    public void testNot1()
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = not( eq( abc.getName(), "Niclas" ) );
        assertEquals( "(NOT((Abc.getName() = \"Niclas\")))", expression.toString() );
    }

    public void testAnd1()
    {
        Abc abc = builder.parameter( Abc.class );
        BooleanExpression expression = and( eq( abc.getName(), "Niclas" ), eq( abc.getCity(), "Kuala Lumpur" ) );
        assertEquals( "((Abc.getName() = \"Niclas\") AND (Abc.getCity() = \"Kuala Lumpur\"))", expression.toString() );
    }
}
