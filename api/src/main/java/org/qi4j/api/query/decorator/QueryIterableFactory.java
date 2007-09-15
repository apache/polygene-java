package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

/**
 * TODO
 */
public class QueryIterableFactory
    implements QueryFactory
{
    Iterable objects;

    public QueryIterableFactory( Iterable objects )
    {
        this.objects = objects;
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return new QueryIterable<T>( (Iterable<T>) objects );
    }
}