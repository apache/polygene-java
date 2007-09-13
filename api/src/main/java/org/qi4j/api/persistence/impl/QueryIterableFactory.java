package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public class QueryIterableFactory
    implements QueryFactory
{
    Iterable objects;

    public QueryIterableFactory( Iterable objects )
    {
        this.objects = objects;
    }

    public <T> Query<T> newQuery(Class<T> resultType)
    {
        return new QueryIterableImpl<T>((Iterable<T>) objects);
    }
}