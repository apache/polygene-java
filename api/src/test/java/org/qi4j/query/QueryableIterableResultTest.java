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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;

public class QueryableIterableResultTest extends TestCase
{
    QueryableIterable queryableIterable;
    private QueryBuilder<QueryableIterableResultTest.MyObject> queryBuilder;


    @Override protected void setUp() throws Exception
    {
        List<MyObject> objects = new ArrayList<MyObject>();
        objects.add( new QueryableIterableResultTest.MyObjectImpl( "Foo", 3, new Date() ) );
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.roll( Calendar.DAY_OF_WEEK, 1 );
        objects.add( new QueryableIterableResultTest.MyObjectImpl( "Bar", 5, tomorrow.getTime() ) );

        queryableIterable = new QueryableIterable( objects );

        queryBuilder = new QueryBuilderFactoryImpl( queryableIterable ).newQueryBuilder( QueryableIterableResultTest.MyObject.class );
    }

    public void testWhere() throws Exception
    {
        List<QueryableIterableResultTest.MyObject> desiredResult = getDesiredResults( "Foo" );

        QueryableIterableResultTest.MyObject param = queryBuilder.parameter( QueryableIterableResultTest.MyObject.class );
        Query<QueryableIterableResultTest.MyObject> query = queryBuilder.where( QueryExpression.eq( "Foo", param.getName() ) ).newQuery();
        List<QueryableIterableResultTest.MyObject> result = getResults( query );
        assertEquals( desiredResult, result );

        for( QueryableIterableResultTest.MyObject myObject : query )
        {
            System.out.println( myObject );
        }
    }

    public void testVariable() throws Exception
    {
        QueryableIterableResultTest.MyObject param = queryBuilder.parameter( QueryableIterableResultTest.MyObject.class );
        Query<QueryableIterableResultTest.MyObject> query = queryBuilder.where( QueryExpression.eq( QueryExpression.var( "name", "Bar" ), param.getName() ) ).newQuery();

        {
            query.setVariable( "name", "Foo" );
            List<QueryableIterableResultTest.MyObject> desiredResult = getDesiredResults( "Foo" );
            List<QueryableIterableResultTest.MyObject> result = getResults( query );
            assertEquals( desiredResult, result );
        }

        {
            query.setVariable( "name", "Bar" );
            List<QueryableIterableResultTest.MyObject> desiredResult = getDesiredResults( "Bar" );
            List<QueryableIterableResultTest.MyObject> result = getResults( query );
            assertEquals( desiredResult, result );
        }

        for( QueryableIterableResultTest.MyObject myObject : query )
        {
            System.out.println( myObject );
        }
    }

    public void testOrderBy() throws Exception
    {
        QueryableIterableResultTest.MyObject param = queryBuilder.parameter( QueryableIterableResultTest.MyObject.class );
        Query<QueryableIterableResultTest.MyObject> query = queryBuilder.orderBy( param.getValue() ).newQuery();
        assertEquals( getDesiredResults( "Foo", "Bar" ), getResults( query ) );
    }

    public void testOrderByDescending() throws Exception
    {
        QueryableIterableResultTest.MyObject param = queryBuilder.parameter( QueryableIterableResultTest.MyObject.class );
        Query<QueryableIterableResultTest.MyObject> query = queryBuilder.orderBy( param.getValue(), OrderBy.Order.DESCENDING ).newQuery();
        assertEquals( getDesiredResults( "Bar", "Foo" ), getResults( query ) );
    }

    private List<QueryableIterableResultTest.MyObject> getDesiredResults( String... names )
    {
        List<QueryableIterableResultTest.MyObject> desiredResult = new ArrayList<QueryableIterableResultTest.MyObject>();
        for( String name : names )
        {
            desiredResult.add( new QueryableIterableResultTest.MyObjectImpl( name ) );
        }
        return desiredResult;
    }

    private List<QueryableIterableResultTest.MyObject> getResults( Query<QueryableIterableResultTest.MyObject> query )
    {
        List<QueryableIterableResultTest.MyObject> result = new ArrayList<QueryableIterableResultTest.MyObject>();
        for( QueryableIterableResultTest.MyObject myObject : query )
        {
            result.add( myObject );
        }
        return result;
    }

    public interface MyObject
    {
        String getName();

        int getValue();

        Date getDate();
    }

    public static class MyObjectImpl
        implements QueryableIterableResultTest.MyObject
    {
        String name;
        int value;
        Date date;

        public MyObjectImpl( String name )
        {
            this.name = name;
        }

        public MyObjectImpl( String name, int value, Date date )
        {
            this.name = name;
            this.value = value;
            this.date = date;
        }

        public String getName()
        {
            return name;
        }

        public int getValue()
        {
            return value;
        }

        public Date getDate()
        {
            return date;
        }

        @Override public String toString()
        {
            return name;
        }

        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            QueryableIterableResultTest.MyObjectImpl myObject = (QueryableIterableResultTest.MyObjectImpl) o;

            if( !name.equals( myObject.name ) )
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return name.hashCode();
        }
    }
}
