package org.qi4j.api.query.decorator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.query.Query;

/**
 * TODO
 */
public class CachingQuery<T> extends QueryDecorator<T>
{
    private Iterable<T> find;
    private T findSingle;

    public CachingQuery( Query<T> query )
    {
        super( query );
    }

    public void resultType( Class mixinType )
    {
        clear();

        query.resultType( mixinType );
    }

    public <K> K where( Class<K> mixinType )
    {
        clear();

        return query.where( mixinType );
    }

    public <K> K where( Class<K> mixinType, Query.Is comparisonOperator )
    {
        clear();

        return query.where( mixinType, comparisonOperator );
    }

    public <K> K orderBy( Class<K> mixinType )
    {
        clear();

        return query.orderBy( mixinType );
    }

    public <K> K orderBy( Class<K> mixinType, Query.OrderBy order )
    {
        clear();

        return query.orderBy( mixinType, order );
    }


    public void setFirstResult( int beginIndex )
    {
        clear();

        query.setFirstResult( beginIndex );
    }

    public void setMaxResults( int endIndex )
    {
        clear();

        query.setMaxResults( endIndex );
    }

    public Iterable<T> prepare()
    {
        if( find != null )
        {
            return find;
        }

        // Copy and cache list of results
        List<T> findList = new ArrayList<T>();
        Iterable<T> currentFind = query.prepare();
        for( T t : currentFind )
        {
            findList.add( t );
        }
        find = findList;


        return find;
    }


    public Query<T> copy()
    {
        return new CachingQuery<T>( query.copy() );
    }

    public T find()
    {
        if( findSingle != null )
        {
            return findSingle;
        }

        findSingle = query.find();

        return findSingle;
    }

    public Iterator<T> iterator()
    {
        return prepare().iterator();
    }

    public void clear()
    {
        find = null;
        findSingle = null;
    }
}