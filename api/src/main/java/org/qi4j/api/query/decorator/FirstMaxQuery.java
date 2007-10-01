package org.qi4j.api.query.decorator;

import java.util.Iterator;
import org.qi4j.api.query.Query;

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

    private FirstMaxQuery( FirstMaxQuery<T> copy )
    {
        super( copy.query.copy() );

        begin = copy.begin;
        end = copy.end;
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


    public Query<T> copy()
    {
        return new FirstMaxQuery<T>( this );
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