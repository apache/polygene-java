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
package org.qi4j.query.set1;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactoryImpl;
import org.qi4j.api.query.QueryableIterable;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;

public class ExpressionTest extends TestCase
{
    private QueryBuilderFactory queryBuilderFactory;
    private CompositeBuilderFactory compositeBuilderFactory;
    private ArrayList<Composite1> data;

    public void setUp()
    {
        compositeBuilderFactory = new CompositeBuilderFactoryImpl();
        CompositeBuilder<Composite1> compositeBuilder = compositeBuilderFactory.newCompositeBuilder( Composite1.class );
        data = new ArrayList<Composite1>();
        for( int i = 0; i < 100; i++ )
        {
            Composite1 instance = compositeBuilder.newInstance();
            instance.setName( "i=" + i );
            instance.setBar( "bar=" + i % 4 );
            data.add( instance );
        }
        queryBuilderFactory = new QueryBuilderFactoryImpl( new QueryableIterable( data ) );
    }

    public void testSimpleExpression()
        throws Exception
    {
/*
        QueryBuilder<Composite1> builder = queryBuilderFactory.newQueryBuilder( Composite1.class );
        Mixin1 m1 = builder.parameter( Mixin1.class );
        builder.where( eq( m1.getName(), arg( "i=5" ) ) );
        Query<Composite1> q = builder.newQuery();
        Composite1 result = q.find();
        assertNotNull( result );
        assertEquals( "i=5", result.getName() );
        assertEquals( "bar=1", result.getBar() );
*/
    }

}
