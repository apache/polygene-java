package org.qi4j.api.persistence.impl;

import java.util.Iterator;

/**
 * TODO
 */
public class ResultTypeIterable<T>
    implements Iterable<T>
{
    private Iterable<T> iterable;
    private Iterable<Class> resultTypes;

    public ResultTypeIterable( Iterable<T> iterable, Iterable<Class> resultTypes )
    {
        this.resultTypes = resultTypes;
        this.iterable = iterable;
    }

    public Iterator<T> iterator()
    {
        return new ResultTypeIterator<T>( iterable.iterator() );
    }

    private class ResultTypeIterator<K>
        implements Iterator<K>
    {
        Iterator<K> iterator;

        K next;

        public ResultTypeIterator( Iterator<K> iterator )
        {
            this.iterator = iterator;
        }

        public boolean hasNext()
        {
            nextLoop:
            while( next == null && iterator.hasNext() )
            {
                K nextTest = iterator.next();
                for( Class resultType : resultTypes )
                {
                    if( !resultType.isInstance( nextTest ) )
                    {
                        continue nextLoop;
                    }
                }
                next = nextTest;
            }

            return next != null;
        }

        public K next()
        {
            try
            {
                return next;
            }
            finally
            {
                next = null;
            }
        }

        public void remove()
        {
            iterator.remove();
        }
    }
}