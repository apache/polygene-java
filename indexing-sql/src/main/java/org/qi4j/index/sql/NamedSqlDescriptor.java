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
package org.qi4j.index.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.NamedQueryDescriptor;

public class NamedSqlDescriptor
    implements NamedQueryDescriptor, Serializable
{
    private String query;
    private String name;
    private List<String> variableNames;

    public NamedSqlDescriptor( String name, String query, List<String> variableNames )
    {
        if( name == null || name.length() == 0 )
        {
            throw new NullPointerException( "Queries must have a name." );
        }
        if( query == null )
        {
            throw new NullPointerException( "Null queries are not allowed" );
        }
        if( query.length() == 0 )
        {
            throw new IllegalArgumentException( "Empty query strings are not allowed." );
        }
        if( variableNames != null )
        {
            this.variableNames = variableNames;
        }
        else
        {
            this.variableNames = new ArrayList<String>();
        }
        this.name = name;
        this.query = query;
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
        return "SQL";
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
