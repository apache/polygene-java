package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public class WhereQueryFactory
    implements QueryFactory
{
    QueryFactory delegate;

    public WhereQueryFactory( QueryFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQuery( Class<T> resultType )
    {
        return new WhereQuery<T>( delegate.newQuery( resultType ) );
    }
}