package org.qi4j.index.solr;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.query.grammar2.QuerySpecification;
import org.qi4j.api.specification.Specification;

/**
 * TODO
 */
public class SolrExpressions
{
    public static QuerySpecification search(String queryString)
    {
        return new QuerySpecification( "solr", queryString );
    }
}
