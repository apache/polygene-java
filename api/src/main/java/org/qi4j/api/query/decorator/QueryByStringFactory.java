package org.qi4j.api.query.decorator;


import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryFactory;

/**
 * TODO
 */
public interface QueryByStringFactory
    extends QueryFactory

{
    <T> Query<T> newQuery( Class<T> resultType, String query );
}
