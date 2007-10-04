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
package org.qi4j.api.query;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

public class QueryBuilderImpl<R>
    implements QueryBuilder<R>
{
    private Class resultType;
    private ArrayList<BinaryExpression> where;
    private ArrayList<OrderBy> orderBy;

    public QueryBuilderImpl( Class<R> resultType )
    {
        this.resultType = resultType;
        where = new ArrayList<BinaryExpression>();
        orderBy = new ArrayList<OrderBy>();

    }

    public <K> K parameter( Class<K> mixinType )
    {
        ClassLoader loader = QueryBuilderImpl.class.getClassLoader();
        Class[] intfaces = new Class[]{ mixinType };
        InvocationHandler handler = new InterfaceInvocationHandler();
        return (K) Proxy.newProxyInstance( loader, intfaces, handler );
    }

    public QueryBuilder<R> where( BinaryExpression... expressions )
    {
        for( BinaryExpression expr : expressions )
        {
            where.add( expr );
        }
        return this;
    }

    public QueryBuilder<R> orderBy( Object property )
    {
        Expression expression = QueryStack.popExpression();
        OrderBy by = new OrderBy( expression, OrderBy.Order.ASCENDING );
        orderBy.add( by );
        return this;
    }

    public QueryBuilder<R> orderBy( Object property, OrderBy.Order orderBy )
    {
        Expression expression = QueryStack.popExpression();
        OrderBy by = new OrderBy( expression, orderBy );
        this.orderBy.add( by );
        return this;
    }

    public QueryBuilder<R> setFirstResult( int firstResult )
    {
        //TODO: Auto-generated, need attention.
        return this;
    }

    public QueryBuilder<R> setMaxResults( int maxResults )
    {
        //TODO: Auto-generated, need attention.
        return this;
    }

    public Query<R> newQuery()
    {
        return new QueryImpl<R>( resultType, where, orderBy );
    }

}
