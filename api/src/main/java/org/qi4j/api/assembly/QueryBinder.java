package org.qi4j.api.assembly;

import org.qi4j.api.model.InjectionKey;
import org.qi4j.api.query.QueryBuilderFactory;

/**
 * TODO
 */
public interface QueryBinder
{
    QueryBinder bind( InjectionKey key, QueryBuilderFactory queryBuilderFactory );
}
