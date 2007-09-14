package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public class ResultTypeQueryFactory
    implements QueryFactory
{
    QueryFactory delegate;

    public ResultTypeQueryFactory( QueryFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return new ResultTypeQuery<T>( delegate.newQuery( resultType ), resultType );
    }
}