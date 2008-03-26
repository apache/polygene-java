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

package org.qi4j.queryobsolete;
/**
 *  TODO
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import static org.qi4j.queryobsolete.OrderBy.Order.DESCENDING;
import static org.qi4j.queryobsolete.QueryExpression.eq;
import static org.qi4j.queryobsolete.QueryExpression.var;

public class QueryableIterableTest extends TestCase
{
    QueryableIterable queryableIterable;
    private QueryBuilder<MyObject> queryBuilder;


    @Override protected void setUp() throws Exception
    {
        List<MyObject> objects = new ArrayList<MyObject>();
        objects.add( new MyObjectImpl( "Foo", 3, new Date() ) );
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.roll( Calendar.DAY_OF_WEEK, 1 );
        objects.add( new MyObjectImpl( "Bar", 5, tomorrow.getTime() ) );

        queryableIterable = new QueryableIterable( objects );

        queryBuilder = new QueryBuilderFactoryImpl( queryableIterable ).newQueryBuilder( MyObject.class );
    }

    public void testWhere() throws Exception
    {
        List<MyObject> desiredResult = getDesiredResults( "Foo" );

        MyObject param = queryBuilder.parameter( MyObject.class );
        Query<MyObject> query = queryBuilder.where( eq( "Foo", param.getName() ) ).newQuery();
        List<MyObject> result = getResults( query );
        assertEquals( desiredResult, result );

        for( MyObject myObject : query )
        {
            System.out.println( myObject );
        }
    }

    public void testVariable() throws Exception
    {
        MyObject param = queryBuilder.parameter( MyObject.class );
        Query<MyObject> query = queryBuilder.where( eq( var( "name", "Bar" ), param.getName() ) ).newQuery();

        {
            query.setVariable( "name", "Foo" );
            List<MyObject> desiredResult = getDesiredResults( "Foo" );
            List<MyObject> result = getResults( query );
            assertEquals( desiredResult, result );
        }

        {
            query.setVariable( "name", "Bar" );
            List<MyObject> desiredResult = getDesiredResults( "Bar" );
            List<MyObject> result = getResults( query );
            assertEquals( desiredResult, result );
        }

        for( MyObject myObject : query )
        {
            System.out.println( myObject );
        }
    }

    public void testOrderBy() throws Exception
    {
        MyObject param = queryBuilder.parameter( MyObject.class );
        Query<MyObject> query = queryBuilder.orderBy( param.getValue() ).newQuery();
        assertEquals( getDesiredResults( "Foo", "Bar" ), getResults( query ) );
    }

    public void testOrderByDescending() throws Exception
    {
        MyObject param = queryBuilder.parameter( MyObject.class );
        Query<MyObject> query = queryBuilder.orderBy( param.getValue(), DESCENDING ).newQuery();
        assertEquals( getDesiredResults( "Bar", "Foo" ), getResults( query ) );
    }

    private List<MyObject> getDesiredResults( String... names )
    {
        List<MyObject> desiredResult = new ArrayList<MyObject>();
        for( String name : names )
        {
            desiredResult.add( new MyObjectImpl( name ) );
        }
        return desiredResult;
    }

    private List<MyObject> getResults( Query<MyObject> query )
    {
        List<MyObject> result = new ArrayList<MyObject>();
        for( MyObject myObject : query )
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
        implements MyObject
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

            MyObjectImpl myObject = (MyObjectImpl) o;

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