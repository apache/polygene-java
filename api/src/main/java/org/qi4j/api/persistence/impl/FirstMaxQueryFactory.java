package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public class FirstMaxQueryFactory
    implements QueryFactory
{
    QueryFactory delegate;

    public FirstMaxQueryFactory( QueryFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQuery(Class<T> resultType)
    {
        return new FirstMaxQuery<T>(delegate.newQuery(resultType));
    }
}