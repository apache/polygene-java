package org.qi4j.spi.query;

import org.qi4j.api.query.Query;

/**
 * TODO
 */
public interface QueryBuilderSPI<T>
{
    Query<T> newQuery(QuerySource querySource );
}
