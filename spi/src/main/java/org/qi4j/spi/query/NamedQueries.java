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
package org.qi4j.spi.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Named Queries aggregation class.
 * <p>
 * Named queries are aggregated into this class, via {@link #addQuery(NamedQueryDescriptor)} method, and then
 * set as the metaInfo of the EntityFinder. See {@link NamedQueryDescriptor} for more details.
 * </p>
 */
public final class NamedQueries
    implements Iterable<String>, Serializable
{
    private HashMap<String, NamedQueryDescriptor> queriesByName;

    public NamedQueries()
    {
        queriesByName = new HashMap<String, NamedQueryDescriptor>();
    }

    /**
     * Returns an Iterator of all declared named queries available.
     *
     * @return An iterator of the query names.
     */
    public Iterator<String> iterator()
    {
        return queriesByName.keySet().iterator();
    }

    public NamedQueries addQuery( NamedQueryDescriptor query )
    {
        queriesByName.put( query.name(), query );
        return this;
    }

    public NamedQueries removeQuery( String name )
    {
        queriesByName.remove( name );
        return this;
    }

    public NamedQueryDescriptor getQuery( String name )
    {
        return queriesByName.get( name );
    }
}
