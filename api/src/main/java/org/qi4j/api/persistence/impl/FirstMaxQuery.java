package org.qi4j.api.persistence.impl;

import java.util.Iterator;
import org.qi4j.api.persistence.Query;

/**
 * TODO
 */
public class FirstMaxQuery<T> extends QueryDecorator<T>
{
    private int begin = 0;
    private int end = -1;

    public FirstMaxQuery( Query<T> query )
    {
        super( query );
    }

    public void setFirstResult( int beginIndex )
    {
        begin = beginIndex;
    }

    public void setMaxResults( int endIndex )
    {
        end = endIndex;
    }

    public Iterable<T> prepare()
    {
        if( begin == 0 && end == -1 )
        {
            return query.prepare();
        }
        else
        {
            return new FirstMaxIterable<T>( query.prepare(), begin, end );
        }
    }

    public T find()
    {
        Iterator<T> iterator = prepare().iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }
}