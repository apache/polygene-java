package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public interface QueryByStringFactory
    extends QueryBuilderFactory

{
    <T> Query<T> newQuery( Class<T> resultType, String query );
}
