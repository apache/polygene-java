package org.qi4j.api.query.decorator;

import java.util.Iterator;

/**
 * TODO
 */
public class WhereIterable<T> implements Iterable<T>
{
    private Iterable<T> iterable;
    private Iterable<WhereConstraint> constraints;

    public WhereIterable( Iterable<T> iterable, Iterable<WhereConstraint> constraints )
    {
        this.iterable = iterable;
        this.constraints = constraints;
    }

    public Iterator<T> iterator()
    {
        return new WhereIterator<T>( iterable.iterator() );
    }

    private class WhereIterator<K>
        implements Iterator<K>
    {
        Iterator<K> iterator;

        K next;

        public WhereIterator( Iterator<K> iterator )
        {
            this.iterator = iterator;
        }

        public boolean hasNext()
        {
            nextLoop:
            while( next == null && iterator.hasNext() )
            {
                K nextTest = iterator.next();
                for( WhereConstraint constraint : constraints )
                {
                    if( !constraint.accepts( nextTest ) )
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