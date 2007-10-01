package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class ResultTypeQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory delegate;

    public ResultTypeQueryFactory( QueryBuilderFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new ResultTypeQuery<T>( delegate.newQueryBuilder( resultType ), resultType );
    }
}