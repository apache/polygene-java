package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

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