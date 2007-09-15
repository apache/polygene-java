package org.qi4j.api.query.decorator;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

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