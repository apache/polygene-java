package org.qi4j.assembly;

import org.qi4j.model.InjectionKey;
import org.qi4j.query.QueryBuilderFactory;

/**
 * TODO
 */
public interface QueryBinder
{
    QueryBinder bind( InjectionKey key, QueryBuilderFactory queryBuilderFactory );
}
