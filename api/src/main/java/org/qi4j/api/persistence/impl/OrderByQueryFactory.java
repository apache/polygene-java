package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

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