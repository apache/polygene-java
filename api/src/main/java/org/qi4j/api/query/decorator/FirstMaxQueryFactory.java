package org.qi4j.api.query.decorator;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class FirstMaxQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory delegate;

    public FirstMaxQueryFactory( QueryBuilderFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new FirstMaxQuery<T>( delegate.newQueryBuilder( resultType ) );
    }
}