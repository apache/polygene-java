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

import java.util.List;
import java.util.Map;
import org.qi4j.api.query.grammar.OrderBy;

/**
 * Descriptor of Named Query.
 * <p>
 * Named queries are a way to support native complex queries that are not possible to express with the Fluent Query API.
 * </p>
 * <p>
 * The upside is that Named Queries can express arbitrarily complex queries, at the expense of being tied to the
 * query language(s) supported by the indexing engine AND that the storage format of the indexing engine must be known
 * since the named query is not expressed in domain model terms.
 * </p>
 * <p>
 * Named queries must be declared at bootstrap, and can not be added adhoc while the application is running. This is
 * to ensure that programmers don't litter the domain code with index engine specific code, making a change much
 * harder.
 * </p>
 * <p>
 * Named Queries are indexing engine specific, and aggregated to the {@link NamedQueries} class. For the standard
 * Sparql indexing engine, it would look like this;
 * </p>
 * <code><pre>
 *
 * NamedQueries namedQueries = new NamedQueries();
 *
 * NamedQueryDescriptor queryDescriptor = new NamedSparqlDescriptor( queryString );
 *
 * namedQueries.addQuery( queryName, queryDescriptor );
 *
 * module.addServices( RdfIndexerExporterComposite.class ).setMetaInfo( namedQueries );
 *
 * </pre></code>
 */
public interface NamedQueryDescriptor
{
    /**
     * Returns the name of the query.
     *
     * @return the name of the query as it is declared.
     */
    String name();

    /**
     * Creates a valid Query string.
     *
     * @param variables       The variables used, and their values.
     * @param orderBySegments The list of OrderBy instrctions.
     * @param firstResult     The offset into the resultset.
     * @param maxResults      The maximum number of results to be returned.
     *
     * @return A valid query in the language given.
     */
    String compose( Map<String, Object> variables,
                    OrderBy[] orderBySegments,
                    Integer firstResult,
                    Integer maxResults
    );

    /**
     * Returns the name of the query language.
     *
     * @return The formal name of the query language.
     */
    String language();

    /**
     * Returns a list of variable names allowed in the query.
     *
     * @return a list of variable names allowed in the query.
     */
    List<String> variableNames();
}
