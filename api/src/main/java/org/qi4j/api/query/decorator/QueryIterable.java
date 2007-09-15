package org.qi4j.api.query.decorator;

import java.util.Iterator;
import org.qi4j.api.query.Query;


/**
 * TODO
 */
public class QueryIterable<R> implements Query<R>
{
    private Iterable<R> objects;

    public QueryIterable( Iterable<R> objects )
    {
        this.objects = objects;
    }

    public void resultType( Class mixinType )
    {
        throw new UnsupportedOperationException();
    }

    public <K> K where( Class<K> mixinType )
    {
        throw new UnsupportedOperationException();
    }

    public <K> K where( Class<K> mixinType, Is comparisonOperator )
    {
        throw new UnsupportedOperationException();
    }

    public <K> K orderBy( Class<K> mixinType )
    {
        throw new UnsupportedOperationException();
    }

    public <K> K orderBy( Class<K> mixinType, OrderBy orderBy )
    {
        throw new UnsupportedOperationException();
    }

    public void setFirstResult( int beginIndex )
    {
        throw new UnsupportedOperationException();
    }

    public void setMaxResults( int endIndex )
    {
        throw new UnsupportedOperationException();
    }

    public Iterable<R> prepare()
    {
        return objects;
    }

    public R find()
    {
        Iterator<R> iterator = prepare().iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }

    public Iterator<R> iterator()
    {
        return objects.iterator();
    }
}
