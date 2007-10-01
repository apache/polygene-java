package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class OrderByQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory delegate;

    public OrderByQueryFactory( QueryBuilderFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new OrderByQuery<T>( delegate.newQueryBuilder( resultType ) );
    }
}