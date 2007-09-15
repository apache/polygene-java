package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

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