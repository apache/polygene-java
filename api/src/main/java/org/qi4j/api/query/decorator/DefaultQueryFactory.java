package org.qi4j.api.query.decorator;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class DefaultQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory builderFactory;

    public DefaultQueryFactory( Iterable iterable )
    {
        builderFactory = new QueryIterableFactory( iterable );
        builderFactory = new ResultTypeQueryFactory( builderFactory );
        builderFactory = new WhereQueryFactory( builderFactory );
        builderFactory = new OrderByQueryFactory( builderFactory );
        builderFactory = new FirstMaxQueryFactory( builderFactory );
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return builderFactory.newQueryBuilder( resultType );
    }
}