package org.qi4j.api.query;

/**
 * TODO
 */
public interface Queryable
{
    <T> T find( Query<T> query );

    <T> Iterable<T> iterable( Query<T> query );
}
