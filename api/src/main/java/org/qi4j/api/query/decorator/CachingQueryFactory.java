package org.qi4j.api.query.decorator;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

/**
 * TODO
 */
public class CachingQueryFactory
    implements QueryFactory
{
    QueryFactory delegate;

    public CachingQueryFactory( QueryFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return new CachingQuery<T>( delegate.newQuery( resultType ) );
    }
}
