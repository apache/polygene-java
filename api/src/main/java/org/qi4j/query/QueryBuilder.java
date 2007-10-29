package org.qi4j.query;

/**
 * TODO
 */
public interface QueryBuilder<T>
{
    <K> K parameter( Class<K> mixinType );

    QueryBuilder<T> where( BinaryExpression... expression );

    QueryBuilder<T> orderBy( Object property );

    QueryBuilder<T> orderBy( Object property, OrderBy.Order order );

    QueryBuilder<T> setFirstResult( int firstResult );

    QueryBuilder<T> setMaxResults( int maxResults );

    Query<T> newQuery();
}
