/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;


public final class ElasticSearchFinderSupport
{

    /* package */ static Object resolveVariable( Object value, Map<String, Object> variables )
    {
        if( value == null )
        {
            return null;
        }
        if( value instanceof Variable )
        {
            Variable var = (Variable) value;
            Object realValue = variables.get( var.variableName() );
            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
            }
            return realValue;
        }
        return value;
    }

    public static interface ComplexTypeSupport
    {
        ComplexTypeSupport support(Module module, ElasticSearchSupport support);

        FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables );

        FilterBuilder contains( ContainsSpecification<?> spec, Map<String, Object> variables );

        FilterBuilder containsAll( ContainsAllSpecification<?> spec, Map<String, Object> variables );

        void orderBy(SearchRequestBuilder request,  Specification<Composite> whereClause, OrderBy orderBySegment, Map<String, Object> variables ) throws EntityFinderException;
    }

    private ElasticSearchFinderSupport()
    {
    }

}
