package org.qi4j.api.query.decorator;

import java.util.Iterator;
import org.qi4j.api.query.Query;

/**
 * TODO
 */
public class QueryDecorator<T> implements Query<T>
{
    protected Query<T> query;

    public QueryDecorator( Query<T> query )
    {
        this.query = query;
    }

    public void resultType( Class mixinType )
    {
        query.resultType( mixinType );
    }

    public <K> K where( Class<K> mixinType )
    {
        return query.where( mixinType );
    }

    public <K> K where( Class<K> mixinType, Is comparisonOperator )
    {
        return query.where( mixinType, comparisonOperator );
    }

    public <K> K orderBy( Class<K> mixinType )
    {
        return query.orderBy( mixinType );
    }

    public <K> K orderBy( Class<K> mixinType, OrderBy order )
    {
        return query.orderBy( mixinType, order );
    }


    public void setFirstResult( int beginIndex )
    {
        query.setFirstResult( beginIndex );
    }

    public void setMaxResults( int endIndex )
    {
        query.setMaxResults( endIndex );
    }

    public Iterable<T> prepare()
    {
        return query.prepare();
    }


    public Query<T> copy()
    {
        return query.copy();
    }

    public T find()
    {
        return query.find();
    }

    public Iterator<T> iterator()
    {
        return prepare().iterator();
    }

    public Query<T> getQuery()
    {
        return query;
    }
}