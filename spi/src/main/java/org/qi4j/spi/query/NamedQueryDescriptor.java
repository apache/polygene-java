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

import java.util.Map;
import org.qi4j.api.query.grammar.OrderBy;

public interface NamedQueryDescriptor
{
    /** Creates a valid Query string.
     *
     * @param variables The variables used, and their values.
     * @param orderBySegments The list of OrderBy instrctions.
     * @param firstResult The offset into the resultset.
     * @param maxResults The maximum number of results to be returned.
     * @return A valid query in the language given.
     */
    String compose( Map<String, Object> variables,
                    OrderBy[] orderBySegments,
                    Integer firstResult,
                    Integer maxResults );

    /** Returns the name of the query language.
     *
     * @return The formal name of the query language.
     */
    String language();
}
