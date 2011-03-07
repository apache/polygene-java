/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.query.QueryLanguage;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.NamedQueryDescriptor;

public class NamedSparqlDescriptor
    implements NamedQueryDescriptor, Serializable
{
    private static final List<String> EMPTY_LIST = new ArrayList<String>();

    private String query;
    private String name;
    private List<String> variableNames = new ArrayList<String>();

    public NamedSparqlDescriptor( String name, String query )
    {
        if( name == null )
        {
            throw new NullPointerException( "Queries must have a name" );
        }
        if( query == null )
        {
            throw new NullPointerException( "Null queries are not allowed" );
        }
        if( query.length() == 0 )
        {
            throw new IllegalArgumentException( "Empty query strings are not allowed." );
        }
        this.name = name;
        this.query = query;

        Matcher variableMatcher = Pattern.compile("\\?([\\p{Alpha}]*)").matcher(query);
        while (variableMatcher.find())
        {
            String variableName = variableMatcher.group(1);
            if (!variableNames.contains(variableName))
                variableNames.add(variableName);
        }
    }

    public String name()
    {
        return name;
    }

    public String compose( Map<String, Object> variables,
                           OrderBy[] orderBySegments,
                           Integer firstResult,
                           Integer maxResults
    )
    {

        processOrderBy( orderBySegments );
        StringBuffer s = new StringBuffer();
        s.append( query );
        s.append( " " );
        s.append( range( firstResult, maxResults ) );
        return s.toString();
    }

    public String language()
    {
        return QueryLanguage.SPARQL.getName();
    }

    public List<String> variableNames()
    {
        return variableNames;
    }

    private String range( Integer firstResult, Integer maxResults )
    {
        StringBuffer s = new StringBuffer();
        if( maxResults != null && maxResults > 0 )
        {
            s.append( "LIMIT " );
            s.append( maxResults );
            s.append( '\n' );
        }
        if( firstResult != null && firstResult > 0 )
        {
            s.append( " OFFSET " );
            s.append( firstResult );
            s.append( '\n' );
        }
        return s.toString();
    }

    private void processOrderBy( OrderBy[] orderBySegments )
    {
        if( orderBySegments == null )
        {
            return;
        }
        String keyLocator = "ORDER BY ";
        int pos = query.indexOf( keyLocator ) + keyLocator.length();
        for( OrderBy order : orderBySegments )
        {
            while( Character.isWhitespace( query.charAt( pos ) ) )
            {
                pos = pos + 1;
            }
            String existing = query.substring( pos, pos + 4 ).toUpperCase();
            if( existing.startsWith( "ASC" ) && order.order().equals( OrderBy.Order.DESCENDING ) )
            {
                String pre = query.substring( 0, pos );
                String post = query.substring( pos + 3 );
                query = pre + "DESC" + post;
            }
            else if( existing.startsWith( "DESC" ) && order.order().equals( OrderBy.Order.ASCENDING ) )
            {
                String pre = query.substring( 0, pos );
                String post = query.substring( pos + 4 );
                query = pre + "ASC" + post;
            }
            while( query.charAt( pos ) != ')' )
            {
                pos = pos + 1;
            }
            pos++;
        }
    }
}
