/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.entity.index.rdf;

import org.openrdf.query.QueryLanguage;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since April 02, 2008
 */
public interface RDFQueryParser
{
    QueryLanguage getQueryLanguage();

    String getQuery( Class resultType,
                     BooleanExpression whereClause,
                     OrderBy[] orderBySegments,
                     Integer firstResult,
                     Integer maxResults );
}