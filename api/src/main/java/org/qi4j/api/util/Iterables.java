/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.specification.Specification;

/**
 * Utility methods for working with Iterables. See test for examples of how to use.
 */
public class Iterables
{
    public static <T> Collection<T> addAll( Collection<T> collection, Iterable<T> iterable )
    {
        for( T item : iterable )
        {
            collection.add( item );
        }

        return collection;
    }

    public static long count( Iterable<?> iterable )
    {
        long c = 0;
        for( Object item : iterable )
        {
            c++;
        }
        return c;
    }

    public static <X> Iterable<X> filter( Specification<X> specification, Iterable<X> i )
    {
        return new FilterIterable<X>( i, specification );
    }

    public static <X> X first( Iterable<X> i )
    {
        Iterator<X> iter = i.iterator();
        if( iter.hasNext() )
        {
            return iter.next();
        }
        else
        {
            return null;
        }
    }

    public static <T> boolean matchesAny( Specification<T> specification, Iterable<T> iterable )
    {
        for( T item : iterable )
        {
            if( specification.satisfiedBy( item ) )
            {
                return true;
            }
        }
        return false;
    }

    public static <X> Iterable<X> flatten( Iterable<X>... multiIterator )
    {
        return new FlattenIterable<X>( Arrays.asList( multiIterator ) );
    }

    public static <X> Iterable<X> flatten( Iterable<Iterable<X>> multiIterator )
    {
        return new FlattenIterable<X>( multiIterator );
    }

    public static <FROM, TO> Iterable<TO> map( Function<FROM, TO> function, Iterable<FROM> from )
    {
        return new MapIterable<FROM, TO>( from, function );
    }

    public static <T> Iterable<T> iterable( Enumeration<T> enumeration )
    {
        List<T> list = new ArrayList<T>();
        while( enumeration.hasMoreElements() )
        {
            T item = enumeration.nextElement();
            list.add( item );
        }

        return list;
    }

    public static <T> Iterable<T> iterable( T... items )
    {
        return Arrays.asList( items );
    }

    private static class MapIterable<FROM, TO>
        implements Iterable<TO>
    {
        private final Iterable<FROM> from;
        private final Function<FROM, TO> function;

        public MapIterable( Iterable<FROM> from, Function<FROM, TO> function )
        {
            this.from = from;
            this.function = function;
        }

        public Iterator<TO> iterator()
        {
            return new MapIterator<FROM, TO>( from.iterator(), function );
        }

        static class MapIterator<FROM, TO>
            implements Iterator<TO>
        {
            private final Iterator<FROM> fromIterator;
            private final Function<FROM, TO> function;

            public MapIterator( Iterator<FROM> fromIterator, Function<FROM, TO> function )
            {
                this.fromIterator = fromIterator;
                this.function = function;
            }

            public boolean hasNext()
            {
                return fromIterator.hasNext();
            }

            public TO next()
            {
                return function.map( fromIterator.next() );
            }

            public void remove()
            {
                fromIterator.remove();
            }
        }
    }

    private static class FilterIterable<T>
        implements Iterable<T>
    {
        private Iterable<T> iterable;

        private Specification<T> specification;

        public FilterIterable( Iterable<T> iterable, Specification<T> specification )
        {
            this.iterable = iterable;
            this.specification = specification;
        }

        public Iterator<T> iterator()
        {
            return new FilterIterator<T>( iterable.iterator(), specification );
        }

        static class FilterIterator<T>
            implements Iterator<T>
        {
            private Iterator<T> iterator;

            private Specification<T> specification;

            private T currentValue;
            boolean finished = false;
            boolean nextConsumed = true;

            public FilterIterator( Iterator<T> iterator, Specification<T> specification )
            {
                this.specification = specification;
                this.iterator = iterator;
            }

            public boolean moveToNextValid()
            {
                boolean found = false;
                while( !found && iterator.hasNext() )
                {
                    T currentValue = iterator.next();
                    if( specification.satisfiedBy( currentValue ) )
                    {
                        found = true;
                        this.currentValue = currentValue;
                        nextConsumed = false;
                    }
                }
                if( !found )
                {
                    finished = true;
                }
                return found;
            }

            public T next()
            {
                if( !nextConsumed )
                {
                    nextConsumed = true;
                    return currentValue;
                }
                else
                {
                    if( !finished )
                    {
                        if( moveToNextValid() )
                        {
                            nextConsumed = true;
                            return currentValue;
                        }
                    }
                }
                return null;
            }

            public boolean hasNext()
            {
                return !finished &&
                       ( !nextConsumed || moveToNextValid() );
            }

            public void remove()
            {
            }
        }
    }

    private static class FlattenIterable<T>
        implements Iterable<T>
    {
        private Iterable<Iterable<T>> iterable;

        public FlattenIterable( Iterable<Iterable<T>> iterable )
        {
            this.iterable = iterable;
        }

        public Iterator<T> iterator()
        {
            return new FlattenIterator<T>( iterable.iterator() );
        }

        static class FlattenIterator<T>
            implements Iterator<T>
        {
            private Iterator<Iterable<T>> iterator;
            private Iterator<T> currentIterator;

            public FlattenIterator( Iterator<Iterable<T>> iterator )
            {
                this.iterator = iterator;
                currentIterator = null;
            }

            public boolean hasNext()
            {
                if( currentIterator == null )
                {
                    if( iterator.hasNext() )
                    {
                        currentIterator = iterator.next().iterator();
                    }
                    else
                    {
                        return false;
                    }
                }

                while( !currentIterator.hasNext() &&
                       iterator.hasNext() )
                {
                    currentIterator = iterator.next().iterator();
                }

                return currentIterator.hasNext();
            }

            public T next()
            {
                return currentIterator.next();
            }

            public void remove()
            {
                if( currentIterator == null )
                {
                    throw new IllegalStateException();
                }

                currentIterator.remove();
            }
        }
    }
}
