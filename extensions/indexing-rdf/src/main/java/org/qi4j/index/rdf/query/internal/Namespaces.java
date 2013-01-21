/*
 * Copyright 2008 Michael Hunger.
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
package org.qi4j.index.rdf.query.internal;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class Namespaces
{
    private int namespaceCounter = 0;

    /**
     * Mapping between namespace and prefix.
     */
    private final Map<String, String> namespaces = new HashMap<String, String>();

    public Namespaces()
    {
        addDefaultNamespaces();
    }

    private void addDefaultNamespaces()
    {
        addNamespace( "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
        addNamespace( "rdfs", "http://www.w3.org/2000/01/rdf-schema#" );
    }

    public Iterable<? extends String> namespaces()
    {
        return namespaces.keySet();
    }

    public String addNamespace( final String namespace )
    {
        String prefix = namespacePrefix( namespace );
        if( prefix != null )
        {
            return prefix;
        }
        prefix = "ns" + namespaceCounter++;
        return addNamespace( prefix, namespace );
    }

    public String namespacePrefix( String namespace )
    {
        return namespaces.get( namespace );
    }

    public String addNamespace( final String prefix,
                                final String namespace
    )
    {
        namespaces.put( namespace, prefix );
        return prefix;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for( String namespace : namespaces() )
        {
            sb.append( format( "%s:%s%n", namespacePrefix( namespace ), namespace ) );
        }
        return sb.toString();
    }
}
