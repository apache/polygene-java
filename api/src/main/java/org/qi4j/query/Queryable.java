package org.qi4j.query;

/**
 * TODO
 */
public interface Queryable
{
    <T> T find( Query<T> query );

    <T> Iterable<T> iterable( Query<T> query );
}
