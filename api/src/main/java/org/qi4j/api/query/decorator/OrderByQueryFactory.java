package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

/**
 * TODO
 */
public class OrderByQueryFactory
    implements QueryFactory
{
    QueryFactory delegate;

    public OrderByQueryFactory( QueryFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return new OrderByQuery<T>( delegate.newQuery( resultType ) );
    }
}