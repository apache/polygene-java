package org.qi4j.api.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.qi4j.api.model.Mixin1;
import org.qi4j.api.model.Mixin1Impl;
import org.qi4j.api.model.Mixin2Impl;
import org.qi4j.api.model.Mixin3;

/**
 * TODO
 */
public abstract class QueryTest
    extends TestCase
{
    public Query<Mixin1> query;

    public void testShouldReturnNonEmptyWhenCorrectReturnType()
    {
        query.resultType( Mixin1.class );
        assertTrue( query.prepare().iterator().hasNext() );
    }

    public void testShouldReturnEmptyWhenIncorrectReturnType()
    {
        query.resultType( Mixin3.class );
        assertFalse( query.prepare().iterator().hasNext() );
    }

    public void testShouldReturnSingleWhenWherePropertySet()
    {
        query.resultType( Mixin1.class );

        query.where( Mixin1.class ).setName( "Mixin 1" );

        Iterator<Mixin1> iterator = query.prepare().iterator();
        iterator.next();
        assertTrue( iterator.hasNext() );
    }

    public void testShouldReturnEmptyWhenWhereMultiplePropertiesSet()
    {
        query.where( Mixin1.class ).setName( "Mixin 1" );
        query.where( Mixin1.class ).setBar( "B" );

        Iterator<Mixin1> iterator = query.prepare().iterator();
        assertFalse( iterator.hasNext() );
    }

    public void testShouldReturnManyWhenWhereCommonPropertySet()
    {
        query.where( Mixin1.class ).setBar( "B" );

        List<Mixin1> results = new ArrayList<Mixin1>();
        for( Mixin1 mixin1 : query )
        {
            results.add( mixin1 );
        }
        assertTrue( results.size() == 3 );
    }

    public void testShouldReturnSingleWhenMatches()
    {
        query.where( Mixin1.class, Query.Is.MATCHES ).setName( ".*in 2" );

        Mixin1 match = query.find();
        assertNotNull( match );
    }

    public void testShouldReturnNullWhenNotMatches()
    {
        query.where( Mixin1.class, Query.Is.MATCHES ).setName( ".*in 12" );

        Mixin1 match = query.find();
        assertNull( match );
    }

    public void testShouldReturnSingleWhenContains()
    {
        query.where( Mixin1.class, Query.Is.CONTAINS ).setName( "xin" );

        Mixin1 match = query.find();
        assertNotNull( match );
    }

    public void testShouldReturnNullWhenNotContains()
    {
        query.where( Mixin1.class, Query.Is.CONTAINS ).setName( "xie" );

        Mixin1 match = query.find();
        assertNull( match );
    }

    public void testShouldReturnSingleWhenStartsWith()
    {
        query.where( Mixin1.class, Query.Is.STARTS_WITH ).setName( "Mixin" );

        Mixin1 match = query.find();
        assertNotNull( match );
    }

    public void testShouldReturnNullWhenNotStartsWith()
    {
        query.where( Mixin1.class, Query.Is.STARTS_WITH ).setName( "ixin" );

        Mixin1 match = query.find();
        assertNull( match );
    }

    public void testShouldReturnSingleWhenEndsWith()
    {
        query.where( Mixin1.class, Query.Is.ENDS_WITH ).setName( "xin 3" );

        Mixin1 match = query.find();
        assertNotNull( match );
    }

    public void testShouldReturnNullWhenNotEndsWith()
    {
        query.where( Mixin1.class, Query.Is.ENDS_WITH ).setName( "xin 7" );

        Mixin1 match = query.find();
        assertNull( match );
    }

    protected Iterable createObjects()
    {
        List<Object> objects = new ArrayList<Object>();
        objects.add( new Mixin1Impl( "Mixin 1", "A", "test1" ) );
        objects.add( new Mixin1Impl( "Mixin 2", "A", "test2" ) );
        objects.add( new Mixin1Impl( "Mixin 3", "A", "test3" ) );
        objects.add( new Mixin1Impl( "Mixin 4", "B", "test4" ) );
        objects.add( new Mixin1Impl( "Mixin 5", "B", "test5" ) );
        objects.add( new Mixin1Impl( "Mixin 6", "B", "test6" ) );

        objects.add( new Mixin2Impl( "Mixin2 1" ) );
        objects.add( new Mixin2Impl( "Mixin2 2" ) );
        objects.add( new Mixin2Impl( "Mixin2 3" ) );
        objects.add( new Mixin2Impl( "Mixin2 4" ) );

        return objects;
    }

}
