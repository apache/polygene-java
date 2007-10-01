package org.qi4j.api.query.decorator;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class CachingQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory delegate;

    public CachingQueryFactory( QueryBuilderFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new CachingQuery<T>( delegate.newQueryBuilder( resultType ) );
    }
}
