package org.qi4j.spi.query;

import java.util.Iterator;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Specification;

/**
 * Query Source, used in QueryBuilder SPI.
 */
public interface QuerySource
{
    <T> T find( Class<T> resultType,
                Specification<Composite> whereClause,
                Iterable<OrderBy> orderBySegments,
                Integer firstResult,
                Integer maxResults,
                Map<String, Object> variables
    );

    <T> long count( Class<T> resultType,
                    Specification<Composite> whereClause,
                    Iterable<OrderBy> orderBySegments,
                    Integer firstResult,
                    Integer maxResults,
                    Map<String, Object> variables
    );

    <T> Iterator<T> iterator( Class<T> resultType,
                              Specification<Composite> whereClause,
                              Iterable<OrderBy> orderBySegments,
                              Integer firstResult,
                              Integer maxResults,
                              Map<String, Object> variables
    );
}
