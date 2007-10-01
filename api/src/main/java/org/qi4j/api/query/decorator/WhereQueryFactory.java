package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public class WhereQueryFactory
    implements QueryBuilderFactory
{
    QueryBuilderFactory delegate;

    public WhereQueryFactory( QueryBuilderFactory delegate )
    {
        this.delegate = delegate;
    }

    public <T> Query<T> newQueryBuilder( Class<T> resultType )
    {
        return new WhereQuery<T>( delegate.newQueryBuilder( resultType ) );
    }
}