package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public class DefaultQueryFactory
    implements QueryFactory
{
    QueryFactory factory;

    public DefaultQueryFactory( Iterable iterable )
    {
        factory = new QueryIterableFactory( iterable );
        factory = new ResultTypeQueryFactory( factory );
        factory = new WhereQueryFactory( factory );
        factory = new OrderByQueryFactory( factory );
        factory = new FirstMaxQueryFactory( factory );
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return factory.newQuery( resultType );
    }
}