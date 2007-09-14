package org.qi4j.api.persistence.impl;

import java.util.Iterator;

/**
 * TODO
 */
public class FirstMaxIterable<T>
    implements Iterable<T>
{
    private Iterable<T> iterable;
    private int first;
    private int max;

    public FirstMaxIterable( Iterable<T> iterable, int first, int max )
    {
        this.iterable = iterable;
        this.first = first;
        this.max = max;
    }

    public Iterator<T> iterator()
    {
        Iterator<T> iterator = iterable.iterator();

        // Remove first nr of objects
        for( int i = 0; i < first && iterator.hasNext(); i++ )
        {
            iterator.next();
        }

        if( max == -1 )
        {
            return iterator();
        }

        return new CountIterator( iterator, max );
    }

    private class CountIterator<T>
        implements Iterator<T>
    {
        Iterator<T> iterator;
        int count;

        public CountIterator( Iterator<T> iterator, int count )
        {
            this.iterator = iterator;
            this.count = count;
        }

        public boolean hasNext()
        {
            return count >= 0 && iterator.hasNext();
        }

        public T next()
        {
            count--;
            return iterator.next();
        }

        public void remove()
        {
        }
    }
}
