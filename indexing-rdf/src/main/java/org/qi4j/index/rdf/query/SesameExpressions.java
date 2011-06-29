package org.qi4j.index.rdf.query;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.query.grammar2.QuerySpecification;
import org.qi4j.api.specification.Specification;

/**
 * Sesame specific operators to be used with QueryBuilder.where().
 */
public class SesameExpressions
{
    public static QuerySpecification sparql(String sparql)
    {
        return new QuerySpecification("SPARQL", sparql);
    }

    public static QuerySpecification serql(String serql)
    {
        return new QuerySpecification("SERQL", serql);
    }
}
