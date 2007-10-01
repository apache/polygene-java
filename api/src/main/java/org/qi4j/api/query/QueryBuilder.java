package org.qi4j.api.query;

/**
 * TODO
 */
public interface QueryBuilder
{
    <K> K parameter( Class mixinType );

    QueryBuilder from( Class compositeType );

    QueryBuilder where( QueryExpression... expression );

    QueryBuilder orderBy( Object property );

    QueryBuilder orderBy( Object property, Query.OrderBy orderBy );

    QueryBuilder firstResult( int firstResult );

    QueryBuilder maxResults( int maxResults );

    Query newQuery();
}
