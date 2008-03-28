/*
 * Copyright 2007 Niclas Hedhman.
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
package org.qi4j.runtime.query;

import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryExpressions;
import org.qi4j.query.grammar.BooleanExpression;

/**
 * Default implementation of {@link QueryBuilder}
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public final class QueryBuilderImpl<T>
    implements QueryBuilder<T>
{

    /**
     * Where clause.
     */
    private BooleanExpression where;

    /**
     * Constructor.
     */
    public QueryBuilderImpl()
    {
        where = null;
    }

    /**
     * @see QueryBuilder#where(BooleanExpression)
     */
    public QueryBuilder<T> where( final BooleanExpression expression )
    {
        if( expression == null )
        {
            throw new IllegalArgumentException( "Where expression cannot be null" );
        }
        if( where == null )
        {
            where = expression;
        }
        else
        {
            where = QueryExpressions.and( where, expression );
        }
        System.out.println( where );
        return this;
    }

    /**
     * @see QueryBuilder#newQuery()
     */
    public Query<T> newQuery()
    {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

}