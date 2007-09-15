package org.qi4j.api.query;
/**
 *  TODO
 */

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.qi4j.api.model.Mixin1;
import org.qi4j.api.model.Mixin1Impl;
import org.qi4j.api.query.decorator.CachingQueryFactory;
import org.qi4j.api.query.decorator.DefaultQueryFactory;
import org.qi4j.api.query.decorator.OrderByQueryFactory;
import org.qi4j.api.query.decorator.QueryIterableFactory;

public abstract class QueryFactoryTest extends TestCase
{
    QueryFactory qb;

    public void testComposedQueries() throws Exception
    {
        // Create set of objects
        List objects = createObjects();

        // Create builder and connect it to set
        qb = new QueryIterableFactory( objects );
        qb = new CachingQueryFactory( qb );
        qb = new OrderByQueryFactory( qb );

        Query<Mixin1> query = qb.newQuery( Mixin1.class );

        // Set parameters
        query.where( Mixin1.class ).setBar( "B" );
        query.orderBy( Mixin1.class, Query.OrderBy.DESCENDING ).getFoo();

        // Perform query
        Iterable<Mixin1> result = query.prepare();

        // Iterate results
        for( Mixin1 object : result )
        {
            System.out.println( object.getName() );
        }
    }

    public void testChainedQueries()
    {
        List objects = createObjects();

        // Create builder and connect it to the set
        qb = new DefaultQueryFactory( objects );

        Query<Mixin1> query = qb.newQuery( Mixin1.class );
        query.orderBy( Mixin1.class, Query.OrderBy.ASCENDING ).getName();

        Iterable<Mixin1> results = query.prepare();

        // Take results into new query
        QueryFactory qb2 = new DefaultQueryFactory( results );
        Query<Mixin1> query2 = qb2.newQuery( Mixin1.class );
        query2.where( Mixin1.class ).setBar( "B" );
        Iterable<Mixin1> results2 = query2.prepare();

        // Print results
        for( Mixin1 mixin1 : results2 )
        {
            System.out.println( mixin1.getFoo() );
        }
    }

    private List createObjects()
    {
        List objects = new ArrayList();
        objects.add( new Mixin1Impl( "Mixin 1", "A", "test1" ) );
        objects.add( new Mixin1Impl( "Mixin 2", "A", "test2" ) );
        objects.add( new Mixin1Impl( "Mixin 3", "A", "test3" ) );
        objects.add( new Mixin1Impl( "Mixin 4", "B", "test4" ) );
        objects.add( new Mixin1Impl( "Mixin 5", "B", "test5" ) );
        objects.add( new Mixin1Impl( "Mixin 6", "B", "test6" ) );
        return objects;
    }

}