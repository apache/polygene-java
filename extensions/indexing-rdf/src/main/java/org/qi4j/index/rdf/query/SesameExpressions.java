package org.qi4j.index.rdf.query;

import org.qi4j.api.query.grammar.QuerySpecification;

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
