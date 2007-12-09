/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.qi4j.query.value.VariableExpression;

public class QueryImpl<R>
    implements Query<R>
{
    private Class resultType;
    private Map<String, Object> variables = Collections.EMPTY_MAP;
    private List<BooleanExpression> where;
    private List<OrderBy> orderBy;
    private int firstResult;
    private int maxResults;
    private Queryable queryable;

    public QueryImpl( Class resultType, ArrayList<BooleanExpression> where, ArrayList<OrderBy> orderBy, int firstResult, int maxResults , Queryable queryable)
    {
        this.resultType = resultType;
        this.where = where;
        this.orderBy = orderBy;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.queryable = queryable;
    }

    public void setVariable( String name, Object value )
    {
        if( variables == Collections.EMPTY_MAP )
        {
            variables = new HashMap<String, Object>();
        }
        variables.put( name, value );
    }

    public Map<String, Object> getVariables()
    {
        // Get defaults
        Map<String, Object> variableTmp = new HashMap<String, Object>();
        for( BooleanExpression booleanExpression : where )
        {
            getVariableMap( booleanExpression, variableTmp );
        }

        variableTmp.putAll( variables );

        return variableTmp;
    }

    public R find()
    {
        return queryable.find( this );
    }

    public Iterator<R> iterator()
    {
        return queryable.iterable( this ).iterator();
    }

    public Class getResultType()
    {
        return resultType;
    }

    public List<BooleanExpression> getWhere()
    {
        return where;
    }

    public List<OrderBy> getOrderBy()
    {
        return orderBy;
    }

    public int getFirstResult()
    {
        return firstResult;
    }

    public int getMaxResults()
    {
        return maxResults;
    }

    public Map<String, Object> getSetVariables()
    {
        return variables;
    }

    private void getVariableMap( Expression expression, Map<String, Object> variableTmp )
    {
        if( expression instanceof BinaryOperator )
        {
            BinaryOperator binExpr = (BinaryOperator) expression;
            Expression left = binExpr.getLeftArgument();
            getVariableMap( left, variableTmp );
            Expression right = binExpr.getRightArgument();
            getVariableMap( right, variableTmp );
        }
        else if( expression instanceof UnaryOperator )
        {
            UnaryOperator unaryExpr = (UnaryOperator) expression;
            Expression expr = unaryExpr.getArgument();
            getVariableMap( expr, variableTmp );
        }
        else if( expression instanceof VariableExpression )
        {
            VariableExpression varExpr = (VariableExpression) expression;
            variableTmp.put( varExpr.getName(), varExpr.getDefaultValue() );
        }
    }
}
