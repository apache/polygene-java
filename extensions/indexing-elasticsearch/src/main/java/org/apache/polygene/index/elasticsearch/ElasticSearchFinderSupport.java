/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.elasticsearch;

import java.util.Map;
import org.apache.polygene.api.query.grammar.ComparisonPredicate;
import org.apache.polygene.api.query.grammar.ContainsAllPredicate;
import org.apache.polygene.api.query.grammar.ContainsPredicate;
import org.apache.polygene.api.query.grammar.Variable;
import org.elasticsearch.index.query.QueryBuilder;


/* package */ final class ElasticSearchFinderSupport
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

    /* package */ static interface ComplexTypeSupport
    {

        QueryBuilder comparison( ComparisonPredicate<?> spec, Map<String, Object> variables );

        QueryBuilder contains( ContainsPredicate<?> spec, Map<String, Object> variables );

        QueryBuilder containsAll( ContainsAllPredicate<?> spec, Map<String, Object> variables );

    }

    private ElasticSearchFinderSupport()
    {
    }

}
