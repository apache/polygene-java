package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;

/**
 * This should be used when doing native queries, such as SQL, SPARQL or similar. EntityFinders can choose
 * what type of query languages they can understand by checking the language property of a QuerySpecification
 */
public class QuerySpecification
    implements Predicate<Composite>
{
    public static boolean isQueryLanguage( String language, Predicate<Composite> specification )
    {
        if( !( specification instanceof QuerySpecification ) )
        {
            return false;
        }

        return ( (QuerySpecification) specification ).language().equals( language );
    }

    private String language;
    private String query;

    public QuerySpecification( String language, String query )
    {
        this.language = language;
        this.query = query;
    }

    public String language()
    {
        return language;
    }

    public String query()
    {
        return query;
    }

    @Override
    public boolean test( Composite item )
    {
        return false;
    }

    @Override
    public String toString()
    {
        return language + ":" + query;
    }
}
