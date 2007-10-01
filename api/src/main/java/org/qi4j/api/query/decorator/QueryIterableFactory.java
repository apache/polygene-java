package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class QueryIterableFactory
    implements QueryBuilderFactory
{
    Iterable objects;

    public QueryIterableFactory( Iterable objects )
    {
        this.objects = objects;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new QueryIterable<T>( (Iterable<T>) objects );
    }
}