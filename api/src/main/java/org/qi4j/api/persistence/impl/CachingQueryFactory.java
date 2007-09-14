package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

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
